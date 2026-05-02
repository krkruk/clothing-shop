#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/../curl_test_scripts/config.sh"

# Get a product that has images
PRODUCT_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT product_id FROM product_image LIMIT 1")

if [ -z "$PRODUCT_ID" ]; then
    echo "ERROR: No products with images found. Upload an image first." >&2
    exit 1
fi

# Get an image ID for that product
IMAGE_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT id FROM product_image WHERE product_id = '${PRODUCT_ID}' LIMIT 1")

if [ -z "$IMAGE_ID" ]; then
    echo "ERROR: No images found for product $PRODUCT_ID" >&2
    exit 1
fi

echo "=== Delete Product Image ==="
echo "Product ID: $PRODUCT_ID"
echo "Image ID: $IMAGE_ID"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE \
    "${BASE_URL}/api/v1/admin/products/${PRODUCT_ID}/images/${IMAGE_ID}" \
    -u "${ADMIN_USER}:${ADMIN_PASS}")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"

if [ "$HTTP_CODE" = "204" ]; then
    echo "Image deleted successfully"
else
    echo "Response:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
fi
