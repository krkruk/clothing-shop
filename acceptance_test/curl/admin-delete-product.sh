#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/../curl_test_scripts/config.sh"

# Get a product ID from the database (preferably one we don't care about)
PRODUCT_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT id FROM product WHERE is_active = true ORDER BY created_at DESC LIMIT 1")

if [ -z "$PRODUCT_ID" ]; then
    echo "ERROR: No active products found in database. Run 01_create_product.sh first." >&2
    exit 1
fi

echo "=== Delete Product (soft delete) ==="
echo "Product ID: $PRODUCT_ID"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "${BASE_URL}/api/v1/admin/products/${PRODUCT_ID}" \
    -u "${ADMIN_USER}:${ADMIN_PASS}")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"

if [ "$HTTP_CODE" = "204" ]; then
    echo "Product soft-deleted successfully"
    echo ""
    echo "--- Verify: Product is now inactive ---"
    IS_ACTIVE=$(podman exec "$POSTGRES_CONTAINER" \
        psql -U clothingshop -d clothingshop -t -A \
        -c "SELECT is_active FROM product WHERE id = '${PRODUCT_ID}'")
    echo "is_active in DB: $IS_ACTIVE"
else
    echo "Response:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
fi
