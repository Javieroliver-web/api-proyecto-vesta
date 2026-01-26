# Configuración de Insomnia para API Vesta

## ¿Por qué Insomnia?

Insomnia es una excelente alternativa a Postman que ofrece:
- ✅ Interfaz más limpia y rápida
- ✅ Mejor manejo de variables de entorno
- ✅ Importación/exportación sencilla
- ✅ Gratuito y sin límites de requests
- ✅ Mejor para desarrollo local

## Instalación

1. Descarga Insomnia desde: https://insomnia.rest/download
2. Instala la aplicación en tu sistema

## Configuración Paso a Paso

### 1. Importar la Colección

1. Abre Insomnia
2. Ve a **Application** → **Preferences** → **Data**
3. Haz clic en **Import Data**
4. Selecciona el archivo `insomnia-collection.json`
5. La colección "Vesta API" aparecerá en tu workspace

### 2. Configurar Entornos

La colección incluye 3 entornos preconfigurados:

- **Base Environment**: `https://vesta-web.duckdns.org/vesta-web` (por defecto)
- **Production**: `https://vesta-web.duckdns.org/vesta-web` (producción)
- **Local Development**: `http://localhost:8080` (desarrollo local)

### 3. Iniciar la API

Antes de probar, asegúrate de que la API esté ejecutándose:

```bash
cd api-proyecto-vesta
mvn spring-boot:run
```

### 4. Probar los Endpoints

#### Orden recomendado:

1. **Test Auth Endpoint** (GET `/api/auth/test`)
   - No requiere autenticación
   - Verifica que la API funciona

2. **Login** (POST `/api/auth/login`)
   - Usa las credenciales por defecto:
     ```json
     {
       "correoElectronico": "admin@vesta.com",
       "password": "admin123"
     }
     ```
   - Copia el token de la respuesta

3. **Configurar Token**
   - Ve al entorno activo
   - Pega el token en la variable `auth_token`

4. **Get All Products** (GET `/api/productos`)
   - Ahora funcionará con autenticación

## Endpoints Disponibles

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario
- `GET /api/auth/test` - Verificar API

### Productos
- `GET /api/productos` - Listar todos los productos
- `GET /api/productos/{id}` - Obtener producto por ID
- `GET /api/productos/buscar?q=término` - Buscar productos

## Automatización con Hooks

Se ha configurado un hook que se ejecuta automáticamente cuando modificas código de la API:

- **Archivo**: `.kiro/hooks/api-postman-testing.kiro.hook`
- **Trigger**: Cuando guardas archivos Java o de configuración
- **Acción**: Sugiere probar los endpoints modificados

## Testing Manual con curl

Si prefieres usar curl, ejecuta:

```powershell
.\test-api-curl.ps1
```

Este script te mostrará comandos curl listos para usar.

## Solución de Problemas

### API no responde
```bash
# Verificar que Maven esté instalado
mvn --version

# Iniciar la API
cd api-proyecto-vesta
mvn spring-boot:run
```

### Error de autenticación
- Verifica que el usuario admin existe en la base de datos
- Revisa el archivo `application.properties` para la configuración de BD

### Token expirado
- Ejecuta el endpoint Login nuevamente
- Actualiza la variable `auth_token` en Insomnia

## Archivos Creados

- `insomnia-collection.json` - Colección completa para importar
- `setup-insomnia.ps1` - Script de configuración
- `test-api-curl.ps1` - Comandos curl para testing
- `.kiro/hooks/api-postman-testing.kiro.hook` - Hook automático

## Próximos Pasos

1. Instala Insomnia
2. Importa la colección
3. Inicia la API con Maven
4. Prueba los endpoints siguiendo el orden recomendado
5. ¡Empieza a desarrollar!

---

**¿Necesitas ayuda?** Los scripts de PowerShell te guiarán paso a paso.