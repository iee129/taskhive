#!/usr/bin/env bash
set -euo pipefail

echo "=== TaskHive Build ==="

echo "[1/2] Backend build..."
cd "$(dirname "$0")/../backend"
mvn -B package -DskipTests -q
echo "  Backend JAR: $(ls target/*.jar)"

echo "[2/2] Frontend build..."
cd "../frontend"
npm ci --silent
npm run build
echo "  Frontend dist: dist/"

echo "Build complete."
