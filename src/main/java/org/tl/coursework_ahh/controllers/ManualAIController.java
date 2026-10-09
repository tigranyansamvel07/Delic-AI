package org.tl.coursework_ahh.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tl.coursework_ahh.services.ManualOllamaService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class ManualAIController {

    @Autowired
    private ManualOllamaService ollamaService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            String response = ollamaService.generateResponse(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ChatResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return ollamaService.streamResponse(message);
    }

    // DTO classes
    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatResponse {
        private String response;

        public ChatResponse(String response) {
            this.response = response;
        }

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
    }
}