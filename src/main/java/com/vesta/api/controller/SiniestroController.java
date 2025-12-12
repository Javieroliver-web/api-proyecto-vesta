package com.vesta.api.controller;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Siniestro;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.SiniestroRepository;
import com.vesta.api.service.AIService;
import com.vesta.api.service.FraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // Importar
import java.nio.file.Files; // Importar
import java.nio.file.Path; // Importar
import java.nio.file.Paths; // Importar
import java.nio.file.StandardCopyOption; // Importar
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/siniestros")
public class SiniestroController {

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

            // 2. GUARDAR ARCHIVO FÍSICAMENTE (NUEVO)
            String nombreArchivo = file.getOriginalFilename();
            // Definir ruta relativa "uploads/"
            Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();
            // Crear carpeta si no existe
            Files.createDirectories(rutaArchivo.getParent());
            // Guardar el archivo (sobrescribir si existe)
            Files.copy(file.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

            // 3. Análisis
            String analisisIA = aiService.analizarImagen(nombreArchivo);
            Integer fraudeScore = fraudService.calcularRiesgo(poliza.getUsuario().getId(), descripcion);

            // 4. Guardar Entidad en BD
            Siniestro siniestro = new Siniestro();
            siniestro.setPoliza(poliza);
            siniestro.setDescripcion(descripcion);
            siniestro.setFecha(LocalDate.now());
            // Guardamos la ruta relativa para acceder vía URL luego
            siniestro.setImagenUrl("uploads/" + nombreArchivo); 
            siniestro.setAnalisisIA(analisisIA);
            siniestro.setFraudeScore(fraudeScore);

            if (analisisIA.contains("APROBADO") && fraudeScore < 20) {
                siniestro.setEstado("APROBADO");
            } else {
                siniestro.setEstado("PENDIENTE_REVISION");
            }

            siniestroRepository.save(siniestro);

            return ResponseEntity.ok(Map.of("mensaje", "Siniestro reportado con éxito"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar la imagen"));
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