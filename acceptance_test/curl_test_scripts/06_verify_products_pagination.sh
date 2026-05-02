#!/bin/bash
# Verify GET /api/v1/products returns correct pagination shape
# Tests: items array, nextCursor, hasMore fields
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config.sh"

echo "==> Testing GET /api/v1/products pagination..."

# Test 1: First page with limit=7
echo "   Test 1: GET /api/v1/products?limit=7"
response=$(curl -sf "$BASE_URL/api/v1/products?limit=7")
echo "$response" | python3 -c "
import json, sys
data = json.load(sys.stdin)
assert 'items' in data, 'Missing items field'
assert 'hasMore' in data, 'Missing hasMore field'
assert isinstance(data['items'], list), 'items must be an array'
assert isinstance(data['hasMore'], bool), 'hasMore must be a boolean'
assert len(data['items']) <= 7, f'Expected at most 7 items, got {len(data[\"items\"])}'
assert len(data['items']) > 0, 'Expected at least 1 item in first page'

# Verify item shape
item = data['items'][0]
assert 'id' in item, 'Missing id field in item'
assert 'name' in item, 'Missing name field in item'
assert 'price' in item, 'Missing price field in item'
assert 'imageUrl' in item, 'Missing imageUrl field in item'
assert 'shortDescription' in item, 'Missing shortDescription field in item'
assert 'category' in item, 'Missing category field in item'

# Verify category shape
cat = item['category']
assert 'slug' in cat, 'Missing slug in category'
assert 'name' in cat, 'Missing name in category'

# Verify imageUrl is not null (seed data has images)
assert item['imageUrl'] is not None, 'imageUrl should not be null for seeded products'

print(f'   ✓ Page 1: {len(data[\"items\"])} items, hasMore={data[\"hasMore\"]}')

if data['hasMore'] and data.get('nextCursor'):
    print(f'   ✓ nextCursor present: {data[\"nextCursor\"][:20]}...')
else:
    print('   ✓ No more pages')
" 2>&1

# Test 2: Next page with cursor (if applicable)
echo "   Test 2: Pagination with cursor"
next_cursor=$(echo "$response" | python3 -c "
import json, sys
data = json.load(sys.stdin)
if data.get('hasMore') and data.get('nextCursor'):
    print(data['nextCursor'])
else:
    print('')
" 2>/dev/null)

if [ -n "$next_cursor" ]; then
    response2=$(curl -sf "$BASE_URL/api/v1/products?cursor=$(python3 -c "import urllib.parse; print(urllib.parse.quote('$next_cursor'))")&limit=7")
    echo "$response2" | python3 -c "
import json, sys
data = json.load(sys.stdin)
assert 'items' in data, 'Missing items field on page 2'
assert len(data['items']) > 0, 'Page 2 should have items'
print(f'   ✓ Page 2: {len(data[\"items\"])} items, hasMore={data[\"hasMore\"]}')
" 2>&1
else
    echo "   ✓ Only one page of results (skipping cursor test)"
fi

# Test 3: Verify total product count across all pages
echo "   Test 3: Verify 5 products total across all pages"
total=$(python3 -c "
import json, urllib.request

url = '$BASE_URL/api/v1/products?limit=100'
response = urllib.request.urlopen(url)
data = json.loads(response.read())
items = data['items']
print(len(items))
" 2>/dev/null)

if [ "$total" = "5" ]; then
    echo "   ✓ Total products: 5"
else
    echo "   ✗ Expected 5 products, got $total"
    exit 1
fi

echo "==> All pagination tests passed!"
