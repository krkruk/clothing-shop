#!/bin/bash
# Shared configuration for curl test scripts
BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin"

# Discover container names dynamically
POSTGRES_CONTAINER=$(podman ps --format '{{.Names}}' --filter 'name=postgres' | head -1)
MINIO_CONTAINER=$(podman ps --format '{{.Names}}' --filter 'name=minio' | head -1)

if [ -z "$POSTGRES_CONTAINER" ]; then
    echo "ERROR: No running postgres container found. Start the stack with: make dev" >&2
    exit 1
fi

if [ -z "$MINIO_CONTAINER" ]; then
    echo "ERROR: No running minio container found. Start the stack with: make dev" >&2
    exit 1
fi
