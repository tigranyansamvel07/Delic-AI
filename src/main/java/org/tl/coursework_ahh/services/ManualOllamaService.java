package org.tl.coursework_ahh.services;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ManualOllamaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String modelName = "llama2";

    public ManualOllamaService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String prompt) {
        try {
            String requestBody = String.format(
                    "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}",
                    modelName, prompt.replace("\"", "\\\"")
            );

            String response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Error calling Ollama: " + e.getMessage(), e);
        }
    }

    public Flux<String> streamResponse(String prompt) {
        String requestBody = String.format(
                "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": true}",
                modelName, prompt.replace("\"", "\\\"")
        );

        return webClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> !chunk.trim().isEmpty())
                .map(chunk -> {
                    try {
                        JsonNode jsonChunk = objectMapper.readTree(chunk);
                        return jsonChunk.get("response").asText("");
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(response -> !response.isEmpty());
    }
}