package com.vesta.api.controller;

import com.vesta.api.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clima")

public class WeatherController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/buscar")
    public List<Map<String, Object>> buscar(@RequestParam String q) {
        return recommendationService.buscarCiudades(q);
    }
}
