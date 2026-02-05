package com.vesta.api.controller;

import com.vesta.api.entity.Auditoria;
import com.vesta.api.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auditoria")

public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<Auditoria>> obtenerLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search) {

        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);

        return ResponseEntity.ok(auditoriaService.obtenerLogsPaginados(pageable, search));
    }

    @PostMapping("/exportar")
    public ResponseEntity<String> exportarLogs(@RequestBody java.util.Map<String, String> payload) {
        String email = payload.get("email");
        String content = auditoriaService.exportarLogsUsuario(email);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"logs_" + email + ".txt\"")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(content);
    }
}
