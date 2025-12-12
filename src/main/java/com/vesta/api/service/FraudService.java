package com.vesta.api.service;

import org.springframework.stereotype.Service;

@Service
public class FraudService {

    public Integer calcularRiesgo(Long usuarioId, String descripcion) {
        int riesgo = 0;
        
        // Evitar nulos
        if (descripcion == null) return 100;
        
        String desc = descripcion.toLowerCase();

        // 1. Descripción muy corta (sospechoso de ser un reporte automático o con poco detalle)
        if (descripcion.length() < 20) riesgo += 30;

        // 2. Palabras clave de ALTO riesgo (patrones de fraude común)
        if (desc.contains("perdido") || desc.contains("no sé") || desc.contains("robado") || desc.contains("hurtado")) {
            riesgo += 50;
        }

        // 3. Palabras de RIESGO MEDIO (accidentes comunes pero difíciles de verificar sin prueba física)
        if (desc.contains("roto") || desc.contains("rompió") || desc.contains("agua") || desc.contains("mojado")) {
            riesgo += 15;
        }

        // 4. FACTORES MITIGANTES (Bajan el riesgo)
        // Si menciona detalles físicos concretos como "golpe" o "caída", suele ser más veraz
        if (desc.contains("golpe") || desc.contains("caída") || desc.contains("suelo") || desc.contains("accidente")) {
            riesgo = Math.max(0, riesgo - 10);
        }

        // 5. Verificar límite (0 a 100)
        return Math.min(100, riesgo);
    }
}