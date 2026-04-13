# Vesta API — Configuración Google Cloud VM (Preservada)

> ⚠️ La VM de Google Cloud está **apagada** para ahorrar costes.
> El despliegue activo actual usa **Koyeb + Neon** (perfil `cloud`).
> Este documento explica cómo reactivar la configuración original.

---

## Datos de la VM

| Campo        | Valor                         |
|--------------|-------------------------------|
| IP pública   | `34.175.116.7`                |
| Dominio      | `vesta-web.duckdns.org`       |
| Región       | `europe-southwest1` (Madrid)  |
| SO           | Ubuntu 22.04 LTS              |
| SSH Key      | `vesta_key` / `vesta_key.pub` |

> 🔑 Las claves SSH están en la raíz del proyecto (**solo local, ignoradas por Git**).

---

## Cómo reactivar la VM

### 1. Encender la VM

```bash
gcloud compute instances start vesta-vm --zone=europe-southwest1-a
```

O desde la consola web: https://console.cloud.google.com/compute

### 2. Conectar por SSH

```bash
ssh -i vesta_key altair@34.175.116.7
```

### 3. Levantar servicios con Docker Compose

```bash
cd ~/vesta
git pull
docker compose --env-file .env up -d --build
```

### 4. Verificar servicios

```bash
docker compose ps
docker compose logs -f
curl http://localhost:8080/actuator/health
```

---

## Perfil activo

La VM usaba el perfil `prod` (configurado en `docker-compose.yml` via variable de entorno):

```
SPRING_PROFILES_ACTIVE=prod
```

El archivo `application-prod.properties` en ambos proyectos está **conservado intacto** con la configuración original de Google Cloud.

---

## Cambiar de Koyeb a Google Cloud

1. Encender la VM (paso 1 de arriba)
2. Actualizar DuckDNS con la nueva IP si cambió
3. Renovar certificado SSL si expiró: `sudo certbot renew`
4. Levantar Docker Compose (paso 3)
5. En Koyeb: pausar o eliminar los servicios

---

## Arquitectura original (Docker Compose)

```
Internet → Nginx (80/443) → WAR API (8080) → PostgreSQL (5432)
                         ↘ WAR Web (8081)
```

- `docker-compose.yml` en la raíz del proyecto — conservado intacto
- `Dockerfile.prod` — imagen de producción original con Tomcat

---

## Notas de seguridad

- Las claves SSH (`vesta_key`, `vesta_key.pub`) están en `.gitignore` — **no subir**
- Las credenciales de la BD están en `.env` — **no subir**
- El dominio DuckDNS actualiza la IP automáticamente si el cliente DuckDNS está corriendo en la VM
