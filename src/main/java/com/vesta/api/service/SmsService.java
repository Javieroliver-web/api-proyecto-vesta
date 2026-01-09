package com.vesta.api.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar SMS usando Twilio
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.messaging.service.sid}")
    private String messagingServiceSid;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    @PostConstruct
    public void init() {
        if (smsEnabled && accountSid != null && !accountSid.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                logger.info("✅ Twilio SMS service initialized successfully");
            } catch (Exception e) {
                logger.error("❌ Error initializing Twilio: {}", e.getMessage());
                smsEnabled = false;
            }
        } else {
            logger.warn("⚠️ SMS service is disabled or not configured");
        }
    }

    /**
     * Envía un SMS con el código de recuperación de contraseña
     *
     * @param toPhoneNumber Número de teléfono del destinatario (formato:
     *                      +34622645922)
     * @param code          Código de 6 dígitos
     * @param userName      Nombre del usuario
     */
    public void sendPasswordResetSms(String toPhoneNumber, String code, String userName) {
        if (!smsEnabled) {
            logger.warn("⚠️ SMS NO CONFIGURADO - Código para {}: {}", toPhoneNumber, code);
            return;
        }

        try {
            String messageBody = String.format(
                    "Hola %s,\n\n" +
                            "Tu código de recuperación de contraseña es: %s\n\n" +
                            "Este código expira en 5 minutos.\n\n" +
                            "Si no solicitaste este código, ignora este mensaje.\n\n" +
                            "- Equipo Vesta",
                    userName, code);

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    messagingServiceSid,
                    messageBody).create();

            logger.info("✅ SMS enviado exitosamente a {} - SID: {}", toPhoneNumber, message.getSid());

        } catch (Exception e) {
            logger.error("❌ Error al enviar SMS a {}: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("Error al enviar el SMS. Por favor, intenta con email.", e);
        }
    }

    /**
     * Verifica si el servicio de SMS está habilitado y configurado
     */
    public boolean isEnabled() {
        return smsEnabled;
    }
}
