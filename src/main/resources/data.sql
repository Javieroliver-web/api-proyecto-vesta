-- data.sql: Se ejecuta automáticamente al iniciar la aplicación si Spring Boot lo detecta
-- Insertar usuario administrador si no existe

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
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', -- password
    'ADMINISTRADOR',
    true,
    true,
    true,
    true,
    'Sevilla, ES',
    'España',
    'light',
    NOW()
) ON CONFLICT (usu_email) DO NOTHING;
