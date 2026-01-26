#!/bin/bash
# Script para conectar al servidor con soporte multi-IP

echo "=== CONECTANDO AL SERVIDOR VESTA ==="

# IPs disponibles
IP1="34.175.116.7"
IP2="34.175.81.8"

echo "Probando conexión a ambas IPs..."

# Probar IP1
echo "1. Probando $IP1..."
if ping -c 1 $IP1 &> /dev/null; then
    echo "   ✅ $IP1 responde"
    AVAILABLE_IP1=true
else
    echo "   ❌ $IP1 no responde"
    AVAILABLE_IP1=false
fi

# Probar IP2
echo "2. Probando $IP2..."
if ping -c 1 $IP2 &> /dev/null; then
    echo "   ✅ $IP2 responde"
    AVAILABLE_IP2=true
else
    echo "   ❌ $IP2 no responde"
    AVAILABLE_IP2=false
fi

# Seleccionar IP para conectar
if [ "$AVAILABLE_IP2" = true ]; then
    CONNECT_IP=$IP2
    echo "3. Conectando a IP principal: $CONNECT_IP"
elif [ "$AVAILABLE_IP1" = true ]; then
    CONNECT_IP=$IP1
    echo "3. Conectando a IP alternativa: $CONNECT_IP"
else
    echo "❌ No se puede conectar a ninguna IP"
    exit 1
fi

# Conectar
echo "4. Estableciendo conexión SSH..."
ssh -i vesta_key -o StrictHostKeyChecking=no vestaadmin@$CONNECT_IP

echo "=== CONEXIÓN TERMINADA ==="