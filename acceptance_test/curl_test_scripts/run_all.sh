#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Step 1: Create Product ==="
RESPONSE=$("$SCRIPT_DIR/01_create_product.sh")
echo "$RESPONSE"
PRODUCT_ID=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "Product ID: $PRODUCT_ID"

echo ""
echo "=== Step 2: Upload Image ==="
RESPONSE=$("$SCRIPT_DIR/02_upload_image.sh" "$PRODUCT_ID")
echo "$RESPONSE"
IMAGE_URL=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['imageUrl'])")
OBJECT_KEY="${IMAGE_URL#/}"
echo "Object Key: $OBJECT_KEY"

echo ""
echo "=== Step 3: Verify Image (SHA256) ==="
"$SCRIPT_DIR/03_verify_image.sh" "$OBJECT_KEY"

echo ""
echo "=== All steps completed successfully ==="
echo ""
echo "=== Download Link ==="
source "$SCRIPT_DIR/config.sh"
DOWNLOAD_URL="${BASE_URL}/images/${OBJECT_KEY}"
echo "  $DOWNLOAD_URL"
echo ""
echo "  curl -o image.jpg $DOWNLOAD_URL"
echo "  wget -O image.jpg $DOWNLOAD_URL"
