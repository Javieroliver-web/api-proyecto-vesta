package com.vesta.api.service;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public String responderPregunta(String pregunta) {
        String p = pregunta.toLowerCase();

        if (p.contains("cubre") && (p.contains("agua") || p.contains("mojado"))) {
            return "🤖 VestaBot: Según la cláusula 4.2, el plan 'Básico' NO cubre daños por líquidos. Necesitas el plan 'Premium' para cobertura total bajo el agua.";
        } 
        else if (p.contains("robo") || p.contains("robaron")) {
            return "🤖 VestaBot: Sí, el robo con violencia está cubierto al 100%. Recuerda adjuntar la denuncia policial al crear el siniestro.";
        } 
        else if (p.contains("precio") || p.contains("cuesta")) {
            return "🤖 VestaBot: Nuestros micro-seguros empiezan desde 1€/día. Puedes ver el catálogo completo en tu Dashboard.";
        }
        
        return "🤖 VestaBot: Interesante pregunta. Como soy una IA en entrenamiento, te recomiendo contactar con un agente humano o revisar las Condiciones Generales.";
    }
}