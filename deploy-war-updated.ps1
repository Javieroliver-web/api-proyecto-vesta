# Script de Despliegue - WAR Actualizado con Correcciones
# Fecha: 23 de Enero 2026

Write-Host "=== DESPLIEGUE WAR ACTUALIZADO - PROYECTO VESTA ===" -ForegroundColor Green
Write-Host ""

# Verificar que el WAR existe
$warPath = "..\web-proyecto-vesta\target\vesta-web.war"
if (Test-Path $warPath) {
    Write-Host "✅ WAR encontrado: $warPath" -ForegroundColor Green
    $warSize = (Get-Item $warPath).Length / 1MB
    Write-Host "📦 Tamaño: $([math]::Round($warSize, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "❌ WAR no encontrado en: $warPath" -ForegroundColor Red
    Write-Host "Por favor, ejecuta 'mvn clean package -DskipTests' en el directorio web-proyecto-vesta" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "🔧 CORRECCIONES INCLUIDAS EN ESTE WAR:" -ForegroundColor Yellow
Write-Host "  ✅ Corrección de descarga de PDF" -ForegroundColor Green
Write-Host "  ✅ Migración completa a endpoints proxy" -ForegroundColor Green
Write-Host "  ✅ Eliminación de llamadas directas a /vesta-api/api" -ForegroundColor Green
Write-Host "  ✅ Nuevos controladores para usuario, admin, derechos RGPD" -ForegroundColor Green
Write-Host "  ✅ Mejor manejo de errores y autenticación" -ForegroundColor Green
Write-Host ""

# Copiar WAR al directorio actual para facilitar el despliegue
Write-Host "📋 Copiando WAR al directorio actual..." -ForegroundColor Cyan
Copy-Item $warPath ".\vesta-web-updated.war" -Force
Write-Host "✅ WAR copiado como: vesta-web-updated.war" -ForegroundColor Green
Write-Host ""

Write-Host "🚀 OPCIONES DE DESPLIEGUE:" -ForegroundColor Yellow
Write-Host ""
Write-Host "OPCIÓN 1 - SCP Manual:" -ForegroundColor Cyan
Write-Host "  scp -i vesta_key vesta-web-updated.war root@34.175.116.7:/tmp/" -ForegroundColor White
Write-Host ""
Write-Host "OPCIÓN 2 - Webmin (Recomendado):" -ForegroundColor Cyan
Write-Host "  1. Ir a: https://34.175.116.7:10000" -ForegroundColor White
Write-Host "  2. Login: root / admin123" -ForegroundColor White
Write-Host "  3. File Manager > Upload vesta-web-updated.war a /tmp/" -ForegroundColor White
Write-Host ""
Write-Host "OPCIÓN 3 - Google Cloud Console:" -ForegroundColor Cyan
Write-Host "  1. Ir a Google Cloud Console" -ForegroundColor White
Write-Host "  2. Compute Engine > VM instances" -ForegroundColor White
Write-Host "  3. SSH browser > Upload file" -ForegroundColor White
Write-Host ""

Write-Host "📋 COMANDOS PARA EJECUTAR EN EL SERVIDOR:" -ForegroundColor Yellow
Write-Host ""
Write-Host "# Detener Tomcat" -ForegroundColor Green
Write-Host "systemctl stop tomcat" -ForegroundColor White
Write-Host ""
Write-Host "# Hacer backup del WAR actual" -ForegroundColor Green
Write-Host "cp /opt/tomcat/webapps/vesta-web.war /opt/tomcat/webapps/vesta-web.war.backup" -ForegroundColor White
Write-Host ""
Write-Host "# Copiar nuevo WAR" -ForegroundColor Green
Write-Host "cp /tmp/vesta-web-updated.war /opt/tomcat/webapps/vesta-web.war" -ForegroundColor White
Write-Host ""
Write-Host "# Iniciar Tomcat" -ForegroundColor Green
Write-Host "systemctl start tomcat" -ForegroundColor White
Write-Host ""
Write-Host "# Verificar logs" -ForegroundColor Green
Write-Host "tail -f /opt/tomcat/logs/catalina.out" -ForegroundColor White
Write-Host ""

Write-Host "🧪 PRUEBAS POST-DESPLIEGUE:" -ForegroundColor Yellow
Write-Host "  1. Dashboard: http://vesta-web.duckdns.org/vesta-web/cliente/dashboard" -ForegroundColor White
Write-Host "  2. Mis Pólizas: http://vesta-web.duckdns.org/vesta-web/cliente/mis-polizas" -ForegroundColor White
Write-Host "  3. ⭐ PROBAR DESCARGA PDF: Botón 'Descargar Resumen' en Mis Pólizas" -ForegroundColor Magenta
Write-Host "  4. Marketplace: http://vesta-web.duckdns.org/vesta-web/cliente/marketplace" -ForegroundColor White
Write-Host "  5. Admin: http://vesta-web.duckdns.org/vesta-web/admin/dashboard" -ForegroundColor White
Write-Host ""

Write-Host "🔐 CREDENCIALES DE PRUEBA:" -ForegroundColor Yellow
Write-Host "  Usuario con pólizas: demo@vesta.com / 123456" -ForegroundColor White
Write-Host "  Usuario sin pólizas: javip200555@gmail.com / password" -ForegroundColor White
Write-Host "  Admin: warshadows22@gmail.com / password" -ForegroundColor White
Write-Host ""

Write-Host "✅ WAR LISTO PARA DESPLIEGUE" -ForegroundColor Green
Write-Host "Archivo: vesta-web-updated.war (en este directorio)" -ForegroundColor Cyan
Write-Host ""

# Intentar conexión SSH para verificar
Write-Host "🔍 Verificando conectividad SSH..." -ForegroundColor Cyan
try {
    $sshTest = ssh -i vesta_key -o ConnectTimeout=5 -o StrictHostKeyChecking=no root@34.175.116.7 "echo 'SSH OK'" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ SSH funcionando - Puedes usar SCP" -ForegroundColor Green
        Write-Host ""
        Write-Host "COMANDO DIRECTO:" -ForegroundColor Green
        Write-Host "scp -i vesta_key vesta-web-updated.war root@34.175.116.7:/tmp/" -ForegroundColor White
    } else {
        Write-Host "⚠️  SSH con problemas - Usa Webmin o Google Cloud Console" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  SSH no disponible - Usa Webmin o Google Cloud Console" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎯 RESUMEN: Todas las correcciones están implementadas y listas para despliegue." -ForegroundColor Green