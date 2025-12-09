package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder; // Importante si permites cambiar contraseña

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Para encriptar si cambian la contraseña

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

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario detalles) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // LÓGICA DE ACTUALIZACIÓN PARCIAL ("PATCH" style)
                    // Solo actualizamos si el campo viene en el JSON (no es null)

                    if (detalles.getNombreCompleto() != null) {
                        usuario.setNombreCompleto(detalles.getNombreCompleto());
                    }
                    
                    if (detalles.getEmail() != null) {
                        usuario.setEmail(detalles.getEmail());
                    }

                    if (detalles.getMovil() != null) {
                        usuario.setMovil(detalles.getMovil());
                    }

                    if (detalles.getRol() != null) {
                        usuario.setRol(detalles.getRol());
                    }

                    // Si también quisieras permitir cambiar contraseña aquí:
                    if (detalles.getPassword() != null && !detalles.getPassword().isEmpty()) {
                        usuario.setPassword(passwordEncoder.encode(detalles.getPassword()));
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