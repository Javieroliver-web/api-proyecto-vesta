#!/bin/bash
# Script para configurar certificado SSL con Let's Encrypt
# Dominio: vesta-web2.duckdns.org
# Soporta ambas IPs: 34.175.116.7 y 34.175.81.8

echo "=== CONFIGURANDO CERTIFICADO SSL PARA VESTA-WEB2.DUCKDNS.ORG ==="

# Verificar si estamos ejecutando como root
if [ "$EUID" -ne 0 ]; then
    echo "❌ Este script debe ejecutarse como root"
    echo "   Usa: sudo bash setup_ssl_certificate.sh"
    exit 1
fi

# 1. Instalar Certbot si no está instalado
echo "1. Verificando/Instalando Certbot..."
if ! command -v certbot &> /dev/null; then
    echo "   📦 Instalando Certbot..."
    apt update
    apt install -y certbot python3-certbot-nginx
else
    echo "   ✅ Certbot ya está instalado"
fi

# 2. Verificar que Nginx esté instalado
echo "2. Verificando Nginx..."
if ! command -v nginx &> /dev/null; then
    echo "   📦 Instalando Nginx..."
    apt install -y nginx
else
    echo "   ✅ Nginx ya está instalado"
fi

# 3. Crear configuración temporal de Nginx para validación
echo "3. Creando configuración temporal para validación..."
cat > /etc/nginx/sites-available/vesta-temp << 'EOF'
server {
    listen 80;
    server_name vesta-web2.duckdns.org;
    
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}
EOF

# 4. Habilitar configuración temporal
echo "4. Habilitando configuración temporal..."
ln -sf /etc/nginx/sites-available/vesta-temp /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

# 5. Obtener certificado SSL
echo "5. Obteniendo certificado SSL de Let's Encrypt..."
certbot certonly \
    --nginx \
    --non-interactive \
    --agree-tos \
    --email javip200555@gmail.com \
    --domains vesta-web2.duckdns.org

# Verificar si el certificado se obtuvo correctamente
if [ $? -eq 0 ]; then
    echo "   ✅ Certificado SSL obtenido correctamente"
else
    echo "   ❌ Error obteniendo certificado SSL"
    echo "   Verifica que:"
    echo "   - El dominio vesta-web2.duckdns.org apunte a esta IP"
    echo "   - El puerto 80 esté abierto"
    echo "   - No haya firewall bloqueando"
    exit 1
fi

# 6. Copiar configuración HTTPS final
echo "6. Configurando Nginx con HTTPS..."
cp /home/vestaadmin/nginx_vesta_https.conf /etc/nginx/sites-available/vesta-https
ln -sf /etc/nginx/sites-available/vesta-https /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/vesta-temp

# 7. Verificar configuración y recargar
echo "7. Verificando configuración de Nginx..."
nginx -t

if [ $? -eq 0 ]; then
    echo "   ✅ Configuración de Nginx válida"
    systemctl reload nginx
    echo "   ✅ Nginx recargado"
else
    echo "   ❌ Error en configuración de Nginx"
    exit 1
fi

# 8. Configurar renovación automática
echo "8. Configurando renovación automática..."
(crontab -l 2>/dev/null; echo "0 12 * * * /usr/bin/certbot renew --quiet") | crontab -

# 9. Verificar estado final
echo "9. Verificando estado final..."
echo "   🔍 Estado de Nginx:"
systemctl status nginx --no-pager -l

echo "   🔍 Certificados instalados:"
certbot certificates

echo ""
echo "=== CONFIGURACIÓN SSL COMPLETADA ==="
echo ""
echo "🌐 URLs HTTPS disponibles:"
echo "   • https://vesta-web2.duckdns.org/vesta-web/"
echo "   • https://34.175.116.7/vesta-web/ (si certificado incluye IP)"
echo "   • https://34.175.81.8/vesta-web/ (si certificado incluye IP)"
echo ""
echo "🔧 Configuración aplicada:"
echo "   • Certificado SSL: /etc/letsencrypt/live/vesta-web2.duckdns.org/"
echo "   • Nginx config: /etc/nginx/sites-available/vesta-https"
echo "   • Renovación automática: Configurada (cron diario)"
echo ""
echo "⚠️  IMPORTANTE:"
echo "   • Actualiza las URLs en tu aplicación a HTTPS"
echo "   • Verifica que OAuth esté configurado para HTTPS"
echo "   • Prueba todas las funcionalidades"
echo ""