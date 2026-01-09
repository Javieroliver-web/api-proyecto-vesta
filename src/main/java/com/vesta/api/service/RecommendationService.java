package com.vesta.api.service;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Random;

@Service
public class RecommendationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${openweather.api.key:dummy}")
    private String apiKey;

    public String obtenerRecomendacion(String emailUsuario) {
        String ciudad = "Sevilla, ES";

        // 1. Obtener ciudad del usuario
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElse(null);
        if (usuario != null && usuario.getCiudad() != null && !usuario.getCiudad().isEmpty()) {
            ciudad = usuario.getCiudad();
        }

        // 2. Consultar OpenWeatherMap
        try {
            if ("dummy".equals(apiKey))
                throw new RuntimeException("No API Key");

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/data/2.5/weather?q=" + ciudad + "&appid=" + apiKey
                    + "&units=metric";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("weather")) {
                java.util.List<Map<String, Object>> weatherList = (java.util.List<Map<String, Object>>) response
                        .get("weather");
                if (!weatherList.isEmpty()) {
                    String main = (String) weatherList.get(0).get("main"); // Rain, Clear, Clouds

                    if (main.equalsIgnoreCase("Rain") || main.equalsIgnoreCase("Drizzle")
                            || main.equalsIgnoreCase("Thunderstorm")) {
                        return "🌧️ Llueve en " + ciudad
                                + ". ¡Alerta! Te recomendamos el 'Seguro de Cancelación de Eventos' (-10% dto).";
                    } else if (main.equalsIgnoreCase("Clear") || main.equalsIgnoreCase("Sun")) {
                        return "☀️ ¡Sol en " + ciudad
                                + "! Perfecto para una escapada. ¿Tienes tu 'Seguro de Viaje Express'?";
                    } else {
                        return "☁️ El tiempo en " + ciudad + " es " + main
                                + ". Buen momento para revisar tu 'Seguro de Hogar'.";
                    }
                }
            }
        } catch (Exception e) {
            // Fallback si falla la API o no hay key
            // System.err.println("Error OpenWeather: " + e.getMessage());
        }

        // Fallback Simulado
        boolean llueve = new Random().nextBoolean();
        if (llueve) {
            return "🌧️ (Simulado) Lluvia en " + ciudad + ". Te recomendamos el 'Seguro de Cancelación de Eventos'.";
        } else {
            return "☀️ (Simulado) Sol en " + ciudad + ". ¿Tienes tu 'Seguro de Viaje Express'?";
        }
    }
}