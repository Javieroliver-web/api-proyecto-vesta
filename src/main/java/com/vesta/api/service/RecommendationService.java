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
        if ("dummy".equals(apiKey) || apiKey == null || apiKey.isEmpty())
            return true; // Si es dummy, permitimos (mock)

        // 1. Intentar OpenWeatherMap
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}";
            restTemplate.getForObject(url, Map.class, ciudad, apiKey);
            return true;
        } catch (Exception e) {
            // 2. Fallback: Intentar Open-Meteo (Geocoding)
            try {
                RestTemplate restTemplate = new RestTemplate();
                String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name={query}&count=1&language=es&format=json";
                Map<String, Object> geoResponse = restTemplate.getForObject(geoUrl, Map.class,
                        ciudad.contains(",") ? ciudad.split(",")[0] : ciudad);

                if (geoResponse != null && geoResponse.containsKey("results")) {
                    java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) geoResponse
                            .get("results");
                    return !results.isEmpty();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public java.util.List<Map<String, Object>> buscarCiudades(String query) {
        // Fallback Mock para desarrollo si no hay API Key
        if ("dummy".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return getMockCities(query);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/geo/1.0/direct?q={query}&limit=10&appid={key}";
            return restTemplate.getForObject(url, java.util.List.class, query, apiKey);
        } catch (Exception e) {
            e.printStackTrace();
            return getMockCities(query); // Fallback en caso de error
        }
    }

    private java.util.List<Map<String, Object>> getMockCities(String query) {
        // Implementación REAL usando Open-Meteo (Gratis, sin API Key)
        try {
            RestTemplate restTemplate = new RestTemplate();
            // count=10 para asegurar scroll
            String url = "https://geocoding-api.open-meteo.com/v1/search?name={query}&count=10&language=es&format=json";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, query);

            if (response != null && response.containsKey("results")) {
                java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) response
                        .get("results");

                // Mapear al formato que espera el frontend
                return results.stream().map(r -> {
                    java.util.Map<String, Object> city = new java.util.HashMap<>();
                    city.put("name", r.get("name"));
                    city.put("country", r.get("country_code")); // Usar código (ES, US) para mantener consistencia
                    city.put("state", r.get("admin1"));
                    return city;
                }).collect(java.util.stream.Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return java.util.Collections.emptyList();
    }

    public Map<String, String> obtenerRecomendacion(String emailUsuario) {
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
            String url = "http://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units=metric&lang=es";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, ciudad, apiKey);

            if (response != null && response.containsKey("weather")) {
                java.util.List<Map<String, Object>> weatherList = (java.util.List<Map<String, Object>>) response
                        .get("weather");
                if (!weatherList.isEmpty()) {
                    String main = (String) weatherList.get(0).get("main"); // Rain, Clear, Clouds
                    String description = (String) weatherList.get(0).get("description");

                    // Limpiar nombre de ciudad
                    String ciudadNombre = ciudad.contains(",") ? ciudad.split(",")[0] : ciudad;

                    if (main.equalsIgnoreCase("Rain") || main.equalsIgnoreCase("Drizzle")
                            || main.equalsIgnoreCase("Thunderstorm")) {
                        return Map.of(
                                "mensaje",
                                "Llueve en " + ciudadNombre
                                        + ". Te recomendamos el 'Seguro de Cancelación de Eventos' (-10% dto).",
                                "icono", "RAIN");
                    } else if (main.equalsIgnoreCase("Clear") || main.equalsIgnoreCase("Sun")) {
                        return Map.of(
                                "mensaje",
                                "¡Sol en " + ciudadNombre
                                        + "! Perfecto para una escapada. ¿Tienes tu 'Seguro de Viaje Express'?",
                                "icono", "SUN");
                    } else {
                        String desc = description != null
                                ? description.substring(0, 1).toUpperCase() + description.substring(1)
                                : "Variable";
                        return Map.of(
                                "mensaje",
                                "El tiempo en " + ciudadNombre + " es " + desc
                                        + ". Buen momento para revisar tu 'Seguro de Hogar'.",
                                "icono", "CLOUD");
                    }
                }
            }
        } catch (Exception e) {
            // Fallback si falla la API
        }

        // Fallback a Open-Meteo Weather
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Paso 1: Geocodificar ciudad para obtener lat/lon
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name={query}&count=1&language=es&format=json";
            Map<String, Object> geoResponse = restTemplate.getForObject(geoUrl, Map.class,
                    ciudad.contains(",") ? ciudad.split(",")[0] : ciudad);

            if (geoResponse != null && geoResponse.containsKey("results")) {
                java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) geoResponse
                        .get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> location = results.get(0);
                    Double lat = (Double) location.get("latitude");
                    Double lon = (Double) location.get("longitude");

                    // Paso 2: Obtener clima actual
                    String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                            + "&current_weather=true";
                    Map<String, Object> weatherResponse = restTemplate.getForObject(weatherUrl, Map.class);

                    if (weatherResponse != null && weatherResponse.containsKey("current_weather")) {
                        Map<String, Object> current = (Map<String, Object>) weatherResponse.get("current_weather");
                        Integer code = (Integer) current.get("weathercode");

                        // Mapeo WMO
                        boolean isRainy = (code >= 51 && code <= 67) || (code >= 80 && code <= 82) || (code >= 95);
                        boolean isSunny = (code == 0 || code == 1);

                        String ciudadNombre = ciudad.contains(",") ? ciudad.split(",")[0] : ciudad;

                        if (isRainy) {
                            return Map.of(
                                    "mensaje",
                                    "Llueve en " + ciudadNombre
                                            + ". Te recomendamos el 'Seguro de Cancelación de Eventos' (-10% dto).",
                                    "icono", "RAIN");
                        } else if (isSunny) {
                            return Map.of(
                                    "mensaje",
                                    "¡Sol en " + ciudadNombre
                                            + "! Perfecto para una escapada. ¿Tienes tu 'Seguro de Viaje Express'?",
                                    "icono", "SUN");
                        } else {
                            return Map.of(
                                    "mensaje",
                                    "El tiempo en " + ciudadNombre
                                            + " es variable. Buen momento para revisar tu 'Seguro de Hogar'.",
                                    "icono", "CLOUD");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback final
        }

        // Fallback Simulado (Solo si falla TODO)
        boolean llueve = new Random().nextBoolean();
        if (llueve) {
            return Map.of(
                    "mensaje",
                    "(Simulado) Lluvia en " + ciudad + ". Te recomendamos el 'Seguro de Cancelación de Eventos'.",
                    "icono", "RAIN");
        } else {
            return Map.of(
                    "mensaje", "(Simulado) Sol en " + ciudad + ". ¿Tienes tu 'Seguro de Viaje Express'?",
                    "icono", "SUN");
        }
    }
}