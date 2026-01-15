package com.vesta.api.controller;

import com.vesta.api.entity.Auditoria;
import com.vesta.api.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Auditoria>> obtenerLogs() {
        return ResponseEntity.ok(auditoriaService.obtenerUltimosLogs());
    }
}
