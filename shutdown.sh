#!/bin/sh

set -e

echo "=== Shutdowning Problem Forge Application ==="

# Down services
docker compose -f backend/docker-compose.yml down
docker compose -f frontend/docker-compose.yml down

echo "=== Shutdown finished ==="
