package com.vesta.api.service;

import com.vesta.api.entity.PasswordResetToken;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PasswordResetTokenRepository;
import com.vesta.api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Servicio para gestionar la recuperación de contraseñas
 */
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Genera un código de 6 dígitos aleatorio
     */
    private String generateSixDigitCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Genera y envía un token de recuperación de contraseña
     */
    @Transactional
    public void generateResetToken(String email) {
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUsuarioId(usuario.getId());

        // Generar nuevo token de 6 dígitos
        String token = generateSixDigitCode();

        // Crear y guardar el token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setUsado(false);

        tokenRepository.save(resetToken);

        // Enviar email con el código
        emailService.sendPasswordResetEmail(usuario.getEmail(), token, usuario.getNombreCompleto());

        logger.info("Token de recuperación generado para usuario: {}", email);
    }

    /**
     * Valida un token de recuperación
     */
    public boolean validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null) {
            logger.warn("Token no encontrado: {}", token);
            return false;
        }

        if (!resetToken.isValid()) {
            logger.warn("Token inválido o expirado: {}", token);
            return false;
        }

        return true;
    }

    /**
     * Resetea la contraseña usando un token válido
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Buscar y validar el token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("El código ha expirado o ya fue utilizado");
        }

        // Obtener el usuario
        Usuario usuario = resetToken.getUsuario();

        // Actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // Marcar el token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        logger.info("Contraseña reseteada exitosamente para usuario: {}", usuario.getEmail());
    }

    /**
     * Tarea programada para limpiar tokens expirados (se ejecuta diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM todos los días
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByFechaExpiracionBefore(now);
        logger.info("Tokens expirados eliminados");
    }
}
