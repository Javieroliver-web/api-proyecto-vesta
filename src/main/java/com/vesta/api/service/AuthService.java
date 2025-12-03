package com.vesta.api.service;

import com.vesta.api.dto.AuthResponseDTO;
import com.vesta.api.dto.LoginDTO;
import com.vesta.api.dto.RegistroDTO;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Login
    public AuthResponseDTO login(LoginDTO request) {
        // DEBUG: Ver qué llega
        System.out.println("🔍 DEBUG - Email recibido: " + request.getEmail());
        System.out.println("🔍 DEBUG - Password recibido: " + (request.getPassword() != null ? "***" : "NULL"));
        
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + request.getEmail()));

        System.out.println("🔍 DEBUG - Usuario encontrado: " + usuario.getNombreCompleto());
        System.out.println("🔍 DEBUG - Hash en BD: " + usuario.getPassword().substring(0, 20) + "...");
        
        // Verificamos la contraseña usando BCrypt
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        System.out.println("🔍 DEBUG - Password match: " + passwordMatch);
        
        if (!passwordMatch) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        System.out.println("✅ DEBUG - Login exitoso, token generado");
        
        return new AuthResponseDTO(token, usuario.getRol(), usuario.getNombreCompleto());
    }

    // Registro
    public AuthResponseDTO registrar(RegistroDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setMovil(request.getMovil());
        usuario.setRol(request.getTipoUsuario() != null ? request.getTipoUsuario() : "USUARIO");
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        usuario.setEmailConfirmado(true);

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getRol(), usuario.getNombreCompleto());
    }
}