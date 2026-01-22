#!/bin/bash

echo "=== Probando flujo público del dashboard ==="

# 1. Hacer login a través del dominio público
echo "1. Haciendo login público..."
curl -s -c /tmp/public_cookies.txt \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@vesta.com","password":"123456"}' \
  http://vesta-web.duckdns.org/vesta-web/login > /tmp/public_login_response.txt

echo "Respuesta del login:"
cat /tmp/public_login_response.txt
echo ""

# 2. Verificar cookies
echo "2. Cookies guardadas:"
cat /tmp/public_cookies.txt
echo ""

# 3. Probar API de pólizas a través de nginx
echo "3. Probando API de pólizas a través de nginx..."
curl -s -b /tmp/public_cookies.txt \
  -w "Polizas API Status: %{http_code}\n" \
  http://vesta-web.duckdns.org/api/polizas/usuario

echo ""
echo "=== Fin de pruebas públicas ==="