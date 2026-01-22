#!/bin/bash
# Script para actualizar URLs usando DuckDNS
# API y Web están en el mismo servidor con el dominio vesta-web.duckdns.org

sudo python3 << 'PYEOF'
import re

file_path = '/etc/systemd/system/tomcat.service'
with open(file_path, 'r') as f:
    content = f.read()

# Actualizar API_URL para usar DuckDNS (sin /api al final, el código lo agrega)
content = re.sub(
    r'Environment="API_URL=.*"',
    'Environment="API_URL=http://vesta-web.duckdns.org:8080/vesta-api"',
    content
)

# FRONTEND_URL ya está correcto, pero lo verificamos
content = re.sub(
    r'Environment="FRONTEND_URL=.*"',
    'Environment="FRONTEND_URL=http://vesta-web.duckdns.org/vesta-web"',
    content
)

with open(file_path, 'w') as f:
    f.write(content)

print('URLs actualizadas para usar DuckDNS')
PYEOF

sudo systemctl daemon-reload
sudo systemctl restart tomcat
echo "✅ Configuración aplicada y Tomcat reiniciado"
