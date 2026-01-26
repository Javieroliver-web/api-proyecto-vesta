# Test directo de la API de Vesta
# Versión simplificada

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://34.175.81.8:8080"
)

Write-Host "🧪 Testing Vesta API..." -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host ""

# Test básico
Write-Host "1. 🔍 Test básico de la API:" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$BaseUrl/api/auth/test" -Method GET -SkipCertificateCheck -ErrorAction Stop
    Write-Host "✅ API funcionando!" -ForegroundColor Green
    Write-Host "   Respuesta completa: $($result | ConvertTo-Json -Compress)" -ForegroundColor White
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "   Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}
Write-Host ""

# Login de prueba
Write-Host "2. 🔑 Intentando login con admin:" -ForegroundColor Yellow
try {
    $loginBody = @{
        correoElectronico = "admin@vesta.com"
        password = "admin123"
    } | ConvertTo-Json
    
    Write-Host "   Enviando: $loginBody" -ForegroundColor Gray
    
    $loginResult = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -SkipCertificateCheck -ErrorAction Stop
    
    Write-Host "✅ Login exitoso!" -ForegroundColor Green
    Write-Host "   Respuesta: $($loginResult | ConvertTo-Json -Compress)" -ForegroundColor White
    
    if ($loginResult.data -and $loginResult.data.token) {
        $global:AuthToken = $loginResult.data.token
        Write-Host "   Token guardado para siguientes requests" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "   Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        try {
            $errorContent = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorContent)
            $errorText = $reader.ReadToEnd()
            Write-Host "   Error details: $errorText" -ForegroundColor Yellow
        } catch {
            Write-Host "   No se pudo leer el detalle del error" -ForegroundColor Yellow
        }
    }
}
Write-Host ""

# Test de productos (si tenemos token)
if ($global:AuthToken) {
    Write-Host "3. 📦 Probando endpoint de productos:" -ForegroundColor Yellow
    try {
        $headers = @{ Authorization = "Bearer $global:AuthToken" }
        $products = Invoke-RestMethod -Uri "$BaseUrl/api/productos" -Method GET -Headers $headers -SkipCertificateCheck -ErrorAction Stop
        Write-Host "✅ Productos obtenidos: $($products.Count)" -ForegroundColor Green
        
        if ($products -and $products.Count -gt 0) {
            Write-Host "   Primer producto: $($products[0].nombre)" -ForegroundColor White
        }
    } catch {
        Write-Host "❌ Error obteniendo productos: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "3. ⏭️  Saltando test de productos (no hay token)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎯 Para usar en Insomnia:" -ForegroundColor Cyan
Write-Host "1. Importa insomnia-collection.json" -ForegroundColor White
Write-Host "2. Usa el entorno 'Production' con URL: $BaseUrl" -ForegroundColor White
Write-Host "3. Ejecuta 'Test Auth Endpoint' primero" -ForegroundColor White
Write-Host "4. Luego 'Login' para obtener el token" -ForegroundColor White
Write-Host "5. Copia el token a la variable 'auth_token'" -ForegroundColor White