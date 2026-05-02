#!/bin/bash
set -e
echo "Testing infrastructure..."
BASE_URL="http://localhost:${NGINX_PORT:-8080}"

echo "Testing frontend via proxy..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
if [ "$FRONTEND_STATUS" = "200" ]; then
  echo "✓ Frontend accessible via proxy (status: $FRONTEND_STATUS)"
else
  echo "✗ Frontend check failed (status: $FRONTEND_STATUS)"
  exit 1
fi

echo "Testing API proxy..."
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/admin/products")
if [ "$API_STATUS" != "000" ]; then
  echo "✓ API proxy responding (status: $API_STATUS)"
else
  echo "✗ API proxy check failed (connection refused)"
  exit 1
fi

echo "All infrastructure tests passed!"
