package com.vesta.api.service;

import org.springframework.stereotype.Service;

@Service
public class FraudService {

    public Integer calcularRiesgo(Long usuarioId, String descripcion) {
        int riesgo = 0;

        // Regla 1: Descripción muy corta es sospechosa
        if (descripcion.length() < 10) riesgo += 30;

        // Regla 2: Palabras clave sospechosas
        if (descripcion.toLowerCase().contains("perdido") || descripcion.toLowerCase().contains("no sé")) {
            riesgo += 40;
        }

        // Regla 3: (Simulada) Usuario nuevo
        // if (usuarioEsNuevo(usuarioId)) riesgo += 20;

        return riesgo; // 0-100
    }
}