#!/bin/bash
# Script para verificar el estado del servidor en ambas IPs

echo "=== VERIFICANDO ESTADO DEL SERVIDOR VESTA ==="

# IPs a verificar
IP1="34.175.116.7"
IP2="34.175.81.8"
DOMAIN="vesta-web.duckdns.org"

echo "📡 Verificando conectividad..."

# Función para verificar una IP
check_ip() {
    local ip=$1
    local name=$2
    
    echo "🔍 Verificando $name ($ip):"
    
    # Ping
    if ping -c 1 $ip &> /dev/null; then
        echo "   ✅ Ping: OK"
        
        # HTTP
        if curl -s --connect-timeout 5 "http://$ip/health" &> /dev/null; then
            echo "   ✅ HTTP: OK"
        else
            echo "   ⚠️  HTTP: No responde"
        fi
        
        # Aplicación Web
        if curl -s --connect-timeout 5 "http://$ip/vesta-web/" | grep -q "Vesta" 2>/dev/null; then
            echo "   ✅ App Web: OK"
        else
            echo "   ⚠️  App Web: No responde"
        fi
        
        # API
        if curl -s --connect-timeout 5 "http://$ip/api/health" &> /dev/null; then
            echo "   ✅ API: OK"
        else
            echo "   ⚠️  API: No responde"
        fi
        
    else
        echo "   ❌ Ping: FALLO"
    fi
    echo ""
}

# Verificar ambas IPs
check_ip $IP1 "IP Principal"
check_ip $IP2 "IP Secundaria"

# Verificar dominio
echo "🌐 Verificando dominio ($DOMAIN):"
if curl -s --connect-timeout 5 "http://$DOMAIN/vesta-web/" | grep -q "Vesta" 2>/dev/null; then
    echo "   ✅ Dominio: OK"
else
    echo "   ⚠️  Dominio: No responde"
fi

echo ""
echo "🔗 URLs de acceso disponibles:"
echo "   • http://vesta-web.duckdns.org/vesta-web/ (Recomendado)"
echo "   • http://$IP1/vesta-web/"
echo "   • http://$IP2/vesta-web/"

echo ""
echo "=== VERIFICACIÓN COMPLETADA ==="