package com.vesta.api.service;

// Imports corregidos según tus archivos actuales
import com.vesta.api.dtos.LoginRequest;
import com.vesta.api.dtos.LoginResponse; // Asegúrate de tener esta clase en el paquete dtos
import com.vesta.api.models.Usuario;     // Corregido de .entity a .models
import com.vesta.api.repositories.UsuarioRepository; // Corregido de .repository a .repositories
import com.vesta.api.util.JWTUtil;
import com.vesta.api.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o credenciales inválidas."));

        boolean passwordMatch = PasswordUtil.verifyUserPassword(
                request.getPassword(),
                usuario.getPassword(),
                "" 
        );

        if (!passwordMatch) {
            throw new RuntimeException("Credenciales inválidas.");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());

        return new LoginResponse(token, usuario.getRol(), usuario.getNombreCompleto());
    }

    public String encriptarPassword(String passwordPlana) {
        String salt = PasswordUtil.getSalt(30);
        String hash = PasswordUtil.generateSecurePassword(passwordPlana, salt);
        return salt + ":" + hash;
    }
}