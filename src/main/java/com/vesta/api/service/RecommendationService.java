package com.vesta.api.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RecommendationService {

    public String obtenerRecomendacion(String emailUsuario) {
        // AQUÍ CONSULTARÍAS UNA API DEL TIEMPO (OpenWeatherMap)
        // Simulamos que a veces llueve y a veces hace sol
        boolean llueve = new Random().nextBoolean(); 

        if (llueve) {
            return "🌧️ Alerta de Lluvia: Se prevén tormentas hoy. Te recomendamos el 'Seguro de Cancelación de Eventos' (-10% dto).";
        } else {
            return "☀️ ¡Fin de semana soleado! Perfecto para una escapada. ¿Tienes tu 'Seguro de Viaje Express'?";
        }
    }
}