package com.vesta.api.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Service
public class ChatbotService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.vesta.api.repository.ProductoRepository productoRepository;

    // Palabras que ignorar al hacer matching (stop words)
    private static final List<String> STOP_WORDS = Arrays.asList(
            "seguro", "de", "para", "el", "la", "los", "las", "un", "una", "su", "tu");

    // Clase interna para almacenar coincidencias con puntuación
    private static class ProductoMatch {
        com.vesta.api.entity.Producto producto;
        int score;

        ProductoMatch(com.vesta.api.entity.Producto producto, int score) {
            this.producto = producto;
            this.score = score;
        }
    }

    /**
     * Calcula un score de relevancia entre la pregunta y un producto
     */
    private int calcularScoreProducto(String pregunta, com.vesta.api.entity.Producto prod) {
        int score = 0;

        // Normalizar texto
        String nombreNorm = normalizarTexto(prod.getNombre());
        String categoriaNorm = prod.getCategoria() != null ? normalizarTexto(prod.getCategoria()) : "";
        String preguntaNorm = normalizarTexto(pregunta);

        // Extraer palabras clave del nombre del producto (sin stop words)
        String[] palabrasProducto = nombreNorm.split("\\s+");
        String[] palabrasPregunta = preguntaNorm.split("\\s+");

        System.out.println("[CHATBOT] Palabras producto: " + Arrays.toString(palabrasProducto));
        System.out.println("[CHATBOT] Palabras pregunta: " + Arrays.toString(palabrasPregunta));

        // Verificar coincidencias palabra por palabra
        for (String palabraProd : palabrasProducto) {
            if (palabraProd.length() < 3 || STOP_WORDS.contains(palabraProd.toLowerCase())) {
                continue; // Ignorar palabras muy cortas o stop words
            }

            for (String palabraPreg : palabrasPregunta) {
                // Match exacto
                if (palabraPreg.equals(palabraProd)) {
                    score += 10;
                    System.out.println("[CHATBOT] +10 por match exacto: " + palabraProd);
                }
                // Match parcial (contiene)
                else if (palabraPreg.length() >= 4 && palabraProd.contains(palabraPreg)) {
                    score += 7;
                    System.out.println("[CHATBOT] +7 por match parcial: " + palabraProd + " contiene " + palabraPreg);
                } else if (palabraProd.length() >= 4 && palabraPreg.contains(palabraProd)) {
                    score += 7;
                    System.out.println(
                            "[CHATBOT] +7 por match parcial invertido: " + palabraPreg + " contiene " + palabraProd);
                }
            }
        }

        // Bonus si coincide la categoría
        if (!categoriaNorm.isEmpty()) {
            for (String palabraPreg : palabrasPregunta) {
                if (categoriaNorm.contains(palabraPreg) && palabraPreg.length() >= 4) {
                    score += 5;
                    System.out.println("[CHATBOT] +5 por match categoría: " + categoriaNorm);
                    break;
                }
            }
        }

        return score;
    }

    /**
     * Normaliza texto: minúsculas, sin acentos, sin caracteres especiales
     */
    private String normalizarTexto(String texto) {
        if (texto == null)
            return "";

        // A minúsculas
        String norm = texto.toLowerCase();

        // Remover acentos
        norm = java.text.Normalizer.normalize(norm, java.text.Normalizer.Form.NFD);
        norm = norm.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Remover caracteres especiales pero mantener espacios
        norm = norm.replaceAll("[^a-z0-9\\s]", " ");

        // Normalizar espacios múltiples
        norm = norm.replaceAll("\\s+", " ").trim();

        return norm;
    }

    public String responderPregunta(String pregunta) {
        String p = pregunta.toLowerCase();

        System.out.println("[CHATBOT] ====================================");
        System.out.println("[CHATBOT] Pregunta recibida: " + pregunta);
        System.out.println("[CHATBOT] Pregunta normalizada: " + p);

        // 0. Búsqueda Dinámica de Productos (PRIORIDAD)
        try {
            java.util.List<com.vesta.api.entity.Producto> productos = productoRepository.findAll();
            System.out.println("[CHATBOT] Total productos en BD: " + productos.size());

            // Lista para almacenar coincidencias con puntuación
            List<ProductoMatch> matches = new ArrayList<>();

            for (com.vesta.api.entity.Producto prod : productos) {
                System.out.println("[CHATBOT] Evaluando producto: " + prod.getNombre() +
                        " | Activo: " + prod.getActivo() +
                        " | Categoría: " + prod.getCategoria());

                if (prod.getActivo() != null && prod.getActivo()) {
                    int score = calcularScoreProducto(p, prod);
                    System.out.println("[CHATBOT] Score final para '" + prod.getNombre() + "': " + score);

                    if (score > 0) {
                        matches.add(new ProductoMatch(prod, score));
                    }
                }
            }

            // Ordenar por puntuación descendente
            matches.sort((a, b) -> Integer.compare(b.score, a.score));

            if (!matches.isEmpty() && matches.get(0).score >= 7) { // Umbral mínimo
                com.vesta.api.entity.Producto mejorProducto = matches.get(0).producto;
                System.out.println("[CHATBOT] ✓ PRODUCTO ENCONTRADO: " + mejorProducto.getNombre() +
                        " (Score: " + matches.get(0).score + ")");

                return String.format(
                        "🤖 VestaBot: ¡Sí! Tenemos el %s. %s. Puedes contratarlo desde %.2f€ al mes.",
                        mejorProducto.getNombre(),
                        mejorProducto.getDescripcion() != null ? mejorProducto.getDescripcion()
                                : "Un seguro excelente para ti",
                        mejorProducto.getPrecioBase());
            }

            System.out.println("[CHATBOT] No se encontró coincidencia dinámica (mejor score: " +
                    (matches.isEmpty() ? "0" : matches.get(0).score) + "), usando respuestas estáticas");
        } catch (Exception e) {
            // Si falla la BD, continuamos con las respuestas estáticas
            System.err.println("[CHATBOT ERROR] Error consultando productos: " + e.getMessage());
            e.printStackTrace();
        }

        // 1. Saludos
        if (p.contains("hola") || p.contains("buenos") || p.contains("hey")) {
            return "🤖 VestaBot: ¡Hola! Soy tu asistente virtual. Estoy aquí para ayudarte con dudas sobre nuestros seguros, coberturas o soporte. ¿Qué necesitas?";
        }

        // 1.5. Agradecimientos y Despedidas
        if (p.contains("gracias") || p.contains("agradecid")) {
            return "🤖 VestaBot: ¡De nada! Es un placer ayudarte. Si necesitas algo más, aquí estoy. 🛡️";
        }
        if (p.contains("adios") || p.contains("adiós") || p.contains("hasta luego") || p.contains("bye")) {
            return "🤖 VestaBot: ¡Hasta luego! Cuídate mucho. Recuerda que con Vesta, tu tranquilidad viaja contigo.";
        }

        // 1.8. Contacto (NUEVO)
        if (p.contains("contacto") || p.contains("teléfono") || p.contains("telefono") || p.contains("email")
                || p.contains("correo") || p.contains("donde estan")) {
            return "🤖 VestaBot: Puedes contactarnos por:\n" +
                    "📧 Email: javip200555@gmail.com\n" +
                    "📞 Teléfono: +34 622 645 922\n" +
                    "📍 Oficinas: C/ Francisco Arias, 22A, Lora del Río (Sevilla).\n" +
                    "¡Estaremos encantados de atenderte!";
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
            return "🤖 VestaBot: Nuestros seguros son muy económicos. ¡Consulta el catálogo para ver precios actualizados!";
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

        // 9. ELIMINADO - Movilidad/Bici/Patinete ahora se busca dinámicamente arriba
        // 10. Tecnología / Móvil (NUEVO)
        if (p.contains("movil") || p.contains("móvil") || p.contains("celular") || p.contains("tablet")
                || p.contains("tecnologia") || p.contains("tecnología") || p.contains("iphone")) {
            return "🤖 VestaBot: Protegemos tus gadgets (móviles, tablets, ordenadores) contra rotura de pantalla, líquidos y robo.";
        }

        // 11. Genérico 'Seguro' (NUEVO)
        if (p.contains("seguro") || p.contains("poliza") || p.contains("contratar")) {
            return "🤖 VestaBot: Tenemos seguros flexibles y personalizados. Consulta nuestro catálogo para ver todas las opciones disponibles. ¿Hay algo específico que te interese?";
        }

        // 12. Default
        return "🤖 VestaBot: Mmm... no estoy seguro de haber entendido eso. 🤔\n" +
                "Prueba a preguntarme por:\n" +
                "- El nombre de un seguro (ej: 'Patinete', 'Viaje', 'Mascota')\n" +
                "- 'Precios' o 'Tarifas'\n" +
                "- 'Contacto' o 'Teléfono'";
    }
}