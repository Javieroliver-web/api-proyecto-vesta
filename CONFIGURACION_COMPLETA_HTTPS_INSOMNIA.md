# Configuración Completa - HTTPS + Insomnia + API

**Fecha**: 26 de Enero 2026  
**IP Servidor**: 34.175.81.8  

## 🌐 Configuración de Dominios (AMBOS MANTENIDOS)

### ✅ Dominio 1: vesta-web.duckdns.org (HTTP)
- **IP**: 34.175.116.7 (IP anterior)
- **Protocolo**: HTTP solamente
- **URL Web**: http://vesta-web.duckdns.org/vesta-web/
- **URL API**: http://vesta-web.duckdns.org/api/
- **Estado**: ✅ Funcionando (Health check: 200)

### ✅ Dominio 2: vesta-web2.duckdns.org (HTTPS)
- **IP**: 34.175.81.8 (IP actual)
- **Protocolo**: HTTPS con certificado SSL válido
- **URL Web**: https://vesta-web2.duckdns.org/vesta-web/
- **URL API**: https://vesta-web2.duckdns.org/api/
- **Estado**: ✅ Funcionando (Health check: 200)
- **Certificado**: Válido hasta 24 Abril 2026

## 🔒 Certificados SSL

```
Certificate Name: vesta-web.duckdns.org
- Expiry Date: 2026-04-23 (86 días restantes)
- Domains: vesta-web.duckdns.org

Certificate Name: vesta-web2.duckdns.org  
- Expiry Date: 2026-04-24 (87 días restantes)
- Domains: vesta-web2.duckdns.org
```

## 📋 Configuración de Insomnia Actualizada

### Entornos Disponibles:
1. **Base Environment**: `http://34.175.81.8:8080` (desarrollo directo)
2. **Local Development**: `http://localhost:8080` (desarrollo local)
3. **Production (Domain)**: `https://vesta-web.duckdns.org` (dominio original)
4. **HTTP (vesta-web.duckdns.org)**: `http://vesta-web.duckdns.org` (HTTP)
5. **HTTPS (vesta-web2.duckdns.org)**: `https://vesta-web2.duckdns.org` (HTTPS)
6. **Direct IP**: `http://34.175.81.8:8080` (IP directa)

### URLs de Testing por Entorno:

#### HTTP (vesta-web.duckdns.org):
- Test: `http://vesta-web.duckdns.org/api/auth/test`
- Login: `http://vesta-web.duckdns.org/api/auth/login`
- Productos: `http://vesta-web.duckdns.org/api/productos`

#### HTTPS (vesta-web2.duckdns.org):
- Test: `https://vesta-web2.duckdns.org/api/auth/test`
- Login: `https://vesta-web2.duckdns.org/api/auth/login`
- Productos: `https://vesta-web2.duckdns.org/api/productos`

## 🚨 Estado Actual de la API

### ✅ Web Funcionando:
- **HTTP**: http://vesta-web.duckdns.org/vesta-web/ ✅
- **HTTPS**: https://vesta-web2.duckdns.org/vesta-web/ ✅

### ❌ API No Funcionando:
- **Problema**: Endpoints devuelven HTTP 404
- **Causa**: Aplicación vesta-api no inicia correctamente
- **Error**: Fallo de conexión a base de datos durante arranque

### Diagnóstico Realizado:
1. ✅ PostgreSQL funcionando
2. ✅ Usuario vesta_user con contraseña configurada
3. ✅ Base de datos vesta_db existe con tablas
4. ✅ WAR desplegado físicamente
5. ❌ Aplicación Spring Boot no inicia

## 🔧 Configuración de Nginx

### Características:
- **Proxy reverso** configurado para ambos dominios
- **Headers de seguridad** en HTTPS
- **Redirección automática** HTTP → HTTPS para vesta-web2
- **Health checks** disponibles en `/health`
- **Caché de archivos estáticos** configurado

### Rutas Configuradas:
```
/vesta-web/  → localhost:8080/vesta-web/
/api/        → localhost:8080/vesta-api/api/
/cliente/    → localhost:8080/vesta-web/cliente/
/admin/      → localhost:8080/vesta-web/admin/
```

## 🎯 Ventajas de Mantener Ambos Dominios

### Flexibilidad:
- **HTTP**: Para desarrollo y testing rápido
- **HTTPS**: Para producción y seguridad

### Redundancia:
- **Backup automático** si uno falla
- **Diferentes IPs** para balanceo

### Casos de Uso:
- **Desarrollo**: Usar HTTP para velocidad
- **Producción**: Usar HTTPS para seguridad
- **Testing**: Alternar entre ambos

## 📁 Archivos de Configuración

### Insomnia:
- `insomnia-collection.json` - 6 entornos configurados
- Endpoints listos para ambos protocolos

### Testing:
- `test-api-direct.ps1` - Scripts actualizados
- Health checks para ambos dominios

## 🚀 Próximos Pasos

### Para la API:
1. Resolver problema de arranque de Spring Boot
2. Verificar configuración de profiles
3. Probar endpoints una vez funcional

### Para Insomnia:
1. Importar colección actualizada
2. Probar con entorno HTTPS primero
3. Alternar entre HTTP/HTTPS según necesidad

## 💡 Recomendaciones

### Uso Diario:
- **Desarrollo**: `http://vesta-web.duckdns.org`
- **Producción**: `https://vesta-web2.duckdns.org`
- **Testing API**: IP directa cuando esté funcional

### Seguridad:
- Mantener certificados actualizados
- Usar HTTPS para datos sensibles
- HTTP solo para desarrollo

---

**Resumen**: Configuración dual HTTP/HTTPS completada y funcionando. Solo falta resolver el problema de arranque de la API para tener el sistema completamente operativo.