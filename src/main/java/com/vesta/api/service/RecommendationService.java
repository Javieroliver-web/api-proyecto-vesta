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

    public boolean validarCiudad(String ciudad) {
        if ("dummy".equals(apiKey))
            return true; // Si es dummy, permitimos (mock)
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/data/2.5/weather?q=" + ciudad + "&appid=" + apiKey;
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public java.util.List<Map<String, Object>> buscarCiudades(String query) {
        if ("dummy".equals(apiKey))
            return java.util.Collections.emptyList();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/geo/1.0/direct?q=" + query + "&limit=5&appid=" + apiKey;
            return restTemplate.getForObject(url, java.util.List.class);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

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
            // Añadir lang=es
            String url = "http://api.openweathermap.org/data/2.5/weather?q=" + ciudad + "&appid=" + apiKey
                    + "&units=metric&lang=es";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("weather")) {
                java.util.List<Map<String, Object>> weatherList = (java.util.List<Map<String, Object>>) response
                        .get("weather");
                if (!weatherList.isEmpty()) {
                    String main = (String) weatherList.get(0).get("main"); // Rain, Clear, Clouds
                    String description = (String) weatherList.get(0).get("description"); // "nubes dispersas", "lluvia
                                                                                         // ligera"

                    // Limpiar nombre de ciudad (quitar ", ES" u otros códigos)
                    String ciudadNombre = ciudad.contains(",") ? ciudad.split(",")[0] : ciudad;

                    if (main.equalsIgnoreCase("Rain") || main.equalsIgnoreCase("Drizzle")
                            || main.equalsIgnoreCase("Thunderstorm")) {
                        return "🌧️ Llueve en " + ciudadNombre
                                + ". Te recomendamos el 'Seguro de Cancelación de Eventos' (-10% dto).";
                    } else if (main.equalsIgnoreCase("Clear") || main.equalsIgnoreCase("Sun")) {
                        return "☀️ ¡Sol en " + ciudadNombre
                                + "! Perfecto para una escapada. ¿Tienes tu 'Seguro de Viaje Express'?";
                    } else {
                        // Usar descripción en español (capitalizada)
                        String desc = description != null
                                ? description.substring(0, 1).toUpperCase() + description.substring(1)
                                : "Variable";
                        return "☁️ El tiempo en " + ciudadNombre + " es " + desc
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