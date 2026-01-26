#!/bin/bash
# Script para desplegar con configuración HTTPS
# Dominio: vesta-web2.duckdns.org
# Soporta ambas IPs: 34.175.116.7 y 34.175.81.8

echo "=== DESPLEGANDO VESTA CON HTTPS ==="

# Detectar IP actual del servidor
CURRENT_IP="34.175.81.8"  # IP por defecto
BACKUP_IP="34.175.116.7"  # IP alternativa

echo "1. Detectando IP del servidor..."
if ping -c 1 34.175.81.8 &> /dev/null; then
    CURRENT_IP="34.175.81.8"
    echo "   ✅ Usando IP principal: $CURRENT_IP"
elif ping -c 1 34.175.116.7 &> /dev/null; then
    CURRENT_IP="34.175.116.7"
    echo "   ✅ Usando IP alternativa: $CURRENT_IP"
else
    echo "   ❌ No se puede conectar a ninguna IP"
    exit 1
fi

# Subir archivos de configuración HTTPS
echo "2. Subiendo configuración HTTPS..."
scp -i vesta_key nginx_vesta_https.conf vestaadmin@$CURRENT_IP:~/
scp -i vesta_key setup_ssl_certificate.sh vestaadmin@$CURRENT_IP:~/

# Subir WAR actualizados
echo "3. Subiendo WAR actualizados..."
scp -i vesta_key target/vesta-api.war vestaadmin@$CURRENT_IP:~/
scp -i vesta_key ../web-proyecto-vesta/target/vesta-web.war vestaadmin@$CURRENT_IP:~/

# Conectar al servidor y configurar HTTPS
echo "4. Configurando HTTPS en servidor..."
ssh -i vesta_key vestaadmin@$CURRENT_IP << 'EOF'
    echo "=== CONFIGURANDO HTTPS EN SERVIDOR ==="
    
    # Hacer ejecutable el script SSL
    chmod +x setup_ssl_certificate.sh
    
    # Parar servicios
    sudo systemctl stop tomcat
    sudo systemctl stop nginx
    
    # Backup de configuraciones actuales
    sudo cp /etc/nginx/sites-available/vesta /etc/nginx/sites-available/vesta.backup-$(date +%Y%m%d-%H%M) 2>/dev/null || true
    
    # Backup de WAR actuales
    sudo cp /opt/tomcat/webapps/vesta-api.war /opt/tomcat/webapps/vesta-api.war.backup-$(date +%Y%m%d-%H%M)
    sudo cp /opt/tomcat/webapps/vesta-web.war /opt/tomcat/webapps/vesta-web.war.backup-$(date +%Y%m%d-%H%M)
    
    # Copiar nuevos WAR
    sudo cp ~/vesta-api.war /opt/tomcat/webapps/
    sudo cp ~/vesta-web.war /opt/tomcat/webapps/
    
    # Cambiar permisos
    sudo chown tomcat:tomcat /opt/tomcat/webapps/vesta-*.war
    
    # Limpiar directorios desplegados
    sudo rm -rf /opt/tomcat/webapps/vesta-api
    sudo rm -rf /opt/tomcat/webapps/vesta-web
    
    # Configurar SSL (ejecutar como root)
    echo "Configurando certificado SSL..."
    sudo bash setup_ssl_certificate.sh
    
    # Actualizar variables de entorno para HTTPS
    sudo sed -i 's|API_URL=.*|API_URL=https://vesta-web2.duckdns.org/vesta-api/api|' /opt/tomcat/bin/setenv.sh
    sudo sed -i 's|FRONTEND_URL=.*|FRONTEND_URL=https://vesta-web2.duckdns.org/vesta-web|' /opt/tomcat/bin/setenv.sh
    sudo sed -i 's|APP_BASE_URL=.*|APP_BASE_URL=https://vesta-web2.duckdns.org|' /opt/tomcat/bin/setenv.sh
    sudo sed -i 's|APP_FRONTEND_URL=.*|APP_FRONTEND_URL=https://vesta-web2.duckdns.org/vesta-web|' /opt/tomcat/bin/setenv.sh
    
    # Iniciar servicios
    sudo systemctl start tomcat
    
    # Verificar estado
    sleep 15
    echo "Estado de servicios:"
    sudo systemctl status nginx --no-pager -l
    sudo systemctl status tomcat --no-pager -l
EOF

echo "5. Verificando despliegue HTTPS..."
echo "   🌐 HTTPS: https://vesta-web2.duckdns.org/vesta-web/"
echo "   🌐 HTTP (redirect): http://vesta-web2.duckdns.org/vesta-web/"
echo "   🌐 IP 1: https://34.175.116.7/vesta-web/"
echo "   🌐 IP 2: https://34.175.81.8/vesta-web/"

echo ""
echo "=== DESPLIEGUE HTTPS COMPLETADO ==="
echo ""
echo "🔒 HTTPS configurado correctamente"
echo "📋 Próximos pasos:"
echo "   1. Verificar que https://vesta-web2.duckdns.org/vesta-web/ funciona"
echo "   2. Probar login y funcionalidades"
echo "   3. Verificar que OAuth funciona con HTTPS"
echo "   4. Actualizar enlaces en documentación"