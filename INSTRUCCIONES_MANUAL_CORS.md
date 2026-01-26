# Instrucciones Manuales - Actualización CORS

## Problema Identificado
La API no estaba configurada para trabajar correctamente con ambos dominios HTTPS.

## Solución Implementada
He actualizado la configuración CORS y reconstruido el WAR. Ahora necesitas aplicar estos cambios en el servidor.

## Pasos Manuales (Ejecutar en el servidor)

### 1. Conectarse al servidor
```bash
# Usa tu método preferido para conectarte al servidor 34.175.81.8
# Por ejemplo: Google Cloud Console, otro cliente SSH, etc.
```

### 2. Detener Tomcat
```bash
sudo systemctl stop tomcat
```

### 3. Hacer backup del WAR actual
```bash
sudo cp /opt/tomcat/webapps/vesta-api.war /opt/tomcat/webapps/vesta-api.war.backup
```

### 4. Eliminar despliegue anterior
```bash
sudo rm -rf /opt/tomcat/webapps/vesta-api/
sudo rm -f /opt/tomcat/webapps/vesta-api.war
```

### 5. Subir el nuevo WAR
Necesitas subir el archivo `target/vesta-api.war` (recién generado) al servidor y colocarlo en:
```bash
/opt/tomcat/webapps/vesta-api.war
```

### 6. Crear configuración CORS
Crear el archivo `/tmp/application-cors.properties` con este contenido:
```properties
# Configuración CORS para ambos dominios
spring.profiles.active=prod
spring.application.name=vesta-api
server.port=8080

# CORS - Ambos dominios HTTPS
cors.allowed.origins=https://vesta-web.duckdns.org,https://vesta-web2.duckdns.org

# URLs de aplicación
app.frontend.url=https://vesta-web.duckdns.org/vesta-web
app.api.url=https://vesta-web.duckdns.org/api

# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/vesta_db
spring.datasource.username=vesta_user
spring.datasource.password=vesta_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=ClaveSecretaSuperSeguraParaVestaProyecto2025ChangeThisInProduction
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.vesta=DEBUG

# Multipart
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.mail.enabled=false
```

### 7. Configurar permisos
```bash
sudo chown tomcat:tomcat /opt/tomcat/webapps/vesta-api.war
```

### 8. Iniciar Tomcat
```bash
sudo systemctl start tomcat
```

### 9. Esperar y verificar
```bash
# Esperar 20 segundos
sleep 20

# Verificar estado
sudo systemctl status tomcat

# Ver logs
sudo tail -30 /opt/tomcat/logs/catalina.out
```

### 10. Probar endpoints
```bash
# Probar API en ambos dominios
curl -k https://vesta-web.duckdns.org/api/auth/test
curl -k https://vesta-web2.duckdns.org/api/auth/test
```

## Resultado Esperado
Después de estos pasos, la API debería:
- ✅ Responder correctamente desde ambos dominios HTTPS
- ✅ Permitir CORS desde ambos dominios
- ✅ Funcionar con la aplicación web en cualquiera de los dos dominios

## Verificación Final
Una vez completado, ambas URLs deberían funcionar:
- https://vesta-web.duckdns.org/api/auth/test
- https://vesta-web2.duckdns.org/api/auth/test

## Archivos Actualizados Localmente
- ✅ `src/main/resources/application-prod.properties` (CORS configurado)
- ✅ `src/main/resources/application.properties` (URLs HTTPS)
- ✅ `application-fixed.properties` (configuración completa)
- ✅ `target/vesta-api.war` (WAR reconstruido con cambios)