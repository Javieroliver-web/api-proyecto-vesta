package com.vesta.api.controller;

import com.vesta.api.service.GamificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gamificacion")
public class GamificacionController {

    @Autowired
    private GamificacionService gamificacionService;

    @GetMapping("/progreso/{usuarioId}")
    public ResponseEntity<?> getProgreso(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(gamificacionService.obtenerProgreso(usuarioId));
    }
    
    @PostMapping("/verificar/{usuarioId}")
    public ResponseEntity<?> verificarLogros(@PathVariable Long usuarioId) {
        gamificacionService.verificarLogros(usuarioId);
        return ResponseEntity.ok(Map.of("message", "Logros verificados"));
    }
}