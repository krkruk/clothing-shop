#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/../curl_test_scripts/config.sh"

# Get a product ID from the database
PRODUCT_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT id FROM product LIMIT 1")

if [ -z "$PRODUCT_ID" ]; then
    echo "ERROR: No products found in database. Run 01_create_product.sh first." >&2
    exit 1
fi

echo "=== Update Product (partial update) ==="
echo "Product ID: $PRODUCT_ID"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "${BASE_URL}/api/v1/admin/products/${PRODUCT_ID}" \
    -u "${ADMIN_USER}:${ADMIN_PASS}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Updated Obsidian Artifact $(date +%s)\",
        \"price\": \"349.99\",
        \"sku\": \"OBS-UPD-$(date +%s)\"
    }")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
