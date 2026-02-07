package com.vesta.api.service;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

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
            // 2. Fallback: Intentar Open-Meteo (Geocoding) PROPERLY
            try {
                Map<String, Object> location = resolveLocation(ciudad);
                return location != null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarCiudades(String query) {
        // Fallback Mock para desarrollo si no hay API Key
        if ("dummy".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
            return getMockCities(query);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://api.openweathermap.org/geo/1.0/direct?q={query}&limit=10&appid={key}";
            return restTemplate.getForObject(url, List.class, query, apiKey);
        } catch (Exception e) {
            e.printStackTrace();
            return getMockCities(query); // Fallback en caso de error
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMockCities(String query) {
        // Implementación REAL usando Open-Meteo (Gratis, sin API Key)
        try {
            RestTemplate restTemplate = new RestTemplate();
            // count=10 para asegurar scroll
            String url = "https://geocoding-api.open-meteo.com/v1/search?name={query}&count=10&language=es&format=json";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, query);

            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response
                        .get("results");

                // Mapear al formato que espera el frontend
                return results.stream().map(r -> {
                    Map<String, Object> city = new HashMap<>();
                    city.put("name", r.get("name"));
                    city.put("country", r.get("country_code")); // Usar código (ES, US)
                    city.put("state", r.get("admin1"));
                    return city;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> obtenerRecomendacion(String emailUsuario) {
        String ciudad = "Sevilla, ES";

        // 1. Obtener ciudad del usuario
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailUsuario).orElse(null);
        if (usuario != null && usuario.getCiudad() != null && !usuario.getCiudad().isEmpty()) {
            ciudad = usuario.getCiudad();
            log.info("[WEATHER] Encontrado usuario {} (ID: {}). Ciudad: {}", emailUsuario, usuario.getId(), ciudad);
        } else {
            log.warn("[WEATHER] No se pudo encontrar usuario o ciudad para email: {}. Autenticado como: {}",
                    emailUsuario,
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                            .getName());
            log.info("[WEATHER] Usando ciudad por defecto: {}", ciudad);
        }

        String ciudadNombre = ciudad.contains(",") ? ciudad.split(",")[0].trim() : ciudad;

        // 2. Consultar OpenWeatherMap
        try {
            if ("dummy".equals(apiKey))
                throw new RuntimeException("No API Key");

            RestTemplate restTemplate = new RestTemplate();
            // Añadir lang=es
            String url = "http://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units=metric&lang=es";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class, ciudad, apiKey);

            if (response != null && response.containsKey("weather")) {
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response
                        .get("weather");
                if (!weatherList.isEmpty()) {
                    String main = (String) weatherList.get(0).get("main"); // Rain, Clear, Clouds
                    String description = (String) weatherList.get(0).get("description");

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
            log.info("[WEATHER] Usando fallback Open-Meteo para ciudad: {}", ciudad);
            RestTemplate restTemplate = new RestTemplate();

            // Paso 1: Resolver ubicación con precisión (Country Code)
            Map<String, Object> location = resolveLocation(ciudad);
            log.info("[WEATHER] Resultado resolveLocation: {}", location);

            if (location != null) {
                Double lat = (Double) location.get("latitude");
                Double lon = (Double) location.get("longitude");
                log.info("[WEATHER] Coordenadas resueltas - Lat: {}, Lon: {}", lat, lon);

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
        } catch (Exception e) {
            // Fallback final
        }

        // Fallback Simulado (Solo si falla todo el proceso)
        boolean llueve = new Random().nextBoolean();
        if (llueve) {
            return Map.of(
                    "mensaje",
                    "(Simulado) Lluvia en " + ciudadNombre + ". Te recomendamos el 'Seguro de Cancelación de Eventos'.",
                    "icono", "RAIN");
        } else {
            return Map.of(
                    "mensaje", "(Simulado) Sol en " + ciudadNombre + ". ¿Tienes tu 'Seguro de Viaje Express'?",
                    "icono", "SUN");
        }
    }

    // Helper para resolver ubicación con Country Code
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveLocation(String ciudadInput) {
        if (ciudadInput == null || ciudadInput.trim().isEmpty())
            return null;

        String cityName = ciudadInput;
        String countryCode = null;

        if (ciudadInput.contains(",")) {
            String[] parts = ciudadInput.split(",");
            cityName = parts[0].trim();
            if (parts.length > 1) {
                countryCode = parts[1].trim().toUpperCase();
            }
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            // count=10 para buscar en varias coincidencias
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name={query}&count=10&language=es&format=json";

            // Buscar solo por nombre
            Map<String, Object> geoResponse = restTemplate.getForObject(geoUrl, Map.class, cityName);

            if (geoResponse != null && geoResponse.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) geoResponse.get("results");

                if (results == null || results.isEmpty())
                    return null;

                // Filtrar por código de país si existe
                if (countryCode != null) {
                    for (Map<String, Object> res : results) {
                        String resCountry = (String) res.get("country_code");
                        if (resCountry != null && resCountry.equalsIgnoreCase(countryCode)) {
                            return res;
                        }
                    }
                }

                // Si no hay código de país o no se encontró match, devolver el primero
                return results.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}