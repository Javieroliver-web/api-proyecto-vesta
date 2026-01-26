# Script para configurar Insomnia con la API de Vesta
# Configuración más simple que Postman

Write-Host "🚀 Configurando Insomnia para la API de Vesta..." -ForegroundColor Green
Write-Host ""

# Verificar si Insomnia está instalado
$insomniaPath = Get-Command "insomnia" -ErrorAction SilentlyContinue
if (-not $insomniaPath) {
    Write-Host "📦 Insomnia no encontrado. Descárgalo desde:" -ForegroundColor Yellow
    Write-Host "https://insomnia.rest/download" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host "📋 Pasos para configurar Insomnia:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 📥 Importar colección:" -ForegroundColor Yellow
Write-Host "   - Abre Insomnia" -ForegroundColor White
Write-Host "   - Ve a Application → Preferences → Data" -ForegroundColor White
Write-Host "   - Haz clic en 'Import Data'" -ForegroundColor White
Write-Host "   - Selecciona el archivo: insomnia-collection.json" -ForegroundColor White
Write-Host ""
Write-Host "2. 🌍 Configurar entornos:" -ForegroundColor Yellow
Write-Host "   - Local Development: http://localhost:8080" -ForegroundColor White
Write-Host "   - Production: https://vesta-web.duckdns.org" -ForegroundColor White
Write-Host ""
Write-Host "3. 🔑 Configurar autenticación:" -ForegroundColor Yellow
Write-Host "   - Ejecuta el endpoint 'Login' primero" -ForegroundColor White
Write-Host "   - Copia el token de la respuesta" -ForegroundColor White
Write-Host "   - Pégalo en la variable 'auth_token' del entorno" -ForegroundColor White
Write-Host ""
Write-Host "4. ✅ Probar endpoints:" -ForegroundColor Yellow
Write-Host "   - Test Auth Endpoint (sin autenticación)" -ForegroundColor White
Write-Host "   - Login (obtener token)" -ForegroundColor White
Write-Host "   - Get All Products (con token)" -ForegroundColor White
Write-Host ""

# Verificar si el servidor está ejecutándose
Write-Host "🔍 Verificando si la API está ejecutándose..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "https://vesta-web.duckdns.org/vesta-web/api/auth/test" -Method GET -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✅ API funcionando en https://vesta-web.duckdns.org/vesta-web" -ForegroundColor Green
} catch {
    Write-Host "❌ API no está ejecutándose en https://vesta-web.duckdns.org/vesta-web" -ForegroundColor Red
    Write-Host "💡 Verificaciones:" -ForegroundColor Yellow
    Write-Host "   1. ¿Está el servidor desplegado correctamente?" -ForegroundColor White
    Write-Host "   2. ¿Está funcionando el dominio DuckDNS?" -ForegroundColor White
    Write-Host "   3. ¿Están los contenedores Docker ejecutándose?" -ForegroundColor White
    Write-Host ""
    Write-Host "🔄 Para desarrollo local también puedes usar:" -ForegroundColor Yellow
    Write-Host "   cd api-proyecto-vesta" -ForegroundColor White
    Write-Host "   mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
}

Write-Host "📝 Archivos creados:" -ForegroundColor Cyan
Write-Host "   - insomnia-collection.json (colección completa)" -ForegroundColor White
Write-Host "   - .kiro/hooks/api-postman-testing.kiro.hook (hook actualizado)" -ForegroundColor White
Write-Host ""
Write-Host "🎉 ¡Configuración lista! Importa la colección en Insomnia." -ForegroundColor Green