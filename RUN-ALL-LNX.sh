#!/bin/bash

set -e

echo "Building Maven projects..."
mvn clean package

echo "Stopping previous containers..."
docker compose down -v

echo "Starting all components..."
docker compose up --build