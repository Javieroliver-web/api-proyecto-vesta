package com.vesta.api.controller;

import com.vesta.api.service.ChatbotService;
import com.vesta.api.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/innovation")
public class InnovationController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private RecommendationService recommendationService;

    // 1. CHATBOT LEGAL (POST /api/innovation/chat)
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String pregunta = body.get("pregunta");
        String respuesta = chatbotService.responderPregunta(pregunta);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }

    // 2. RECOMENDADOR CONTEXTUAL (GET /api/innovation/recommendation)
    @GetMapping("/recommendation")
    public ResponseEntity<Map<String, String>> getRecommendation(@RequestParam String email) {
        String recomendacion = recommendationService.obtenerRecomendacion(email);
        return ResponseEntity.ok(Map.of("mensaje", recomendacion));
    }
}