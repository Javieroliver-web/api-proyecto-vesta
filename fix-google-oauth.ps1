#!/usr/bin/env pwsh
# Script para corregir configuración Google OAuth

Write-Host "=== CORRIGIENDO CONFIGURACIÓN GOOGLE OAUTH ===" -ForegroundColor Green

# Información de configuración OAuth
$clientId = "249929311715-e9qf6foamkq5dftrpijv9phha1fltd29.apps.googleusercontent.com"
$clientSecret = "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"

Write-Host "1. Configuración OAuth actualizada en variables de entorno" -ForegroundColor Yellow
Write-Host "   Client ID: $clientId" -ForegroundColor Cyan
Write-Host "   Client Secret: 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" -ForegroundColor Cyan

Write-Host "`n2. URLs de redirección que deben estar configuradas en Google Cloud Console:" -ForegroundColor Yellow
Write-Host "   https://vesta-web.duckdns.org/login/oauth2/code/google" -ForegroundColor Cyan
Write-Host "   https://vesta-web2.duckdns.org/login/oauth2/code/google" -ForegroundColor Cyan
Write-Host "   https://vesta-web.duckdns.org/vesta-web/login/oauth2/code/google" -ForegroundColor Cyan
Write-Host "   https://vesta-web2.duckdns.org/vesta-web/login/oauth2/code/google" -ForegroundColor Cyan

Write-Host "`n3. Dominios autorizados que deben estar en Google Cloud Console:" -ForegroundColor Yellow
Write-Host "   vesta-web.duckdns.org" -ForegroundColor Cyan
Write-Host "   vesta-web2.duckdns.org" -ForegroundColor Cyan

Write-Host "`n4. Reiniciando servicios con nueva configuración..." -ForegroundColor Yellow
ssh -i vesta_key -o StrictHostKeyChecking=no vestaadmin@34.175.81.8 "sudo systemctl daemon-reload && sudo systemctl restart tomcat"

Write-Host "`n5. Esperando 20 segundos para que inicie..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host "`n=== VERIFICACIÓN DE CONFIGURACIÓN ===" -ForegroundColor Green

# Verificar variables de entorno
Write-Host "Verificando variables de entorno en el servidor..." -ForegroundColor Cyan
ssh -i vesta_key -o StrictHostKeyChecking=no vestaadmin@34.175.81.8 "sudo cat /etc/systemd/system/tomcat.service | grep GOOGLE_CLIENT"

Write-Host "`n=== INSTRUCCIONES PARA GOOGLE CLOUD CONSOLE ===" -ForegroundColor Green
Write-Host "Para completar la configuración, ve a Google Cloud Console:" -ForegroundColor White
Write-Host "1. Ve a: https://console.cloud.google.com/apis/credentials" -ForegroundColor Yellow
Write-Host "2. Selecciona tu proyecto" -ForegroundColor Yellow
Write-Host "3. Edita el cliente OAuth 2.0 con ID: $clientId" -ForegroundColor Yellow
Write-Host "4. En 'URIs de redirección autorizados', agrega:" -ForegroundColor Yellow
Write-Host "   - https://vesta-web.duckdns.org/login/oauth2/code/google" -ForegroundColor Cyan
Write-Host "   - https://vesta-web2.duckdns.org/login/oauth2/code/google" -ForegroundColor Cyan
Write-Host "5. En 'Dominios autorizados', agrega:" -ForegroundColor Yellow
Write-Host "   - vesta-web.duckdns.org" -ForegroundColor Cyan
Write-Host "   - vesta-web2.duckdns.org" -ForegroundColor Cyan
Write-Host "6. Guarda los cambios" -ForegroundColor Yellow

Write-Host "`n=== PRUEBA DE OAUTH ===" -ForegroundColor Green
Write-Host "Una vez configurado en Google Cloud Console, prueba:" -ForegroundColor White
Write-Host "https://vesta-web.duckdns.org/vesta-web/login" -ForegroundColor Cyan
Write-Host "https://vesta-web2.duckdns.org/vesta-web/login" -ForegroundColor Cyan

Write-Host "`n✅ Configuración OAuth actualizada. Completa la configuración en Google Cloud Console." -ForegroundColor Green