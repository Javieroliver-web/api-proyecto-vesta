package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Importante
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Importante

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CAMBIO: Ahora recibimos un Map<String, Object> para mayor flexibilidad
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // 1. Validar Cambio de Contraseña (Si se solicita)
                    if (updates.containsKey("newPassword")) {
                        String currentPassword = (String) updates.get("currentPassword");
                        String newPassword = (String) updates.get("newPassword");

                        // Verificar que enviaron la contraseña actual
                        if (currentPassword == null || currentPassword.isEmpty()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message", "Debes ingresar tu contraseña actual."));
                        }

                        // Verificar que la contraseña actual sea correcta
                        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message", "La contraseña actual es incorrecta."));
                        }

                        // Si todo es correcto, encriptar y asignar la nueva
                        usuario.setPassword(passwordEncoder.encode(newPassword));
                    }

                    // 2. Actualizar otros campos (Opcional)
                    if (updates.containsKey("nombreCompleto")) {
                        usuario.setNombreCompleto((String) updates.get("nombreCompleto"));
                    }
                    if (updates.containsKey("movil")) {
                        usuario.setMovil((String) updates.get("movil"));
                    }
                    if (updates.containsKey("rol")) { // Solo admin debería poder, pero por ahora lo dejamos
                        usuario.setRol((String) updates.get("rol"));
                    }
                    if (updates.containsKey("email")) {
                        usuario.setEmail((String) updates.get("email"));
                    }

                    Usuario actualizado = usuarioRepository.save(usuario);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}