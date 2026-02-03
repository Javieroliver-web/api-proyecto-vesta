package com.vesta.api.controller;

import com.vesta.api.entity.Auditoria;
import com.vesta.api.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")

public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Auditoria>> obtenerLogs() {
        return ResponseEntity.ok(auditoriaService.obtenerUltimosLogs());
    }

    @GetMapping("/exportar/{email}")
    public ResponseEntity<String> exportarLogs(@PathVariable String email) {
        String content = auditoriaService.exportarLogsUsuario(email);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"logs_" + email + ".txt\"")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(content);
    }
}
