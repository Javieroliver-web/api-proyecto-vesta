package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. Obtener un usuario por ID (NUEVO)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Actualizar usuario (NUEVO - Endpoint PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario detallesUsuario) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Actualizamos los campos básicos
                    usuario.setNombreCompleto(detallesUsuario.getNombreCompleto());
                    usuario.setMovil(detallesUsuario.getMovil());
                    usuario.setEmail(detallesUsuario.getEmail());
                    
                    // Actualizamos el rol solo si se envía
                    if (detallesUsuario.getRol() != null) {
                        usuario.setRol(detallesUsuario.getRol());
                    }
                    
                    // Guardamos los cambios
                    Usuario usuarioActualizado = usuarioRepository.save(usuario);
                    return ResponseEntity.ok(usuarioActualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 4. Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}