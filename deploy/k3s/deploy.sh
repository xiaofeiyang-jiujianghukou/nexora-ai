#!/bin/bash
# ============================================================
# Nexora AI — K3s 一键部署脚本
# 用法: bash deploy/k3s/deploy.sh
# ============================================================
set -e

echo "=== Nexora AI K3s Deploy ==="
echo ""

# 0. Install ingress-nginx (if not already installed)
echo "[0/9] Ensuring ingress-nginx is installed..."
if kubectl get deployment ingress-nginx-controller -n ingress-nginx &>/dev/null; then
  echo "  ingress-nginx already installed, skipping."
else
  echo "  Installing ingress-nginx controller..."
  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.1/deploy/static/provider/cloud/deploy.yaml
  echo "  Waiting for ingress-nginx to be ready..."
  kubectl wait --namespace ingress-nginx \
    --for=condition=ready pod \
    --selector=app.kubernetes.io/component=controller \
    --timeout=120s
  echo "  ingress-nginx installed successfully."
fi

# 1. Namespace
echo "[1/9] Creating namespace..."
kubectl apply -f deploy/k3s/namespace.yaml

# 2. Secrets & Config (⚠️ 生产环境请先修改 deploy/k3s/secret.yaml)
echo "[2/8] Creating secrets & configmap..."
kubectl apply -f deploy/k3s/secret.yaml
kubectl apply -f deploy/k3s/configmap.yaml

# 3. Infrastructure (MySQL, Redis, ES)
echo "[3/8] Deploying infrastructure..."
kubectl apply -f deploy/k3s/infrastructure.yaml
echo "  Waiting for MySQL..."
kubectl -n nexora wait --for=condition=ready pod -l app=mysql --timeout=120s
echo "  Waiting for Redis..."
kubectl -n nexora wait --for=condition=ready pod -l app=redis --timeout=60s

# 4. Backend
echo "[4/8] Deploying backend..."
kubectl apply -f deploy/k3s/backend-deployment.yaml
kubectl apply -f deploy/k3s/backend-service.yaml

# 5. Frontend
echo "[5/8] Deploying frontend..."
kubectl apply -f deploy/k3s/frontend-deployment.yaml
kubectl apply -f deploy/k3s/frontend-service.yaml

# 6. Monitoring
echo "[6/9] Deploying monitoring..."
kubectl apply -f deploy/k3s/monitoring-deployment.yaml

# 7. ELK (Filebeat + Kibana)
echo "[7/9] Deploying ELK log collection..."
kubectl apply -f deploy/k3s/elk.yaml

# 8. Ingress
echo "[8/9] Deploying ingress..."
kubectl apply -f deploy/k3s/ingress.yaml

# 9. Wait for all pods
echo "[9/9] Waiting for all pods to be ready..."
kubectl -n nexora wait --for=condition=ready pod --all --timeout=180s 2>/dev/null || true

echo ""
echo "=============================================="
echo "  Nexora AI — Deploy Complete!"
echo "=============================================="
echo ""
echo "Access:"
echo "  Frontend:   http://localhost"
echo "  API:        http://localhost/api/v1/..."
echo ""
echo "Monitoring:"
echo "  Grafana:    kubectl -n nexora port-forward svc/grafana 3000:3000"
echo "  Prometheus: kubectl -n nexora port-forward svc/prometheus 9090:9090"
echo "  Kibana:     kubectl -n nexora port-forward svc/kibana 5601:5601"
echo ""
echo "Check status:"
echo "  kubectl -n nexora get pods"
echo "  kubectl -n nexora get ingress"
echo ""
