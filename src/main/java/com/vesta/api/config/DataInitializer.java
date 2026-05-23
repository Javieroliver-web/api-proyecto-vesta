package com.vesta.api.config;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Credenciales admin via variable de entorno (no expuestas en el repositorio)
    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        logger.info("🌱 INICIANDO CARGA DE DATOS DEMO...");

        // === USUARIO ADMINISTRADOR (solo si están configuradas las variables de entorno) ===
        if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
            usuarioRepository.findByEmail(adminEmail)
                .map(existingUser -> {
                    // Siempre actualizar rol y contraseña desde las variables de entorno
                    existingUser.setRol("ADMINISTRADOR");
                    existingUser.setPassword(passwordEncoder.encode(adminPassword));
                    existingUser.setEmailConfirmado(true);
                    existingUser.setActivo(true);
                    logger.info("✅ Usuario administrador actualizado.");
                    return usuarioRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    Usuario admin = new Usuario();
                    admin.setNombreCompleto("Administrador");
                    admin.setEmail(adminEmail);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRol("ADMINISTRADOR");
                    admin.setMovil("+34000000000");
                    admin.setEmailConfirmado(true);
                    admin.setActivo(true);
                    admin.setAceptaTerminos(true);
                    admin.setAceptaPrivacidad(true);
                    logger.info("✅ Usuario administrador creado.");
                    return usuarioRepository.save(admin);
                });
        } else {
            logger.warn("⚠️ Variables APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD no configuradas. Admin no creado.");
        }

        // === USUARIO DEMO ===
        usuarioRepository.findByEmail("demo@vesta.com")
            .map(existingUser -> {
                if (!Boolean.TRUE.equals(existingUser.getEmailConfirmado())) {
                    existingUser.setEmailConfirmado(true);
                    return usuarioRepository.save(existingUser);
                }
                return existingUser;
            })
            .orElseGet(() -> {
                Usuario u = new Usuario();
                u.setNombreCompleto("Usuario Demo");
                u.setEmail("demo@vesta.com");
                u.setPassword(passwordEncoder.encode("123456"));
                u.setRol("USUARIO");
                u.setMovil("+34600000000");
                u.setEmailConfirmado(true);
                u.setActivo(true);
                u.setAceptaTerminos(true);
                u.setAceptaPrivacidad(true);
                logger.info("✅ Usuario demo creado: demo@vesta.com");
                return usuarioRepository.save(u);
            });

        logger.info("🌱 CARGA DE DATOS COMPLETADA.");
    }
}