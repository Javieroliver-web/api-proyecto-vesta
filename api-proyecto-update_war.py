#!/usr/bin/env python3
import zipfile
import re
import os
import tempfile
import shutil

war_path = '/opt/tomcat/webapps/vesta-web.war'
temp_dir = tempfile.mkdtemp()

try:
    # Extraer el WAR
    with zipfile.ZipFile(war_path, 'r') as war:
        war.extractall(temp_dir)
    
    # Actualizar application-prod.properties
    prod_prop_path = os.path.join(temp_dir, 'WEB-INF/classes/application-prod.properties')
    if os.path.exists(prod_prop_path):
        with open(prod_prop_path, 'r') as f:
            content = f.read()
        
        content = re.sub(r'api\.url=.*', 'api.url=http://vesta-web.duckdns.org:8080/vesta-api/api', content)
        
        with open(prod_prop_path, 'w') as f:
            f.write(content)
        print('application-prod.properties actualizado')
    
    # Actualizar application.properties
    prop_path = os.path.join(temp_dir, 'WEB-INF/classes/application.properties')
    if os.path.exists(prop_path):
        with open(prop_path, 'r') as f:
            content = f.read()
        
        content = re.sub(r'api\.url=.*', 'api.url=http://vesta-web.duckdns.org:8080/vesta-api/api', content)
        
        with open(prop_path, 'w') as f:
            f.write(content)
        print('application.properties actualizado')
    
    # Recrear el WAR
    with zipfile.ZipFile(war_path, 'w', zipfile.ZIP_DEFLATED) as war:
        for root, dirs, files in os.walk(temp_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, temp_dir)
                war.write(file_path, arcname)
    
    print('WAR actualizado correctamente')
    
finally:
    shutil.rmtree(temp_dir)
