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

import java.time.LocalDate;
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

    // CREAR SINIESTRO (POST /api/siniestros)
    // Recibe: descripcion (texto), polizaId (long) y file (imagen)
    @PostMapping
    public ResponseEntity<?> reportarSiniestro(
            @RequestParam("polizaId") Long polizaId,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("file") MultipartFile file) {

        // 1. Validar Póliza
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RuntimeException("Póliza no encontrada"));

        // 2. Análisis de IA (Perito Digital)
        String nombreImagen = file.getOriginalFilename();
        String analisisIA = aiService.analizarImagen(nombreImagen);

        // 3. Análisis de Fraude
        Integer fraudeScore = fraudService.calcularRiesgo(poliza.getUsuario().getId(), descripcion);

        // 4. Guardar Siniestro
        Siniestro siniestro = new Siniestro();
        siniestro.setPoliza(poliza);
        siniestro.setDescripcion(descripcion);
        siniestro.setFecha(LocalDate.now());
        siniestro.setImagenUrl("uploads/" + nombreImagen); // Aquí guardarías el fichero real
        siniestro.setAnalisisIA(analisisIA);
        siniestro.setFraudeScore(fraudeScore);

        // Decisión automática basada en IA
        if (analisisIA.contains("APROBADO") && fraudeScore < 50) {
            siniestro.setEstado("APROBADO");
        } else {
            siniestro.setEstado("PENDIENTE_REVISION");
        }

        siniestroRepository.save(siniestro);

        return ResponseEntity.ok(Map.of(
            "mensaje", "Siniestro reportado con éxito",
            "estado", siniestro.getEstado(),
            "analisisIA", analisisIA
        ));
    }
}