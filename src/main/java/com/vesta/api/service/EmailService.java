package com.vesta.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de correos electrónicos
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@vesta.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un email con el código de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String toEmail, String token, String nombreUsuario) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña - Vesta");

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Has solicitado recuperar tu contraseña en Vesta.\n\n" +
                            "Tu código de verificación es: %s\n\n" +
                            "Este código expirará en 1 hora.\n\n" +
                            "Si no solicitaste este cambio, ignora este mensaje.\n\n" +
                            "Saludos,\n" +
                            "El equipo de Vesta",
                    nombreUsuario,
                    token);

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("Email de recuperación enviado a: {}", toEmail);

        } catch (Exception e) {
            logger.error("Error al enviar email a {}: {}", toEmail, e.getMessage());
            // En desarrollo, mostramos el código en los logs
            logger.warn("⚠️ EMAIL NO CONFIGURADO - Código de recuperación para {}: {}", toEmail, token);
            throw new RuntimeException("Error al enviar el email. Por favor, contacta al administrador.");
        }
    }
}
