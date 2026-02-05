package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    /**
     * Helper para obtener la IP real del cliente
     */
    private String getClientIp() {
        if (request == null)
            return "";
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
        }
        if (remoteAddr != null && !remoteAddr.isEmpty() && !"unknown".equalsIgnoreCase(remoteAddr)) {
            return remoteAddr.split(",")[0].trim();
        }
        remoteAddr = request.getHeader("X-Real-IP");
        if (remoteAddr != null && !remoteAddr.isEmpty() && !"unknown".equalsIgnoreCase(remoteAddr)) {
            return remoteAddr;
        }
        return request.getRemoteAddr();
    }

    @GetMapping
    public ResponseEntity<Page<Usuario>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(usuarioService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Usuario actualizado = usuarioService.updateUser(id, updates, getClientIp());
            return ResponseEntity.ok(actualizado);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/link-oauth")
    public ResponseEntity<?> linkOAuth(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            usuarioService.linkOAuth(id, payload, getClientIp());
            return ResponseEntity.ok(Map.of("message", "Cuenta vinculada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unlink-oauth")
    public ResponseEntity<?> unlinkOAuth(@PathVariable Long id) {
        try {
            usuarioService.unlinkOAuth(id, getClientIp());
            return ResponseEntity.ok(Map.of("message", "Cuenta desvinculada correctamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.deleteUser(id, getClientIp());
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}
