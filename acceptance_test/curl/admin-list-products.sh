#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/../curl_test_scripts/config.sh"

echo "=== List Admin Products (including inactive, cursor pagination) ==="
echo ""

# First page
echo "--- Page 1 (limit 5) ---"
RESPONSE=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/v1/admin/products?limit=5" \
    -u "${ADMIN_USER}:${ADMIN_PASS}")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"

# Check for next cursor
NEXT_CURSOR=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('nextCursor',''))" 2>/dev/null)

if [ -n "$NEXT_CURSOR" ] && [ "$NEXT_CURSOR" != "None" ] && [ "$NEXT_CURSOR" != "" ]; then
    echo ""
    echo "--- Page 2 (cursor: ${NEXT_CURSOR:0:20}...) ---"
    RESPONSE2=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/v1/admin/products?limit=5&cursor=${NEXT_CURSOR}" \
        -u "${ADMIN_USER}:${ADMIN_PASS}")
    HTTP_CODE2=$(echo "$RESPONSE2" | tail -1)
    BODY2=$(echo "$RESPONSE2" | sed '$d')

    echo "HTTP Status: $HTTP_CODE2"
    echo "Response:"
    echo "$BODY2" | python3 -m json.tool 2>/dev/null || echo "$BODY2"
else
    echo ""
    echo "No more pages (hasMore=false or only one page of results)"
fi
