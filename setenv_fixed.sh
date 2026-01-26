#!/bin/bash

# ==========================================
# Variables de Producción (Dual Domain Support)
# ==========================================

# URLs principales - Detectar automáticamente el dominio
# vesta-web.duckdns.org (HTTP) - Trabajo
# vesta-web2.duckdns.org (HTTPS) - Casa

# URLs por defecto (HTTPS para vesta-web2)
export API_URL="https://vesta-web2.duckdns.org/vesta-api/api"
export FRONTEND_URL="https://vesta-web2.duckdns.org/vesta-web"

# URLs HTTP para vesta-web (fallback)
export API_URL_HTTP="http://vesta-web.duckdns.org/vesta-api/api"
export FRONTEND_URL_HTTP="http://vesta-web.duckdns.org/vesta-web"

# URLs alternativas para acceso directo por IP
export API_URL_IP1="http://34.175.116.7:8080/vesta-api/api"
export API_URL_IP2="http://34.175.81.8:8080/vesta-api/api"
export FRONTEND_URL_IP1="http://34.175.116.7:8080/vesta-web"
export FRONTEND_URL_IP2="http://34.175.81.8:8080/vesta-web"

# URLs HTTPS para acceso directo por IP
export API_URL_IP1_HTTPS="https://34.175.116.7/vesta-api/api"
export API_URL_IP2_HTTPS="https://34.175.81.8/vesta-api/api"
export FRONTEND_URL_IP1_HTTPS="https://34.175.116.7/vesta-web"
export FRONTEND_URL_IP2_HTTPS="https://34.175.81.8/vesta-web"

# URLs para enlaces de activación (usar dominio HTTPS por defecto)
export APP_BASE_URL="https://vesta-web2.duckdns.org"
export APP_FRONTEND_URL="https://vesta-web2.duckdns.org/vesta-web"

# Base de datos
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/vesta_db"
export SPRING_DATASOURCE_USERNAME="vesta_user"
export SPRING_DATASOURCE_PASSWORD="vesta_password_2026"

# JWT
export JWT_SECRET="ClaveSecretaSuperSeguraParaVestaProyecto2025ChangeThisInProduction"
export JWT_EXPIRATION="86400000"

# Email
export MAIL_USERNAME="javip200555@gmail.com"
export MAIL_PASSWORD="dzbgccdnrnwtrnnm"

# APIs
export OPENWEATHER_API_KEY="tu_api_key"
export TWILIO_ACCOUNT_SID="tu_sid"
export TWILIO_AUTH_TOKEN="tu_token"
export TWILIO_MESSAGING_SERVICE_SID="tu_msg_sid"
export GOOGLE_CLIENT_ID="249929311715-e9qf6foamkq5dftrpijv9phha1fltd29.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="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"

export SPRING_PROFILES_ACTIVE="prod"