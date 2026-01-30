-- Script para crear usuario administrador directamente en la base de datos
-- Contraseña establecida: "password" (hash BCrypt)

INSERT INTO usuarios (
    usu_nombre_completo,
    usu_email,
    usu_movil,
    usu_password,
    usu_rol,
    usu_activo,
    usu_email_confirmado,
    usu_acepta_terminos,
    usu_acepta_privacidad,
    usu_ciudad,
    usu_pais,
    usu_tema,
    usu_fecha_creacion
) VALUES (
    'Admin Vesta',
    'warshadows22@gmail.com',
    '+34622645922',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', -- Contraseña: password
    'OWNER',
    true,
    true, -- Email confirmado para evitar problemas de login
    true, -- Acepta términos
    true, -- Acepta privacidad
    'Sevilla, ES',
    'España',
    'light',
    NOW()
);
