package org.tl.coursework_ahh.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tl.coursework_ahh.services.ManualOllamaService;

@RestController
public class TestController {

    @Autowired
    private ManualOllamaService ollamaService;

    @GetMapping("/test")
    public String testOllama() {
        try {
            String response = ollamaService.generateResponse("Say 'Hello World' briefly.");
            return "Ollama is working! Response: " + response;
        } catch (Exception e) {
            return "Ollama connection failed: " + e.getMessage();
        }
    }
}
