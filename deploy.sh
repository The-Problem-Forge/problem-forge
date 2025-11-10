#!/bin/sh

set -e

echo "=== Building and Deploying Problem Forge Application ==="

# Start services
echo "Creating network"
docker network create -d bridge problem-forge-network 2>/dev/null || true

docker compose -f backend/docker-compose.yml up --build -d
docker compose -f frontend/docker-compose.yml up --build -d

echo "=== Deployment Complete ==="
echo "Frontend: http://localhost:3000"
echo "Backend: http://localhost:8080"
echo "PostgreSQL: localhost:5432"

echo "Use \"docker compose logs -f\" in appropriate directory to display logs"
