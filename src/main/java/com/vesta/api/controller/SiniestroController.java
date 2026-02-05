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
    public ResponseEntity<org.springframework.data.domain.Page<Siniestro>> listarSiniestros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado) {

        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);

        // Búsqueda combinada: Search y/o Estado
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasEstado = estado != null && !estado.trim().isEmpty();

        if (hasSearch && hasEstado) {
            return ResponseEntity.ok(siniestroRepository
                    .findByEstadoAndDescripcionContainingIgnoreCase(estado, search, pageable));
        } else if (hasSearch) {
            return ResponseEntity.ok(siniestroRepository
                    .findByDescripcionContainingIgnoreCaseOrEstadoContainingIgnoreCase(search, search, pageable));
        } else if (hasEstado) {
            return ResponseEntity.ok(siniestroRepository.findByEstado(estado, pageable));
        }

        return ResponseEntity.ok(siniestroRepository.findAll(pageable));
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

            // Definir ruta en directorio temporal del sistema para evitar problemas de
            // permisos
            String tempDir = System.getProperty("java.io.tmpdir");
            Path uploadDir = Paths.get(tempDir, "vesta_uploads").toAbsolutePath();

            // Crear carpeta si no existe
            try {
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    logger.debug("📁 Directorio uploads creado: {}", uploadDir);
                }
            } catch (IOException e) {
                logger.error("❌ Error al crear directorio uploads en: " + uploadDir, e);
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