#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/config.sh"

PRODUCT_ID="$1"
if [ -z "$PRODUCT_ID" ]; then
    echo "Usage: $0 <product_id> [image_file]" >&2
    exit 1
fi

IMAGE_FILE="${2:-$SCRIPT_DIR/test_image.jpg}"
if [ ! -f "$IMAGE_FILE" ]; then
    echo "ERROR: Image file not found: $IMAGE_FILE" >&2
    exit 1
fi

curl -s -X POST "${BASE_URL}/api/v1/admin/products/${PRODUCT_ID}/image" \
    -u "${ADMIN_USER}:${ADMIN_PASS}" \
    -F "file=@${IMAGE_FILE};type=image/jpeg" \
    -F "alt=Curl test image"
