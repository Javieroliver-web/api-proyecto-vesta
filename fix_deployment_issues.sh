#!/bin/bash
# Script para corregir problemas identificados en el despliegue de Google Cloud
# IP: 34.175.116.7

echo "🔧 Iniciando corrección de problemas de despliegue..."

# 1. Crear usuario administrador
echo "📝 1. Creando usuario administrador..."
sudo -u postgres psql -d vesta_db <<EOF
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
) 
SELECT 
    'Admin Vesta',
    'warshadows22@gmail.com',
    '+34622645922',
    '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'ADMINISTRADOR',
    true,
    true,
    true,
    true,
    'Sevilla, ES',
    'España',
    'light',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE usu_email = 'warshadows22@gmail.com'
);
EOF

if [ $? -eq 0 ]; then
    echo "✅ Usuario administrador creado/verificado"
else
    echo "❌ Error al crear usuario administrador"
fi

# 2. Confirmar usuarios pendientes (opcional - descomentar si se desea)
# echo "📧 2. Confirmando usuarios pendientes..."
# sudo -u postgres psql -d vesta_db -c "UPDATE usuarios SET usu_email_confirmado = true WHERE usu_email_confirmado = false;"

# 3. Verificar estado
echo "📊 3. Verificando estado de usuarios..."
sudo -u postgres psql -d vesta_db -c "
SELECT 
    COUNT(*) as total,
    COUNT(CASE WHEN usu_rol = 'ADMINISTRADOR' THEN 1 END) as administradores,
    COUNT(CASE WHEN usu_email_confirmado = true THEN 1 END) as confirmados
FROM usuarios;
"

echo "✅ Correcciones aplicadas. Por favor, reinicia Tomcat si es necesario:"
echo "   sudo systemctl restart tomcat"
