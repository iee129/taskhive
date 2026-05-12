#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="taskhive"

echo "=== TaskHive K8s Deploy ==="

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/database/
kubectl apply -f k8s/backend/
kubectl apply -f k8s/frontend/
kubectl apply -f k8s/ingress.yaml

echo "Waiting for pods..."
kubectl rollout status deployment/backend -n "$NAMESPACE"
kubectl rollout status deployment/frontend -n "$NAMESPACE"

echo "Deploy complete."
kubectl get pods -n "$NAMESPACE"
