#!/bin/bash

echo "=== Probando flujo completo del dashboard ==="

# 1. Hacer login y guardar cookies
echo "1. Haciendo login..."
curl -s -c /tmp/session_cookies.txt \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@vesta.com","password":"123456"}' \
  http://localhost:8080/vesta-web/login > /tmp/login_response.txt

echo "Respuesta del login:"
cat /tmp/login_response.txt
echo ""

# 2. Verificar cookies
echo "2. Cookies guardadas:"
cat /tmp/session_cookies.txt
echo ""

# 3. Probar acceso al dashboard
echo "3. Accediendo al dashboard..."
curl -s -b /tmp/session_cookies.txt \
  -w "Dashboard Status: %{http_code}\n" \
  -o /dev/null \
  http://localhost:8080/vesta-web/cliente/dashboard

# 4. Probar API de pólizas con cookies
echo "4. Probando API de pólizas con cookies de sesión..."
curl -s -b /tmp/session_cookies.txt \
  -w "Polizas API Status: %{http_code}\n" \
  http://localhost:8080/vesta-api/api/polizas/usuario

echo ""
echo "=== Fin de pruebas ==="