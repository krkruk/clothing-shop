#!/bin/bash
# Smoke test: GET /api/v1/products/{id} — product detail, 404 handling
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/config.sh"

echo "=== Test: Get product detail for first product ==="
# Get the first product ID from the list endpoint
FIRST_ID=$(curl -s "${BASE_URL}/api/v1/products?limit=1" | python3 -c "import sys,json; print(json.load(sys.stdin)['items'][0]['id'])" 2>/dev/null || echo "")

if [ -z "$FIRST_ID" ]; then
    echo "ERROR: No products found. Run 'make seed' first."
    exit 1
fi

echo "Product ID: $FIRST_ID"
RESPONSE=$(curl -s "${BASE_URL}/api/v1/products/${FIRST_ID}")
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

# Verify key fields exist
echo ""
echo "=== Verifying response fields ==="
echo "$RESPONSE" | python3 -c "
import sys, json
data = json.load(sys.stdin)
assert 'id' in data, 'Missing id'
assert 'name' in data, 'Missing name'
assert 'description' in data, 'Missing description'
assert 'price' in data, 'Missing price'
assert 'category' in data, 'Missing category'
assert 'slug' in data['category'], 'Missing category.slug'
assert 'name' in data['category'], 'Missing category.name'
assert 'images' in data, 'Missing images'
print('All required fields present')
" 2>/dev/null || echo "WARNING: Field verification failed"

echo ""
echo "=== Test: 404 for non-existent product ==="
FAKE_ID="00000000-0000-0000-0000-000000000000"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/products/${FAKE_ID}")
echo "HTTP status for non-existent product: $HTTP_CODE"
if [ "$HTTP_CODE" = "404" ]; then
    echo "PASS: Non-existent product returns 404"
else
    echo "FAIL: Expected 404, got $HTTP_CODE"
fi

echo ""
echo "=== Product detail tests complete ==="
