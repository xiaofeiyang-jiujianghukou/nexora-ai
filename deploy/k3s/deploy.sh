#!/bin/bash
# ============================================================
# Nexora AI — K3s 一键部署脚本
# 用法: bash deploy/k3s/deploy.sh
# ============================================================
set -e

echo "=== Nexora AI K3s Deploy ==="
echo ""

# 1. Namespace
echo "[1/7] Creating namespace..."
kubectl apply -f deploy/k3s/namespace.yaml

# 2. Secrets & Config (⚠️ 生产环境请先修改 deploy/k3s/secret.yaml)
echo "[2/7] Creating secrets & configmap..."
kubectl apply -f deploy/k3s/secret.yaml
kubectl apply -f deploy/k3s/configmap.yaml

# 3. Infrastructure (MySQL, Redis, ES)
echo "[3/7] Deploying infrastructure..."
kubectl apply -f deploy/k3s/infrastructure.yaml
echo "  Waiting for MySQL..."
kubectl -n nexora wait --for=condition=ready pod -l app=mysql --timeout=120s
echo "  Waiting for Redis..."
kubectl -n nexora wait --for=condition=ready pod -l app=redis --timeout=60s

# 4. Backend
echo "[4/7] Deploying backend..."
kubectl apply -f deploy/k3s/backend-deployment.yaml
kubectl apply -f deploy/k3s/backend-service.yaml

# 5. Frontend
echo "[5/7] Deploying frontend..."
kubectl apply -f deploy/k3s/frontend-deployment.yaml
kubectl apply -f deploy/k3s/frontend-service.yaml

# 6. Monitoring
echo "[6/7] Deploying monitoring..."
kubectl apply -f deploy/k3s/monitoring-deployment.yaml

# 7. Ingress
echo "[7/7] Deploying ingress..."
kubectl apply -f deploy/k3s/ingress.yaml

echo ""
echo "=== Deploy complete! ==="
echo ""
echo "Check status:"
echo "  kubectl -n nexora get pods"
echo "  kubectl -n nexora get svc"
echo ""
echo "Access (if K3s with Traefik):"
echo "  Frontend: http://nexora.local"
echo "  Grafana:  http://nexora.local:3000  (admin/admin)"
echo "  Prometheus: http://nexora.local:9090"
