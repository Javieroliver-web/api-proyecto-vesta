# Script para configurar Postman con la API de Vesta
# Ejecutar después de configurar POSTMAN_API_KEY

param(
    [Parameter(Mandatory=$false)]
    [string]$ApiKey = $env:POSTMAN_API_KEY
)

if (-not $ApiKey) {
    Write-Host "❌ Error: POSTMAN_API_KEY no está configurada" -ForegroundColor Red
    Write-Host "Por favor, configura tu clave API de Postman:" -ForegroundColor Yellow
    Write-Host "set POSTMAN_API_KEY=tu_clave_aqui" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Para obtener una clave API:" -ForegroundColor Yellow
    Write-Host "1. Ve a https://postman.com" -ForegroundColor White
    Write-Host "2. Inicia sesión" -ForegroundColor White
    Write-Host "3. Ve a Settings → API Keys" -ForegroundColor White
    Write-Host "4. Genera una nueva clave con permisos de workspace, collection y environment" -ForegroundColor White
    exit 1
}

Write-Host "🚀 Configurando Postman para la API de Vesta..." -ForegroundColor Green
Write-Host ""

# Verificar que Kiro esté ejecutándose
Write-Host "📋 Verificando configuración..." -ForegroundColor Cyan

# Mostrar instrucciones para el usuario
Write-Host "✅ Script preparado. Ahora ejecuta los siguientes comandos en Kiro:" -ForegroundColor Green
Write-Host ""
Write-Host "1. Crear workspace:" -ForegroundColor Yellow
Write-Host 'kiroPowers use postman postman createWorkspace {"workspace": {"name": "Vesta API Project", "type": "personal"}}' -ForegroundColor White
Write-Host ""
Write-Host "2. Importar colección desde archivo:" -ForegroundColor Yellow
Write-Host "   - Usa el archivo: vesta-api-collection.json" -ForegroundColor White
Write-Host ""
Write-Host "3. Crear entornos de desarrollo:" -ForegroundColor Yellow
Write-Host "   - Local: http://localhost:8080" -ForegroundColor White
Write-Host "   - Producción: https://vesta-web.duckdns.org" -ForegroundColor White
Write-Host ""
Write-Host "4. Ejecutar tests automáticos" -ForegroundColor Yellow
Write-Host ""
Write-Host "📝 Los IDs se guardarán en .postman.json para uso futuro" -ForegroundColor Cyan