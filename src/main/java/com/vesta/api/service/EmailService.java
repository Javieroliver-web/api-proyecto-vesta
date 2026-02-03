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

    @Value("${app.api.url:http://localhost:8080}")
    private String apiUrl;

    private final org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, org.thymeleaf.spring6.SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
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
                            "Este código expirará en 5 minutos.\n\n" +
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

    public void sendAccountConfirmationEmail(String toEmail, String token, String nombre) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("¡Bienvenido a Vesta! Confirma tu cuenta");

            String link = frontendUrl + "/api/auth/confirm-account?token=" + token;

            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("nombre", nombre);
            context.setVariable("url", link);

            String htmlContent = templateEngine.process("email/confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email HTML de confirmación enviado a: {}", toEmail);

        } catch (Exception e) {
            logger.error("Error al enviar email de confirmación a {}: {}", toEmail, e.getMessage());
            // Log del token para desarrollo
            logger.warn("⚠️ EMAIL NO CONFIGURADO - Link de activación para {}: {}", toEmail,
                    frontendUrl + "/api/auth/confirm-account?token=" + token);
        }
    }

    public void sendAccountLockedEmail(String toEmail, String nombre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Alerta de Seguridad: Cuenta Bloqueada - Vesta");

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Hemos detectado múltiples intentos fallidos de inicio de sesión en tu cuenta.\n" +
                            "Por tu seguridad, hemos bloqueado temporalmente el acceso durante 15 minutos.\n\n" +
                            "Si has sido tú y has olvidado tu contraseña, puedes restablecerla aquí:\n" +
                            "%s/select-recovery-method\n\n" +
                            "Si no has sido tú, te recomendamos cambiar tu contraseña inmediatamente después de recuperar el acceso.\n\n"
                            +
                            "Saludos,\n" +
                            "El equipo de Seguridad de Vesta",
                    nombre,
                    frontendUrl); // Usando frontendUrl inyectada

            message.setText(emailBody);

            mailSender.send(message);
            logger.info("Alerta de bloqueo enviada a: {}", toEmail);

        } catch (Exception e) {
            logger.error("Error al enviar alerta de bloqueo a {}: {}", toEmail, e.getMessage());
        }
    }
}
