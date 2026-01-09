package com.vesta.api.service;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public String responderPregunta(String pregunta) {
        String p = pregunta.toLowerCase();

        // 1. Saludos
        if (p.contains("hola") || p.contains("buenos") || p.contains("hey")) {
            return "🤖 VestaBot: ¡Hola! Soy tu asistente virtual. Puedo ayudarte con dudas sobre coberturas, precios, siniestros o tipos de seguros. ¿En qué te ayudo hoy?";
        }

        // 2. Coberturas específicas
        if (p.contains("cubre") && (p.contains("agua") || p.contains("mojado"))) {
            return "🤖 VestaBot: Según la cláusula 4.2, el plan 'Básico' NO cubre daños por líquidos. Necesitas el plan 'Premium' para cobertura total bajo el agua.";
        }

        // 3. Robo
        if (p.contains("robo") || p.contains("robaron") || p.contains("hurto")) {
            return "🤖 VestaBot: Sí, el robo con violencia está cubierto al 100%. Recuerda adjuntar la denuncia policial al crear el siniestro.";
        }

        // 4. Precios
        if (p.contains("precio") || p.contains("cuesta") || p.contains("vale") || p.contains("cotizar")) {
            return "🤖 VestaBot: Nuestros micro-seguros empiezan desde 1€/día. Puedes ver el catálogo completo en tu Dashboard.";
        }

        // 5. Mascotas
        if (p.contains("mascota") || p.contains("perro") || p.contains("gato") || p.contains("animal")) {
            return "🤖 VestaBot: El seguro de Mascotas cubre gastos veterinarios por accidente y enfermedad. También incluye responsabilidad civil.";
        }

        // 6. Viaje
        if (p.contains("viaje") || p.contains("vuelo") || p.contains("equipaje")) {
            return "🤖 VestaBot: Nuestro seguro de Viaje cubre gastos médicos, pérdida de equipaje y cancelaciones. ¡Perfecto para escapadas de fin de semana!";
        }

        // 7. Siniestros
        if (p.contains("siniestro") || p.contains("reportar") || p.contains("accidente") || p.contains("daño")) {
            return "🤖 VestaBot: Para reportar un siniestro, ve a la sección 'Mis Pólizas', selecciona la póliza afectada y haz clic en el botón rojo 'Reportar'.";
        }

        // 8. Eventos (NUEVO)
        if (p.contains("evento") || p.contains("concierto") || p.contains("entrada") || p.contains("festival")) {
            return "🤖 VestaBot: El seguro de Eventos protege tu entrada en caso de cancelación o enfermedad. ¡No pierdas tu dinero si no puedes ir!";
        }

        // 9. Movilidad / Bici (NUEVO)
        if (p.contains("bici") || p.contains("ciclista") || p.contains("patinete") || p.contains("movilidad")) {
            return "🤖 VestaBot: Asegura tu bicicleta o patinete eléctrico contra robos y daños. Incluye asistencia en carretera para ciclistas.";
        }

        // 10. Tecnología / Móvil (NUEVO)
        if (p.contains("movil") || p.contains("móvil") || p.contains("celular") || p.contains("tablet")
                || p.contains("tecnologia") || p.contains("tecnología") || p.contains("iphone")) {
            return "🤖 VestaBot: Protegemos tus gadgets (móviles, tablets, ordenadores) contra rotura de pantalla, líquidos y robo.";
        }

        // 11. Genérico 'Seguro' (NUEVO)
        if (p.contains("seguro") || p.contains("poliza") || p.contains("contratar")) {
            return "🤖 VestaBot: Ofrecemos seguros flexibles para: Viajes, Mascotas, Movilidad, Tecnología y Eventos. ¿Sobre cuál quieres saber más?";
        }

        // 12. Default
        return "🤖 VestaBot: No estoy seguro de entenderte. Prueba preguntándome por 'precios', 'cobertura de robo', 'seguro de mascotas' o 'cómo reportar un siniestro'.";
    }
}