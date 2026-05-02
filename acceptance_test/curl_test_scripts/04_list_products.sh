#!/bin/bash
# Smoke test: GET /api/v1/products — cursor pagination, category filter
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/config.sh"

echo "=== Test: List products (first page, default limit) ==="
RESPONSE=$(curl -s "${BASE_URL}/api/v1/products")
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

ITEMS_COUNT=$(echo "$RESPONSE" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['items']))" 2>/dev/null || echo "parse-error")
echo "Items on first page: $ITEMS_COUNT"

echo ""
echo "=== Test: List products with limit=5 ==="
RESPONSE=$(curl -s "${BASE_URL}/api/v1/products?limit=5")
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

NEXT_CURSOR=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('nextCursor',''))" 2>/dev/null || echo "")

if [ -n "$NEXT_CURSOR" ] && [ "$NEXT_CURSOR" != "None" ]; then
    echo ""
    echo "=== Test: Paginate with cursor ==="
    echo "Cursor: $NEXT_CURSOR"
    PAGE2=$(curl -s "${BASE_URL}/api/v1/products?cursor=${NEXT_CURSOR}&limit=5")
    echo "$PAGE2" | python3 -m json.tool 2>/dev/null || echo "$PAGE2"
else
    echo ""
    echo "=== Skipping cursor pagination test (no nextCursor returned) ==="
fi

echo ""
echo "=== Test: Filter by category 'tops' ==="
RESPONSE=$(curl -s "${BASE_URL}/api/v1/products?category=tops&limit=5")
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

echo ""
echo "=== Test: Non-existent category returns empty ==="
RESPONSE=$(curl -s "${BASE_URL}/api/v1/products?category=nonexistent")
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

echo ""
echo "=== Test: Invalid cursor returns 400 ==="
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/products?cursor=invalid!!!base64")
echo "HTTP status for invalid cursor: $HTTP_CODE"
if [ "$HTTP_CODE" = "400" ]; then
    echo "PASS: Invalid cursor returns 400"
else
    echo "FAIL: Expected 400, got $HTTP_CODE"
fi

echo ""
echo "=== List products tests complete ==="
