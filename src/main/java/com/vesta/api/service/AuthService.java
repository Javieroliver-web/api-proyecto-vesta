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
        // DEBUG: Ver qué llega (útil para desarrollo)
        System.out.println("🔍 DEBUG - Email recibido: " + request.getEmail());
        
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + request.getEmail()));

        // Verificamos la contraseña usando BCrypt
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        
        if (!passwordMatch) {
            System.out.println("❌ DEBUG - Contraseña incorrecta para: " + request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        System.out.println("✅ DEBUG - Login exitoso, token generado");
        
        // CORRECCIÓN: Ahora devolvemos también el ID del usuario
        return new AuthResponseDTO(
            token, 
            usuario.getRol(), 
            usuario.getNombreCompleto(),
            usuario.getId() // <--- CAMBIO CLAVE AQUÍ
        );
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
        // Asignar rol por defecto si viene nulo
        usuario.setRol(request.getTipoUsuario() != null ? request.getTipoUsuario() : "USUARIO");
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        usuario.setEmailConfirmado(true);

        // Guardamos para obtener el ID generado
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuarioGuardado.getEmail(), usuarioGuardado.getRol());
        
        // CORRECCIÓN: Devolvemos el ID recién creado
        return new AuthResponseDTO(
            token, 
            usuarioGuardado.getRol(), 
            usuarioGuardado.getNombreCompleto(),
            usuarioGuardado.getId() // <--- CAMBIO CLAVE AQUÍ
        );
    }
}