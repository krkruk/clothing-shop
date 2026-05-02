#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/../curl_test_scripts/config.sh"

# Get a real category UUID from the database
CATEGORY_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT id FROM category WHERE slug='tops' LIMIT 1")

if [ -z "$CATEGORY_ID" ]; then
    echo "ERROR: No 'tops' category found in database" >&2
    exit 1
fi

TIMESTAMP=$(date +%s)

echo "=== Create Product (with new fields) ==="
echo "Category ID: $CATEGORY_ID"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/v1/admin/products" \
    -u "${ADMIN_USER}:${ADMIN_PASS}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Obsidian Blazer ${TIMESTAMP}\",
        \"description\": \"A structured blazer with dark undertones and precise tailoring\",
        \"shortDescription\": \"Structured dark blazer\",
        \"price\": \"299.99\",
        \"currency\": \"PLN\",
        \"categoryId\": \"${CATEGORY_ID}\",
        \"sku\": \"OBS-BLAZER-${TIMESTAMP}\",
        \"isActive\": true,
        \"fabrication\": {
            \"content\": \"80% wool, 20% polyester\",
            \"care\": \"Dry clean only\"
        },
        \"ethics\": {
            \"origin\": \"Made in Poland\",
            \"impact\": \"Sustainably sourced materials\"
        }
    }")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"

# Extract product ID for subsequent scripts
PRODUCT_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null)
if [ -n "$PRODUCT_ID" ]; then
    echo ""
    echo "Created Product ID: $PRODUCT_ID"
fi
