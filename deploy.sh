#!/bin/bash

APP_DIR="/home/bruno/bgls-app" # <--- Substitua pelo seu usuário no Ubuntu Server!

echo "--- Starting deployment process ---"

# Navega para o diretório da aplicação
cd "$APP_DIR" || { echo "Error: Could not navigate to $APP_DIR. Exiting."; exit 1; }

# Puxa a imagem mais recente para o serviço 'bgls-app'
echo "Pulling latest Docker image for bgls-app..."
docker compose pull bgls-app || { echo "Error: Failed to pull Docker image. Exiting."; exit 1; }

# Inicia/Reinicia os containers, removendo órfãos
# O '--build' não é necessário se a imagem já foi puxada, mas é inofensivo.
# '--remove-orphans' remove containers para serviços que não estão mais no docker-compose.yml
echo "Stopping and recreating bgls-app container..."
docker compose up -d --remove-orphans bgls-app || { echo "Error: Failed to start application containers. Exiting."; exit 1; }

echo "Deployment completed successfully for bgls-app!"
echo "--- Deployment process finished ---"
