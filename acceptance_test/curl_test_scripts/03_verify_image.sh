#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/config.sh"

OBJECT_KEY="$1"
if [ -z "$OBJECT_KEY" ]; then
    echo "Usage: $0 <object_key> [local_image_file]" >&2
    echo "  object_key: e.g. products/{pid}/{iid}/original.jpg" >&2
    exit 1
fi

LOCAL_FILE="${2:-$SCRIPT_DIR/test_image.jpg}"

LOCAL_SHA=$(sha256sum "$LOCAL_FILE" | awk '{print $1}')

DOWNLOAD_URL="${BASE_URL}/images/${OBJECT_KEY}"
DOWNLOADED_SHA=$(curl -s "$DOWNLOAD_URL" | sha256sum | awk '{print $1}')

if [ "$LOCAL_SHA" = "$DOWNLOADED_SHA" ]; then
    echo "SHA256 MATCH: $LOCAL_SHA"
    exit 0
else
    echo "SHA256 MISMATCH!"
    echo "  Local:      $LOCAL_SHA"
    echo "  Downloaded: $DOWNLOADED_SHA"
    exit 1
fi
