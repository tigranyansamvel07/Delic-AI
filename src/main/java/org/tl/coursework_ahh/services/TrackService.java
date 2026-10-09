package org.tl.coursework_ahh.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.tl.coursework_ahh.models.Track;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TrackService {

    @Value("${lastfm.api.key}")
    private String lastFmApiKey;

    @Value("${lastfm.api.base-url}")
    private String lastFmBaseUrl;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    // ========== MULTI-SOURCE SEARCH ==========

    public Track searchTrack(String trackName, String artistName) {
        System.out.println("🔍 Multi-source search for: " + trackName + " by " + artistName);

        Track track = null;

        // Strategy 1: Try iTunes first
        try {
            track = searchInITunes(trackName, artistName);
            if (track != null && !"Not found".equals(track.getTrackName())) {
                System.out.println("✅ Found via iTunes");
                return track;
            }
        } catch (Exception e) {
            System.err.println("❌ iTunes failed: " + e.getMessage());
        }

        // Strategy 2: Try Last.fm
        try {
            track = searchInLastFm(trackName, artistName);
            if (track != null && !"Not found".equals(track.getTrackName())) {
                System.out.println("✅ Found via Last.fm");
                return track;
            }
        } catch (Exception e) {
            System.err.println("❌ Last.fm failed: " + e.getMessage());
        }

        // Strategy 3: Try Spotify
        try {
            track = searchInSpotify(trackName, artistName);
            if (track != null && !"Not found".equals(track.getTrackName())) {
                System.out.println("✅ Found via Spotify");
                return track;
            }
        } catch (Exception e) {
            System.err.println("❌ Spotify failed: " + e.getMessage());
        }

        System.out.println("🔄 Using mock data as final fallback");
        return createMockTrack(trackName, artistName);
    }


    private Track searchInITunes(String trackName, String artistName) {
        String searchTerm = trackName + " " + artistName;
        String url = "https://itunes.apple.com/search?term=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8)
                + "&media=music&limit=5";

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode results = root.get("results");

            if (results == null || !results.isArray() || results.size() == 0) {
                return null;
            }

            JsonNode firstResult = results.get(0);
            if (firstResult == null) {
                return null;
            }

            String foundTrackName = firstResult.hasNonNull("trackName") ?
                    firstResult.get("trackName").asText() : "Unknown";
            String foundArtistName = firstResult.hasNonNull("artistName") ?
                    firstResult.get("artistName").asText() : "Unknown";

            String highResCover = firstResult.hasNonNull("artworkUrl100")
                    ? firstResult.get("artworkUrl100").asText()
                    : null;
            if (highResCover != null) {
                highResCover = highResCover
                        .replace("100x100", "600x600");
                highResCover = highResCover
                        .replace("600x600", "3000x3000");
            }

            String albumCover = highResCover != null ? highResCover : "Unknown";


            return new Track(foundTrackName, foundArtistName, albumCover,
                    generateYoutubeLink(foundTrackName, foundArtistName),
                    generateSpotifyLink(foundTrackName, foundArtistName),
                    generateAppleMusicLink(foundTrackName, foundArtistName));

        } catch (Exception e) {
            System.err.println("❌ iTunes search error: " + e.getMessage());
            return null;
        }
    }


    private Track searchInLastFm(String trackName, String artistName) {
        String url = lastFmBaseUrl +
                "?method=track.getInfo" +
                "&artist=" + URLEncoder.encode(artistName, StandardCharsets.UTF_8) +
                "&track=" + URLEncoder.encode(trackName, StandardCharsets.UTF_8) +
                "&api_key=" + lastFmApiKey +
                "&format=json";

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode trackNode = root.path("track");

            if (trackNode.isMissingNode()) {
                return null;
            }

            String foundTrackName = trackNode.path("name").asText();
            String foundArtistName = trackNode.path("artist").path("name").asText();

            String albumCover = "Unknown";
            JsonNode images = trackNode.path("album").path("image");
            if (images.isArray() && images.size() > 0) {
                albumCover = images.get(images.size() - 1).path("#text").asText();
            }

            return new Track(foundTrackName, foundArtistName, albumCover,
                    generateYoutubeLink(foundTrackName, foundArtistName),
                    generateSpotifyLink(foundTrackName, foundArtistName),
                    generateAppleMusicLink(foundTrackName, foundArtistName));

        } catch (Exception e) {
            System.err.println("❌ Last.fm search error: " + e.getMessage());
            return null;
        }
    }


    private Track searchInSpotify(String trackName, String artistName) {
        try {
            String token = getSpotifyAccessToken();
            String query = "track:" + trackName + " artist:" + artistName;
            String searchUrl = "https://api.spotify.com/v1/search?q=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&type=track&limit=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("Authorization", "Bearer " + token)
                    .build();

            HttpResponse<String> searchResponse = client.send(searchRequest, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode searchJson = mapper.readTree(searchResponse.body());
            JsonNode tracks = searchJson.path("tracks").path("items");

            if (!tracks.isArray() || tracks.size() == 0) {
                return null;
            }

            JsonNode track = tracks.get(0);
            String foundTrackName = track.path("name").asText();
            String foundArtistName = track.path("artists").get(0).path("name").asText();

            String albumCover = "Unknown";
            JsonNode images = track.path("album").path("images");
            if (images.isArray() && images.size() > 0) {
                albumCover = images.get(0).path("url").asText();
            }

            String spotifyUri = track.path("uri").asText();
            String spotifyLink = spotifyUri.startsWith("spotify:track:") ?
                    "https://open.spotify.com/track/" + spotifyUri.substring(14) :
                    generateSpotifyLink(foundTrackName, foundArtistName);

            return new Track(foundTrackName, foundArtistName, albumCover,
                    generateYoutubeLink(foundTrackName, foundArtistName),
                    spotifyLink,
                    generateAppleMusicLink(foundTrackName, foundArtistName));

        } catch (Exception e) {
            System.err.println("❌ Spotify search error: " + e.getMessage());
            return null;
        }
    }


    private Track createMockTrack(String trackName, String artistName) {
        String color = generateColorFromString(trackName + artistName);
        String mockCover = "https://via.placeholder.com/300/" + color + "/ffffff?text=" +
                URLEncoder.encode(trackName.substring(0, Math.min(10, trackName.length())), StandardCharsets.UTF_8);

        return new Track(
                trackName,
                artistName,
                mockCover,
                generateYoutubeLink(trackName, artistName),
                generateSpotifyLink(trackName, artistName),
                generateAppleMusicLink(trackName, artistName)
        );
    }

    private String generateColorFromString(String input) {
        int hash = input.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;

        r = Math.max(100, r);
        g = Math.max(100, g);
        b = Math.max(100, b);

        return String.format("%02x%02x%02x", r, g, b);
    }


    public Track searchInITunes(String search) {
        String[] parts = search.split(" ", 2);
        String trackName = parts.length > 0 ? parts[0] : search;
        String artistName = parts.length > 1 ? parts[1] : "";

        return searchTrack(trackName, artistName);
    }

    public List<Track> searchSimilarSongs(String trackName, String artistName) {
        Track baseTrack = searchTrack(trackName, artistName);

        System.out.println("🔍 Searching for songs similar to: " + trackName + " by " + artistName);

        List<Track> similarTracks = new ArrayList<>();

        try {
            List<Track> lastFmTracks = fetchSimilarFromLastFm(trackName, artistName);
            similarTracks.addAll(lastFmTracks);
            System.out.println("✅ Found " + lastFmTracks.size() + " tracks from Last.fm");
        } catch (Exception e) {
            System.err.println("❌ Last.fm API failed: " + e.getMessage());
        }


        List<Track> finalTracks = removeDuplicatesAndOriginal(similarTracks, baseTrack);

        System.out.println("🎉 Final result: " + finalTracks.size() + " unique similar tracks");
        return finalTracks;
    }


    private String getSpotifyAccessToken() throws IOException, InterruptedException {
        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = new ObjectMapper().readTree(response.body());
        return json.get("access_token").asText();
    }

    public String generateYoutubeLink(String trackName, String artistName) {
        return "https://www.youtube.com/results?search_query=" +
                URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
    }

    public String generateSpotifyLink(String trackName, String artistName) {
        return "https://open.spotify.com/search/" +
                URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
    }

    public String generateAppleMusicLink(String trackName, String artistName) {
        return "https://music.apple.com/us/search?term=" +
                URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
    }

    private List<Track> fetchSimilarFromLastFm(String trackName, String artistName) throws IOException {
        String lastFmUrl = lastFmBaseUrl +
                "?method=track.getSimilar" +
                "&artist=" + URLEncoder.encode(artistName, StandardCharsets.UTF_8) +
                "&track=" + URLEncoder.encode(trackName, StandardCharsets.UTF_8) +
                "&api_key=" + lastFmApiKey +
                "&format=json&limit=15";

        System.out.println("🌐 Calling Last.fm: " + lastFmUrl);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(lastFmUrl, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        if (root.has("error")) {
            System.err.println("❌ Last.fm API error: " + root.get("message").asText());
            return new ArrayList<>();
        }

        JsonNode similarTracks = root.path("similartracks").path("track");
        List<Track> tracks = new ArrayList<>();

        if (similarTracks.isArray()) {
            for (JsonNode trackNode : similarTracks) {
                String similarTrackName = trackNode.path("name").asText();
                String similarArtistName = trackNode.path("artist").path("name").asText();

                if (!similarTrackName.isEmpty() && !similarArtistName.isEmpty()) {
                    try {
                        Track fullTrack = searchInITunes(similarTrackName + " " + similarArtistName);
                        if (fullTrack != null && !"Not found".equals(fullTrack.getTrackName())) {
                            tracks.add(fullTrack);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Failed to fetch track from iTunes: " + similarTrackName);
                    }
                }
            }
        }

        return tracks;
    }


    private List<Track> removeDuplicatesAndOriginal(List<Track> tracks, Track original) {
        Set<String> seen = new HashSet<>();
        List<Track> uniqueTracks = new ArrayList<>();

        for (Track track : tracks) {
            String key = track.getTrackName().toLowerCase() + "|" + track.getArtistName().toLowerCase();

            if (!seen.contains(key) &&
                    !track.getTrackName().equalsIgnoreCase(original.getTrackName()) &&
                    !track.getArtistName().equalsIgnoreCase(original.getArtistName())) {

                seen.add(key);
                uniqueTracks.add(track);
            }
        }

        return uniqueTracks.size() > 10 ? uniqueTracks.subList(0, 10) : uniqueTracks;
    }
}