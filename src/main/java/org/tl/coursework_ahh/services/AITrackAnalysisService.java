package org.tl.coursework_ahh.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tl.coursework_ahh.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AITrackAnalysisService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private TrackService trackService;

    public AITrackAnalysisService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String aiResponse(String baseTrack) {
        Track baseTrackAsObject = trackService.searchInITunes(baseTrack);

        // Get similar tracks first
        List<Track> similarTracks = trackService.searchSimilarSongs(
                baseTrackAsObject.getTrackName(),
                baseTrackAsObject.getArtistName()
        );

        String prompt = buildPrompt(baseTrackAsObject, similarTracks);
        return callOllama(prompt);
    }

    private String buildPrompt(Track baseTrack, List<Track> similarSongs) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a music expert. Analyze the song '")
                .append(baseTrack.getTrackName())
                .append("' by ")
                .append(baseTrack.getArtistName())
                .append(".\n\n");

        prompt.append("From the following list of songs, select the 10 most similar in terms of:\n")
                .append("- Music style and genre\n")
                .append("- Mood and emotional tone\n")
                .append("- Tempo and rhythm\n")
                .append("- Target audience\n\n");

        prompt.append("Available songs:\n");
        for (int i = 0; i < similarSongs.size(); i++) {
            Track song = similarSongs.get(i);
            prompt.append(i + 1).append(". ").append(song.getTrackName())
                    .append(" by ").append(song.getArtistName()).append("\n");
        }

        prompt.append("\nReturn ONLY a JSON array with exactly this structure:\n")
                .append("[\n")
                .append("  {\"songName\": \"Track Name\", \"artistName\": \"Artist Name\"}\n")
                .append("]\n\n")
                .append("Rules:\n")
                .append("- Return exactly 10 songs (or fewer if not enough available)\n")
                .append("- No explanations, no extra text\n")
                .append("- Only valid JSON array\n")
                .append("- Each object must have songName and artistName exactly as shown above");

        return prompt.toString();
    }

    private String callOllama(String prompt) {
        try {
            // Create proper JSON using Jackson
            ObjectMapper mapper = new ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode requestBody = mapper.createObjectNode();
            String modelName = "gemma3:4b";
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            // Add options for better JSON response
            com.fasterxml.jackson.databind.node.ObjectNode options = mapper.createObjectNode();
            options.put("temperature", 0.1);
            requestBody.set("options", options);

            System.out.println("🤖 Sending request to Ollama with model: " + modelName);

            String response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            String aiOutput = jsonResponse.get("response").asText();

            return extractJsonArray(aiOutput);

        } catch (Exception e) {
            System.err.println("❌ Error calling Ollama: " + e.getMessage());
            throw new RuntimeException("Error calling Ollama: " + e.getMessage(), e);
        }
    }

    private String extractJsonArray(String aiOutput) {
        // Find the JSON array in the response
        String[] lines = aiOutput.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                return line;
            }
        }

        // If no clean array found, try to extract it
        int start = aiOutput.indexOf("[");
        int end = aiOutput.lastIndexOf("]") + 1;

        if (start >= 0 && end > start) {
            return aiOutput.substring(start, end);
        }

        throw new RuntimeException("❌ Could not extract JSON array from Ollama response: " + aiOutput);
    }

    public List<Track> getFullParametersTrackList(String baseTrack) throws IOException, InterruptedException {
        List<Track> finalRankedTracks = new ArrayList<>();
        String finalAIResponse = aiResponse(baseTrack);

        try {
            String cleanedResponse = cleanJsonResponse(finalAIResponse);
            JsonNode jsonArray = objectMapper.readTree(cleanedResponse);

            System.out.println("🎵 Parsed " + jsonArray.size() + " songs from AI response");

            int successCount = 0;
            int failCount = 0;

            for (JsonNode songNode : jsonArray) {
                String songName = songNode.get("songName").asText();
                String artistName = songNode.get("artistName").asText();

                System.out.println("🔍 Multi-source search for: " + songName + " by " + artistName);

                try {
                    // Use the new multi-source search
                    Track track = trackService.searchTrack(songName, artistName);

                    if (track != null && !"Not found".equals(track.getTrackName())) {
                        finalRankedTracks.add(track);
                        successCount++;
                        System.out.println("✅ Found: " + track.getTrackName());
                    } else {
                        failCount++;
                        System.out.println("❌ Not found in any source: " + songName);
                    }

                    // Small delay to be respectful to APIs
                    Thread.sleep(500);

                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Error searching for " + songName + ": " + e.getMessage());
                    // Continue with next song
                }
            }

            System.out.println("🎉 Successfully found " + successCount + " out of " + jsonArray.size() + " songs");
            System.out.println("❌ Failed to find " + failCount + " songs");

        } catch (Exception e) {
            System.err.println("❌ Error parsing AI response: " + e.getMessage());
            // Return empty list instead of crashing
        }

        return finalRankedTracks;
    }

    private String cleanJsonResponse(String aiResponse) {
        // Remove any non-JSON text
        String response = aiResponse.trim();

        // Extract JSON array if it's wrapped in other text
        int startIndex = response.indexOf("[");
        int endIndex = response.lastIndexOf("]") + 1;

        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex);
        }

        return response;
    }
}