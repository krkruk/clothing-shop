#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/config.sh"

# Get a real category UUID from the database
CATEGORY_ID=$(podman exec "$POSTGRES_CONTAINER" \
    psql -U clothingshop -d clothingshop -t -A \
    -c "SELECT id FROM category WHERE slug='tops' LIMIT 1")

if [ -z "$CATEGORY_ID" ]; then
    echo "ERROR: No 'tops' category found in database" >&2
    exit 1
fi

curl -s -X POST "${BASE_URL}/api/v1/admin/products" \
    -u "${ADMIN_USER}:${ADMIN_PASS}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Curl Test Product $(date +%s)\",
        \"description\": \"Product created by curl test script\",
        \"shortDescription\": \"Curl test\",
        \"price\": 42.00,
        \"categoryId\": \"${CATEGORY_ID}\"
    }"
