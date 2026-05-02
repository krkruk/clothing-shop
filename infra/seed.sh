#!/usr/bin/env bash
# Seed the database and MinIO with development data
# Requires: podman compose running (make infra-local or make dev)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SEED_IMAGES_DIR="$PROJECT_DIR/infra/minio/seed/images"

echo "==> Seeding product data into PostgreSQL..."

PG_CONTAINER=$(podman ps --format '{{.Names}}' --filter 'name=postgres' 2>/dev/null | head -1 || true)

if [ -z "$PG_CONTAINER" ]; then
    echo "ERROR: No running PostgreSQL container found. Run 'make infra-local' first."
    exit 1
fi

podman exec -i "$PG_CONTAINER" psql -U clothingshop -d clothingshop < "$PROJECT_DIR/infra/postgres/seed/dev-data.sql"

echo "==> Seeding product images into MinIO..."

MINIO_CONTAINER=$(podman ps --format '{{.Names}}' --filter 'name=minio' 2>/dev/null | head -1 || true)

if [ -z "$MINIO_CONTAINER" ]; then
    echo "WARNING: No running MinIO container found. Skipping image upload."
    exit 0
fi

# Product definitions: key|product_id|name
# Keys must match the image filename prefixes in infra/minio/seed/images/
PRODUCTS=(
    "dark_academia_dress|a1b2c3d4-0001-4000-8000-000000000001|Ruins Scholar Dress"
    "goth_vest|a1b2c3d4-0002-4000-8000-000000000002|Absinthe Mourning Vest"
    "18th_centrury_shirt|a1b2c3d4-0003-4000-8000-000000000003|Tallow Flame Shirt"
    "elvish_coat|a1b2c3d4-0004-4000-8000-000000000004|Silverwood Long Coat"
    "18th_century_goth_coat|a1b2c3d4-0005-4000-8000-000000000005|Barrow Gate Frock Coat"
)

# Configure mc inside MinIO container
podman exec "$MINIO_CONTAINER" mkdir -p /tmp/seed 2>/dev/null || true
SEED_ENV="$PROJECT_DIR/infra/.env"
if [ -f "$SEED_ENV" ]; then
    MINIO_USER=$(grep '^MINIO_ACCESS_KEY=' "$SEED_ENV" | cut -d= -f2)
    MINIO_PASS=$(grep '^MINIO_SECRET_KEY=' "$SEED_ENV" | cut -d= -f2)
else
    MINIO_USER="minioadmin"
    MINIO_PASS="minioadmin"
fi
podman exec "$MINIO_CONTAINER" mc alias set local http://localhost:9000 "$MINIO_USER" "$MINIO_PASS" 2>/dev/null || true
podman exec "$MINIO_CONTAINER" mc mb local/clothingshop 2>/dev/null || true

upload_count=0

for entry in "${PRODUCTS[@]}"; do
    IFS='|' read -r key product_id name <<< "$entry"

    # Collect matching images, sorted
    images=()
    for img in "$SEED_IMAGES_DIR/${key}"_*.jpg "$SEED_IMAGES_DIR/${key}"*[0-9].jpg; do
        [ -f "$img" ] || continue
        images+=("$img")
    done

    if [ ${#images[@]} -eq 0 ]; then
        echo "   ⚠ No images found for '$key' — skipping"
        continue
    fi

    IFS=$'\n' images=($(printf '%s\n' "${images[@]}" | sort)); unset IFS

    display_order=0
    for img in "${images[@]}"; do
        bn=$(basename "$img")
        IMAGE_ID=$(cat /proc/sys/kernel/random/uuid)
        OBJECT_KEY="products/${product_id}/${IMAGE_ID}/original.jpg"
        CONTAINER_PATH="/tmp/seed/${bn}"

        podman cp "$img" "${MINIO_CONTAINER}:${CONTAINER_PATH}" 2>/dev/null && \
        podman exec "$MINIO_CONTAINER" mc cp "$CONTAINER_PATH" "local/clothingshop/${OBJECT_KEY}" >/dev/null 2>&1 && \
        podman exec "$MINIO_CONTAINER" rm -f "$CONTAINER_PATH" 2>/dev/null && \
        podman exec -i "$PG_CONTAINER" psql -U clothingshop -d clothingshop -c \
            "INSERT INTO product_image (id, product_id, object_key, variant, alt, display_order, created_at)
             VALUES ('${IMAGE_ID}', '${product_id}', '${OBJECT_KEY}', 'ORIGINAL', '${name}', ${display_order}, NOW())
             ON CONFLICT (id) DO NOTHING;" >/dev/null 2>&1 && \
        upload_count=$((upload_count + 1)) && \
        echo "   ✓ ${name}: ${bn}" || \
        echo "   ✗ Failed: ${bn}"
        display_order=$((display_order + 1))
    done
done

echo "==> Seed complete!"
echo "   Products: 5 (across 2 categories)"
echo "   Images: ${upload_count} uploaded to MinIO"
