# Vesta Seguros — API

API REST de **Vesta**, una plataforma de microseguros hecha como **proyecto de clase** (noviembre de 2025 – mayo de 2026). La web que la consume está en [web-proyecto-vesta](https://github.com/Javieroliver-web/web-proyecto-vesta).

> **Proyecto cerrado.** Se conserva como muestra de trabajo; no se mantiene ni está pensado para usarse en producción.

## Qué hace

- Registro e inicio de sesión con **JWT**, inicio de sesión con **Google (OAuth2)** y **doble factor (TOTP)**.
- Catálogo de seguros, contratación de pólizas, pedidos y pasarela de pago simulada (TPV).
- Gestión de siniestros, estadísticas para el administrador e informes y certificados en **PDF**.
- Registro de auditoría, registro del consentimiento de cookies y solicitudes de derechos RGPD.
- Avisos por correo y SMS (Twilio).
- Documentación de la API con **Swagger / OpenAPI**.

## Tecnologías

Java 21 · Spring Boot 3.2 (Web, Data JPA, Security, Validation, Cache, Mail, Actuator) · PostgreSQL · JJWT · springdoc-openapi · OpenPDF · Twilio · Docker.

## Ejecutarlo en local

1. Copia `.env.example` a `.env` y rellena los valores (base de datos, `JWT_SECRET`, correo, Google…).
2. `docker compose up` levanta PostgreSQL, la API y la web (la web se construye desde la carpeta `../web-proyecto-vesta`, así que clona los dos repos uno al lado del otro).
3. La documentación de la API queda en `/swagger-ui.html`.

Ningún secreto va en el repositorio: la configuración los lee de variables de entorno.
