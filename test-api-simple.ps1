# Test simple de la API de Vesta
# Maneja certificados SSL auto-firmados

param(
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "https://vesta-web.duckdns.org/vesta-web"
)

# Ignorar errores de certificado SSL para testing
if (-not ([System.Management.Automation.PSTypeName]'ServerCertificateValidationCallback').Type) {
    $certCallback = @"
        using System;
        using System.Net;
        using System.Net.Security;
        using System.Security.Cryptography.X509Certificates;
        public class ServerCertificateValidationCallback
        {
            public static void Ignore()
            {
                if(ServicePointManager.ServerCertificateValidationCallback ==null)
                {
                    ServicePointManager.ServerCertificateValidationCallback += 
                        delegate
                        (
                            Object obj, 
                            X509Certificate certificate, 
                            X509Chain chain, 
                            SslPolicyErrors errors
                        )
                        {
                            return true;
                        };
                }
            }
        }
"@
    Add-Type $certCallback
}
[ServerCertificateValidationCallback]::Ignore()

Write-Host "🧪 Testing Vesta API (ignorando SSL)..." -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host ""

# Test básico
Write-Host "1. 🔍 Test básico de la API:" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$BaseUrl/api/auth/test" -Method GET -SkipCertificateCheck
    Write-Host "✅ Respuesta: $($result.data)" -ForegroundColor Green
    Write-Host "   Mensaje: $($result.message)" -ForegroundColor White
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 Verifica que la URL sea correcta y que la API esté funcionando" -ForegroundColor Yellow
}
Write-Host ""

# Login de prueba
Write-Host "2. 🔑 Intentando login:" -ForegroundColor Yellow
try {
    $loginBody = @{
        correoElectronico = "admin@vesta.com"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResult = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -SkipCertificateCheck
    
    if ($loginResult.success) {
        $token = $loginResult.data.token
        Write-Host "✅ Login exitoso!" -ForegroundColor Green
        Write-Host "   Usuario: $($loginResult.data.usuario.nombreCompleto)" -ForegroundColor White
        Write-Host "   Token: $($token.Substring(0,20))..." -ForegroundColor White
        
        # Guardar token para siguientes requests
        $global:AuthToken = $token
    } else {
        Write-Host "❌ Login falló: $($loginResult.message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 Verifica las credenciales o que el usuario admin exista" -ForegroundColor Yellow
}
Write-Host ""

# Test con autenticación (si tenemos token)
if ($global:AuthToken) {
    Write-Host "3. 📦 Probando endpoint de productos:" -ForegroundColor Yellow
    try {
        $headers = @{ Authorization = "Bearer $global:AuthToken" }
        $products = Invoke-RestMethod -Uri "$BaseUrl/api/productos" -Method GET -Headers $headers -SkipCertificateCheck
        Write-Host "✅ Productos obtenidos: $($products.Count)" -ForegroundColor Green
        
        if ($products.Count -gt 0) {
            Write-Host "   Primer producto: $($products[0].nombre)" -ForegroundColor White
        }
    } catch {
        Write-Host "❌ Error obteniendo productos: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🎯 Comandos curl equivalentes:" -ForegroundColor Cyan
Write-Host ""
Write-Host "# Test básico:" -ForegroundColor Yellow
Write-Host "curl -k -X GET `"$BaseUrl/api/auth/test`"" -ForegroundColor White
Write-Host ""
Write-Host "# Login:" -ForegroundColor Yellow
Write-Host @"
curl -k -X POST "$BaseUrl/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "correoElectronico": "admin@vesta.com",
    "password": "admin123"
  }'
"@ -ForegroundColor White

Write-Host ""
Write-Host "💡 Nota: -k ignora errores de certificado SSL" -ForegroundColor Cyan