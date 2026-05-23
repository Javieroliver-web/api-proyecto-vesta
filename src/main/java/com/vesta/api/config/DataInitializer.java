package com.vesta.api.config;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void run(String... args) throws Exception {
        logger.info("🌱 INICIANDO CARGA DE DATOS DEMO...");

        // Crear Usuario Demo (si no existe)
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
                return usuarioRepository.save(u);
            });

        logger.info("🌱 CARGA DE DATOS COMPLETADA.");
    }
}