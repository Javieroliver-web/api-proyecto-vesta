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
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificamos la contraseña usando el encoder
        // 'request.getPassword()' viene del DTO corregido con @JsonProperty("contrasena")
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getRol(), usuario.getNombreCompleto());
    }

    // Registro
    public AuthResponseDTO registrar(RegistroDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail()); // Viene de @JsonProperty("correoElectronico")
        usuario.setMovil(request.getMovil());
        
        // Asignar rol por defecto si viene nulo
        usuario.setRol(request.getTipoUsuario() != null ? request.getTipoUsuario() : "USUARIO");
        
        // IMPORTANTE: Aquí usamos getContrasena() porque así se llama en tu RegistroDTO
        // Y lo encriptamos antes de guardar
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        
        usuario.setEmailConfirmado(true); // Auto-confirmar para simplificar el prototipo

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getRol(), usuario.getNombreCompleto());
    }
}