#!/bin/bash
sudo sed -i 's|Environment="API_URL=.*"|Environment="API_URL=http://vesta-web.duckdns.org/vesta-api"|' /etc/systemd/system/tomcat.service
sudo sed -i 's|Environment="FRONTEND_URL=.*"|Environment="FRONTEND_URL=http://vesta-web.duckdns.org/vesta-web"|' /etc/systemd/system/tomcat.service
sudo systemctl daemon-reload
sudo systemctl restart tomcat
