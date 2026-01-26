#!/usr/bin/env python3
import re
import sys

file_path = '/etc/systemd/system/tomcat.service'

try:
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Reemplazar API_URL manteniendo FRONTEND_URL - Soporta ambas IPs
    old_pattern = r'Environment="API_URL=.*"'
    new_value = 'Environment="API_URL=http://vesta-web.duckdns.org/vesta-api/api"'
    
    new_content = re.sub(old_pattern, new_value, content)
    
    with open(file_path, 'w') as f:
        f.write(new_content)
    
    print("SUCCESS: API_URL actualizada correctamente")
    sys.exit(0)
except Exception as e:
    print(f"ERROR: {e}")
    sys.exit(1)
