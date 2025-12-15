package com.vesta.api.service;

import com.vesta.api.dto.AuthResponseDTO;
import com.vesta.api.dto.LoginDTO;
import com.vesta.api.dto.RegistroDTO;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación
 * Maneja login y registro de usuarios
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Autenticar usuario
     * 
     * @param request Credenciales de login
     * @return Respuesta con token y datos del usuario
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginDTO request) {
        logger.debug("Intentando login para email: {}", request.getCorreoElectronico());

        Usuario usuario = usuarioRepository.findByEmail(request.getCorreoElectronico())
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado: {}", request.getCorreoElectronico());
                    return new RuntimeException("Usuario no encontrado con email: " + request.getCorreoElectronico());
                });

        // Verificamos la contraseña usando BCrypt
        boolean passwordMatch = passwordEncoder.matches(request.getContrasena(), usuario.getPassword());

        if (!passwordMatch) {
            logger.warn("Contraseña incorrecta para usuario: {}", request.getCorreoElectronico());
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        logger.info("Login exitoso para usuario: {} con rol: {}", usuario.getEmail(), usuario.getRol());

        return new AuthResponseDTO(
                token,
                usuario.getRol(),
                usuario.getNombreCompleto(),
                usuario.getId());
    }

    /**
     * Registrar nuevo usuario
     * 
     * @param request Datos del nuevo usuario
     * @return Respuesta con token y datos del usuario creado
     */
    @Transactional
    public AuthResponseDTO registrar(RegistroDTO request) {
        logger.debug("Intentando registrar usuario: {}", request.getCorreoElectronico());

        if (usuarioRepository.existsByEmail(request.getCorreoElectronico())) {
            logger.warn("Intento de registro con email ya existente: {}", request.getCorreoElectronico());
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getCorreoElectronico());
        usuario.setMovil(request.getMovil());
        // Asignar rol por defecto si viene nulo
        usuario.setRol(request.getTipoUsuario() != null ? request.getTipoUsuario() : "USUARIO");
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        usuario.setEmailConfirmado(true);

        // Guardamos para obtener el ID generado
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        logger.info("Usuario registrado exitosamente: {} con ID: {}", usuarioGuardado.getEmail(),
                usuarioGuardado.getId());

        String token = jwtUtil.generateToken(usuarioGuardado.getEmail(), usuarioGuardado.getRol());

        return new AuthResponseDTO(
                token,
                usuarioGuardado.getRol(),
                usuarioGuardado.getNombreCompleto(),
                usuarioGuardado.getId());
    }
}
