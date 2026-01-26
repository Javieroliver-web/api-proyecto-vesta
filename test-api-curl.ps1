# Comandos curl para probar la API de Vesta
# Útil para testing rápido sin herramientas adicionales

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "https://vesta-web.duckdns.org/vesta-web"
)

Write-Host "🧪 Testing Vesta API con curl..." -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host ""

# Test básico
Write-Host "1. 🔍 Test básico de la API:" -ForegroundColor Yellow
$testCmd = "curl -X GET `"$BaseUrl/api/auth/test`""
Write-Host $testCmd -ForegroundColor White
try {
    $result = Invoke-RestMethod -Uri "$BaseUrl/api/auth/test" -Method GET
    Write-Host "✅ Respuesta: $($result.data)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Login
Write-Host "2. 🔑 Login (obtener token):" -ForegroundColor Yellow
$loginCmd = @"
curl -X POST "$BaseUrl/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "correoElectronico": "admin@vesta.com",
    "password": "admin123"
  }'
"@
Write-Host $loginCmd -ForegroundColor White

try {
    $loginBody = @{
        correoElectronico = "admin@vesta.com"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResult = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResult.data.token
    Write-Host "✅ Token obtenido: $($token.Substring(0,20))..." -ForegroundColor Green
    
    # Guardar token para siguientes requests
    $global:AuthToken = $token
    
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 Asegúrate de que la API esté ejecutándose y que exista el usuario admin" -ForegroundColor Yellow
}
Write-Host ""

# Productos (si tenemos token)
if ($global:AuthToken) {
    Write-Host "3. 📦 Obtener productos:" -ForegroundColor Yellow
    $productsCmd = @"
curl -X GET "$BaseUrl/api/productos" \
  -H "Authorization: Bearer $global:AuthToken"
"@
    Write-Host $productsCmd -ForegroundColor White
    
    try {
        $headers = @{ Authorization = "Bearer $global:AuthToken" }
        $products = Invoke-RestMethod -Uri "$BaseUrl/api/productos" -Method GET -Headers $headers
        Write-Host "✅ Productos encontrados: $($products.Count)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Error obteniendo productos: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📋 Comandos adicionales útiles:" -ForegroundColor Cyan
Write-Host ""
Write-Host "# Registro de nuevo usuario:" -ForegroundColor Yellow
Write-Host @"
curl -X POST "$BaseUrl/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Test User",
    "correoElectronico": "test@example.com",
    "password": "password123",
    "movil": "+34600000000"
  }'
"@ -ForegroundColor White

Write-Host ""
Write-Host "# Buscar productos:" -ForegroundColor Yellow
Write-Host "curl -X GET `"$BaseUrl/api/productos/buscar?q=seguro`"" -ForegroundColor White

Write-Host ""
Write-Host "🎯 Para usar estos comandos:" -ForegroundColor Cyan
Write-Host "1. Asegúrate de que la API esté ejecutándose (mvn spring-boot:run)" -ForegroundColor White
Write-Host "2. Copia y pega los comandos curl en tu terminal" -ForegroundColor White
Write-Host "3. Reemplaza los tokens y URLs según sea necesario" -ForegroundColor White