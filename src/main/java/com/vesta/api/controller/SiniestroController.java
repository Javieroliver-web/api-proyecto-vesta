package com.vesta.api.controller;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Siniestro;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.SiniestroRepository;
import com.vesta.api.service.AIService;
import com.vesta.api.service.FraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // Importar
import java.nio.file.Files; // Importar
import java.nio.file.Path; // Importar
import java.nio.file.Paths; // Importar
import java.nio.file.StandardCopyOption; // Importar
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/siniestros")
public class SiniestroController {

    private static final Logger logger = LoggerFactory.getLogger(SiniestroController.class);

    @Autowired
    private SiniestroRepository siniestroRepository;
    @Autowired
    private PolizaRepository polizaRepository;
    @Autowired
    private AIService aiService;
    @Autowired
    private FraudService fraudService;

    @GetMapping
    public ResponseEntity<List<Siniestro>> listarSiniestros() {
        return ResponseEntity.ok(siniestroRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> reportarSiniestro(
            @RequestParam("polizaId") Long polizaId,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. Validar Póliza
            Poliza poliza = polizaRepository.findById(polizaId)
                    .orElseThrow(() -> new RuntimeException("Póliza no encontrada"));

            // 2. GUARDAR ARCHIVO FÍSICAMENTE
            String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Definir ruta relativa "uploads/" en el directorio de trabajo actual
            Path uploadDir = Paths.get("uploads").toAbsolutePath();

            // Crear carpeta si no existe
            try {
                Files.createDirectories(uploadDir);
                logger.debug("📁 Directorio uploads creado/verificado: {}", uploadDir);
            } catch (IOException e) {
                logger.error("❌ Error al crear directorio uploads", e);
                throw new RuntimeException("No se pudo crear el directorio de uploads");
            }

            Path rutaArchivo = uploadDir.resolve(nombreArchivo);

            // Guardar el archivo
            try {
                Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
                logger.info("💾 Archivo guardado exitosamente: {}", rutaArchivo);
            } catch (IOException e) {
                logger.error("❌ Error al guardar archivo", e);
                throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
            }

            // 3. Análisis
            String analisisIA = aiService.analizarImagen(nombreArchivo);

            // Detectar si la IA lo aprobó (esto depende de lo que retorne AIService)
            boolean validadoPorIA = analisisIA.contains("APROBADO") || analisisIA.contains("DETECTA: Daños");

            // Pasar la validación de la IA al servicio de fraude para que ajuste el score
            Integer fraudeScore = fraudService.calcularRiesgo(poliza.getUsuario().getId(), descripcion, validadoPorIA);

            // 4. Guardar Entidad en BD
            Siniestro siniestro = new Siniestro();
            siniestro.setPoliza(poliza);
            siniestro.setDescripcion(descripcion);
            siniestro.setFecha(LocalDate.now());
            siniestro.setImagenUrl("uploads/" + nombreArchivo);
            siniestro.setAnalisisIA(analisisIA);
            siniestro.setFraudeScore(fraudeScore);

            if (analisisIA.contains("APROBADO") && fraudeScore < 20) {
                siniestro.setEstado("APROBADO");
            } else {
                siniestro.setEstado("PENDIENTE_REVISION");
            }

            siniestroRepository.save(siniestro);

            logger.info("✅ Siniestro guardado exitosamente con ID: {}", siniestro.getId());

            // Devolver respuesta completa con todos los datos que espera el frontend
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Siniestro reportado con éxito");
            response.put("analisisIA", analisisIA);
            response.put("fraudeScore", fraudeScore);
            response.put("estado", siniestro.getEstado());
            response.put("siniestroId", siniestro.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error general en reportarSiniestro", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al procesar el siniestro: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        return siniestroRepository.findById(id)
                .map(siniestro -> {
                    siniestro.setEstado(nuevoEstado);
                    siniestroRepository.save(siniestro);
                    return ResponseEntity.ok(Map.of("message", "Estado actualizado"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}