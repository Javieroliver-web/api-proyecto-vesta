# Revisión del Estado del Despliegue en Google Cloud
**Fecha:** 22 de Enero 2026  
**IP del Servidor:** 34.175.116.7  
**Usuario:** vestaadmin

## 📊 Estado General

### ✅ Servicios Activos
- **PostgreSQL 14.20**: ✅ Activo y funcionando
  - Puerto: 5432 (solo escucha en localhost)
  - Base de datos: `vesta_db` existe
  - Usuario de BD: `vesta_user` existe
  - Tablas: 13 tablas creadas correctamente

- **Tomcat**: ✅ Activo y funcionando
  - Puerto: 8080 (escucha en todas las interfaces)
  - Proceso Java: PID 480, usando 760MB RAM
  - Configuración: Java 21, 512M-1024M heap

### ⚠️ Problemas Identificados

#### 1. **FALTA DE ADMINISTRADORES** 🔴 CRÍTICO
- **Estado**: No hay ningún usuario con rol `ADMINISTRADOR` en la base de datos
- **Impacto**: No se puede acceder al panel de administración
- **Solución**: Ejecutar el script `create_admin.sql` que ya existe en el proyecto

#### 2. **USUARIOS NO CONFIRMADOS** 🟡 IMPORTANTE
- **Estado**: De 5 usuarios, solo 1 tiene el email confirmado (demo@vesta.com)
- **Usuarios afectados**:
  - javierp20055@gmail.com (no confirmado)
  - javierp20055@gmail.com (no confirmado)
  - juandaopka@gmail.com (no confirmado)
  - javip200555@gmail.com (no confirmado)
- **Impacto**: Los usuarios no pueden iniciar sesión hasta confirmar su email
- **Solución**: Confirmar manualmente o reenviar emails de confirmación

#### 3. **CONFIGURACIÓN DE URLs** ✅ CORREGIDO
- **Estado**: ✅ **RESUELTO** - URLs actualizadas correctamente
- **Configuración actual en tomcat.service**:
  ```
  API_URL=http://34.175.116.7:8080/vesta-api/api
  FRONTEND_URL=http://vesta-web.duckdns.org/vesta-web
  ```
- **Nota**: La API_URL usa la IP directa porque no tiene DNS, mientras que FRONTEND_URL mantiene el dominio DuckDNS que sí está configurado.

#### 4. **ERROR DE CONEXIÓN EN LOGS** 🟡 IMPORTANTE
- **Error detectado**:
  ```
  ERROR com.vesta.web.service.ApiService - Error de conexión con la API: 
  Connect to http://34.175.55.214.nip.io:8080 [34.175.55.214.nip.io/34.175.55.214] 
  failed: Connect timed out
  ```
- **Causa**: La aplicación web está intentando conectarse a una IP antigua
- **Impacto**: Los usuarios no pueden iniciar sesión desde la web
- **Solución**: Actualizar la configuración de la API en la aplicación web

#### 5. **CONFIGURACIÓN DE BASE DE DATOS** ✅ CORRECTO
- **Estado**: La configuración es correcta
  - URL: `jdbc:postgresql://localhost:5432/vesta_db`
  - Usuario: `vesta_user`
  - Contraseña: Configurada en el servicio systemd

## 🔧 Soluciones Recomendadas

### Solución 1: Crear Usuario Administrador
```bash
# Conectarse al servidor
ssh -i vesta_key vestaadmin@34.175.116.7

# Ejecutar el script SQL
sudo -u postgres psql -d vesta_db -f /ruta/a/create_admin.sql
```

O ejecutar manualmente:
```sql
INSERT INTO usuarios (
    usu_nombre_completo, usu_email, usu_movil, usu_password,
    usu_rol, usu_activo, usu_email_confirmado,
    usu_acepta_terminos, usu_acepta_privacidad,
    usu_ciudad, usu_pais, usu_tema, usu_fecha_creacion
) VALUES (
    'Admin Vesta',
    'warshadows22@gmail.com',
    '+34622645922',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'ADMINISTRADOR',
    true,
    true,
    true,
    true,
    'Sevilla, ES',
    'España',
    'light',
    NOW()
);
```

### Solución 2: Confirmar Usuarios Manualmente
```sql
-- Confirmar todos los usuarios pendientes
UPDATE usuarios 
SET usu_email_confirmado = true 
WHERE usu_email_confirmado = false;
```

### Solución 3: Actualizar URLs en tomcat.service ✅ COMPLETADO
```bash
# ✅ Ya aplicado - Usar el script update_api_url.py
# La configuración actual es:
Environment="API_URL=http://34.175.116.7:8080/vesta-api/api"
Environment="FRONTEND_URL=http://vesta-web.duckdns.org/vesta-web"
```

### Solución 4: Verificar Configuración de la Aplicación Web
- Revisar `web-proyecto-vesta/src/main/resources/application-prod.properties`
- Asegurarse de que `api.url` apunta a la IP correcta: `http://34.175.116.7:8080/vesta-api/api`

## 📋 Checklist de Verificación Post-Despliegue

- [ ] Usuario administrador creado y funcional
- [ ] Todos los usuarios pueden confirmar su email
- [ ] URLs de confirmación funcionan correctamente
- [ ] La aplicación web se conecta correctamente a la API
- [ ] Los logs no muestran errores de conexión
- [ ] El servicio de email está configurado correctamente
- [ ] Los puertos están abiertos en el firewall de Google Cloud

## 🔐 Seguridad

### Recomendaciones:
1. **Cambiar contraseñas por defecto**: La contraseña del admin en el script es "password" - cambiarla inmediatamente
2. **Configurar firewall**: Asegurar que solo los puertos necesarios estén abiertos
3. **SSL/TLS**: Considerar configurar HTTPS si se usa dominio
4. **Backups**: Configurar backups automáticos de la base de datos

## 📝 Notas Adicionales

- El servicio PostgreSQL solo escucha en localhost (127.0.0.1:5432), lo cual es correcto para seguridad
- Tomcat escucha en todas las interfaces (*:8080), lo cual permite acceso externo
- La aplicación está usando Java 21 con configuración de memoria adecuada
- Los logs muestran que la aplicación se inició correctamente
