package com.vesta.api.service;

import com.vesta.api.dto.LoginDTO;         // Cambio Sprintix
import com.vesta.api.dto.AuthResponseDTO;  // Cambio Sprintix
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.util.JWTUtil;
import com.vesta.api.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public AuthResponseDTO login(LoginDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean match = PasswordUtil.verifyUserPassword(request.getPassword(), usuario.getPassword(), "");
        
        if (!match) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        
        // Devolvemos el DTO de respuesta estándar
        return new AuthResponseDTO(token, usuario.getRol(), usuario.getNombreCompleto());
    }
}