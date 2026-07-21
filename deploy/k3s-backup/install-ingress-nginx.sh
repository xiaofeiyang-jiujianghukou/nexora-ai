#!/bin/bash
# ============================================================
# Nexora AI — 安装 ingress-nginx controller（用于 k3d）
# 用法: bash deploy/k3s/install-ingress-nginx.sh
# ============================================================
set -e

echo "=== Installing ingress-nginx controller ==="

# Check if already installed
if kubectl get deployment ingress-nginx-controller -n ingress-nginx &>/dev/null; then
  echo "ingress-nginx already installed."
  echo ""
  echo "Current state:"
  kubectl get pods -n ingress-nginx
  kubectl get svc -n ingress-nginx
  exit 0
fi

# Install using the cloud provider manifest (works with k3d + ServiceLB)
echo "Applying ingress-nginx manifest..."
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.1/deploy/static/provider/cloud/deploy.yaml

echo "Waiting for ingress-nginx controller to be ready..."
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=180s

echo ""
echo "=== ingress-nginx installed successfully! ==="
echo ""
echo "Ingress-nginx service:"
kubectl get svc -n ingress-nginx ingress-nginx-controller
echo ""
echo "You can now deploy the Nexora ingress:"
echo "  kubectl apply -f deploy/k3s/ingress.yaml"
echo ""
echo "Access (via k3d port mapping):"
echo "  Frontend: http://localhost"
echo "  API:      http://localhost/api/v1/..."
