package com.vesta.api.service;

import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String analizarImagen(String nombreArchivo) {
        // AQUÍ CONECTARÍAS CON OPENAI VISION API O AZURE
        // Simulamos un análisis basado en el nombre del archivo para pruebas
        
        System.out.println("🤖 IA Vesta: Procesando imagen " + nombreArchivo + "...");
        
        // Simulación: Si el archivo tiene "roto" o "golpe", la IA lo detecta
        if (nombreArchivo.toLowerCase().contains("roto") || 
            nombreArchivo.toLowerCase().contains("golpe") || 
            nombreArchivo.toLowerCase().contains("daño")) {
            return "✅ IA DETECTA: Daños visibles compatibles con siniestro. Confianza: 98%. APROBADO.";
        }
        
        return "⚠️ IA DETECTA: Imagen poco clara o sin daños evidentes. Confianza: 40%. REVISIÓN MANUAL.";
    }
}