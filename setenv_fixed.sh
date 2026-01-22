#!/bin/bash

# ==========================================
# Variables de Producción (Actualizadas)
# ==========================================

# URLs corregidas con IP actual
export API_URL="http://34.175.116.7:8080/vesta-api/api"
export FRONTEND_URL="http://vesta-web.duckdns.org/vesta-web"

# URLs para enlaces de activación
export APP_BASE_URL="http://vesta-web.duckdns.org"
export APP_FRONTEND_URL="http://vesta-web.duckdns.org/vesta-web"

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