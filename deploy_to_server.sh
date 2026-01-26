#!/bin/bash
# Script para desplegar WAR actualizados al servidor
# Soporta ambas IPs: 34.175.116.7 y 34.175.81.8

echo "=== DESPLEGANDO WARS ACTUALIZADOS (MULTI-IP SUPPORT) ==="

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

# Subir WAR de API
echo "2. Subiendo vesta-api.war a $CURRENT_IP..."
scp -i vesta_key target/vesta-api.war vestaadmin@$CURRENT_IP:~/

# Subir WAR de Web
echo "3. Subiendo vesta-web.war a $CURRENT_IP..."
scp -i vesta_key ../web-proyecto-vesta/target/vesta-web.war vestaadmin@$CURRENT_IP:~/

# Conectar al servidor y desplegar
echo "4. Desplegando en servidor $CURRENT_IP..."
ssh -i vesta_key vestaadmin@$CURRENT_IP << 'EOF'
    # Parar Tomcat
    sudo systemctl stop tomcat
    
    # Hacer backup de WAR actuales
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
    
    # Iniciar Tomcat
    sudo systemctl start tomcat
    
    # Verificar estado
    sleep 10
    sudo systemctl status tomcat
EOF

echo "5. Verificando acceso por ambas IPs..."
echo "   🌐 Dominio: http://vesta-web.duckdns.org/vesta-web/"
echo "   🌐 IP 1: http://34.175.116.7/vesta-web/"
echo "   🌐 IP 2: http://34.175.81.8/vesta-web/"

echo "=== DESPLIEGUE COMPLETADO ==="