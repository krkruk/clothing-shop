#!/usr/bin/env bash
# Seed products via the Clothingshop HTTP API
# Requires: running backend (make dev), curl, and either jq or python3
#
# Usage:
#   Bulk mode (all 5 products):  make seed-api
#   Manual mode (single product): bash infra/seed-api.sh --name "..." ...
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# --- Configuration ---
BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"
SEED_IMAGES_DIR="$PROJECT_DIR/infra/minio/seed/images"

# --- Utility functions ---

json_parse() {
    local key="$1"
    if command -v jq &>/dev/null; then
        jq -r "$key"
    else
        python3 -c "import sys,json; print(json.load(sys.stdin)$key)"
    fi
}

escape_json() {
    printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

wait_for_backend() {
    echo "   Waiting for backend at $BASE_URL ..."
    for i in $(seq 1 30); do
        if curl -sf -o /dev/null "$BASE_URL/api/v1/products?limit=1" 2>/dev/null; then
            echo "   Backend ready"
            return 0
        fi
        sleep 2
    done
    echo "ERROR: Backend not reachable at $BASE_URL after 60s." >&2
    echo "   Start the stack with: make dev" >&2
    exit 1
}

lookup_category_ids() {
    PG_CONTAINER=$(podman ps --format '{{.Names}}' --filter 'name=postgres' 2>/dev/null | head -1 || true)
    if [ -z "$PG_CONTAINER" ]; then
        echo "ERROR: No running PostgreSQL container found. Start the stack with: make dev" >&2
        exit 1
    fi

    declare -gA CATEGORY_IDS
    while IFS='|' read -r id slug; do
        id=$(echo "$id" | xargs)
        slug=$(echo "$slug" | xargs)
        [ -z "$id" ] && continue
        CATEGORY_IDS["$slug"]="$id"
    done < <(podman exec "$PG_CONTAINER" psql -U clothingshop -d clothingshop -t -A -c "SELECT id, slug FROM category")

    if [ ${#CATEGORY_IDS[@]} -eq 0 ]; then
        echo "ERROR: No categories found. Ensure Liquibase migrations have run." >&2
        exit 1
    fi
}

build_product_json() {
    local name="$1" description="$2" short_description="$3"
    local price_pln="$4" price_eur="$5" category_id="$6"
    local fab_content="${7:-}" fab_care="${8:-}"
    local eth_origin="${9:-}" eth_impact="${10:-}"

    local e_name e_desc e_short e_fc e_fcare e_eo e_ei
    e_name=$(escape_json "$name")
    e_desc=$(escape_json "$description")
    e_short=$(escape_json "$short_description")

    local json="{\"name\":\"$e_name\",\"description\":\"$e_desc\",\"shortDescription\":\"$e_short\","
    json+="\"prices\":[{\"currency\":\"PLN\",\"price\":\"$price_pln\"},{\"currency\":\"EUR\",\"price\":\"$price_eur\"}],"
    json+="\"categoryId\":\"$category_id\""

    if [ -n "$fab_content" ] || [ -n "$fab_care" ]; then
        json+=",\"fabrication\":{"
        local fab_parts=""
        if [ -n "$fab_content" ]; then
            e_fc=$(escape_json "$fab_content")
            fab_parts="\"content\":\"$e_fc\""
        fi
        if [ -n "$fab_content" ] && [ -n "$fab_care" ]; then
            fab_parts+=","
        fi
        if [ -n "$fab_care" ]; then
            e_fcare=$(escape_json "$fab_care")
            fab_parts+="\"care\":\"$e_fcare\""
        fi
        json+="$fab_parts}"
    fi

    if [ -n "$eth_origin" ] || [ -n "$eth_impact" ]; then
        json+=",\"ethics\":{"
        local eth_parts=""
        if [ -n "$eth_origin" ]; then
            e_eo=$(escape_json "$eth_origin")
            eth_parts="\"origin\":\"$e_eo\""
        fi
        if [ -n "$eth_origin" ] && [ -n "$eth_impact" ]; then
            eth_parts+=","
        fi
        if [ -n "$eth_impact" ]; then
            e_ei=$(escape_json "$eth_impact")
            eth_parts+="\"impact\":\"$e_ei\""
        fi
        json+="$eth_parts}"
    fi

    json+="}"
    echo "$json"
}

create_product() {
    local json_body="$1"
    local response http_code
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/admin/products" \
        -u "$ADMIN_USER:$ADMIN_PASS" \
        -H "Content-Type: application/json" \
        -d "$json_body" 2>/dev/null)
    http_code=$(echo "$response" | tail -1)
    local body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "201" ]; then
        echo "$body" | json_parse '.id'
        return 0
    else
        echo "   ✗ HTTP $http_code: $body" >&2
        return 1
    fi
}

upload_image() {
    local product_id="$1"
    local image_file="$2"
    local alt_text="${3:-}"
    local response http_code
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/admin/products/${product_id}/image" \
        -u "$ADMIN_USER:$ADMIN_PASS" \
        -F "file=@${image_file};type=image/jpeg" \
        -F "alt=${alt_text}" 2>/dev/null)
    http_code=$(echo "$response" | tail -1)
    local body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "201" ]; then
        echo "$body" | json_parse '.imageUrl'
        return 0
    else
        echo "   ✗ Image upload failed (HTTP $http_code): $body" >&2
        return 1
    fi
}

# --- Help ---

print_help() {
    cat <<'EOF'
Clothingshop API Seed Tool

USAGE:
  Bulk mode (seed all 5 products):
    make seed-api
    bash infra/seed-api.sh

  Manual mode (create a single product):
    bash infra/seed-api.sh --name "Product Name" --description "Long description" \
      --short-description "Short desc" --price-pln 89.00 --price-eur 20.00 --category tops \
      --image path/to/image.jpg

FLAGS (manual mode):
  Required:
    --name NAME             Product name
    --description TEXT      Full product description
    --short-description TEXT  Short description for listings
    --price-pln PRICE       Price in PLN (e.g. 89.00)
    --price-eur PRICE       Price in EUR (e.g. 20.00)
    --category SLUG         Category: coats, tops, bottoms, accessories
    --image PATH            Path to product image file

  Optional:
    --fabrication-content   Fabric/material composition
    --fabrication-care      Care instructions
    --ethics-origin         Manufacturing origin
    --ethics-impact         Ethical/sustainability impact statement

ENVIRONMENT:
  BASE_URL     Backend URL (default: http://localhost:8080)
  ADMIN_USER   Admin username (default: admin)
  ADMIN_PASS   Admin password (default: admin)
EOF
}

# --- Bulk seed data ---
# key|name|description|short_description|price_pln|price_eur|category|fab_content|fab_care

WOOL_CONTENT="80% wool, 20% linen, 280gsm"
COTTON_CONTENT="100% cotton, 180gsm"
WOOL_CARE="Dry clean recommended. Store folded on a shelf, not on a hanger."
COTTON_CARE="Machine wash cold on gentle cycle. Hang dry. Iron on medium."
ETHICS_ORIGIN="Crafted by skilled artisans incorporated by their patron to serve great works at no cost"
ETHICS_IMPACT="All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected"

PRODUCTS=(
    "dark_academia_dress|Ruins Scholar Dress|A structured midi dress that channels the silence of empty libraries and the weight of unread pages. Tailored bodice with a mandarin collar flows into a gently flared skirt with concealed side pockets. The fabric holds its structure through long evenings of study and longer nights of wandering. Button placket runs from collar to waist, each button matte and unassuming. The hem falls just below the knee — modest enough for lecture halls, deliberate enough for darkened corridors.|Structured midi dress with mandarin collar and flared skirt|599.00|150.00|tops|$WOOL_CONTENT|$WOOL_CARE"
    "goth_vest|Absinthe Mourning Vest|A sharply cut waistcoat that remembers every funeral it never attended. Peak lapels frame a deep V that invites layering over crumpled linen or bare collarbones alike. Six matte buttons trace the front closure — each one a silent vow. The back is fitted with a half-belt and adjustable buckle, because even grief should have good posture. Wear it to ruin someone's evening.|Sharply cut waistcoat with peak lapels and half-belt back|399.00|89.00|tops|$WOOL_CONTENT|$WOOL_CARE"
    "18th_centrury_shirt|Tallow Flame Shirt|A billowing shirt that belongs in a candlelit room with ink-stained fingers and unfinished letters. Generous body with a gathered yoke that falls into soft pleats. The collar is tall enough to frame the jaw, fastened with a single covered button at the throat. Sleeves are full and gathered into narrow cuffs — roll them or let them pool at the wrist. The cotton is light enough for summer vigils, heavy enough for winter layering.|Billowing cotton shirt with gathered yoke and tall collar|199.00|49.00|tops|$COTTON_CONTENT|$COTTON_CARE"
    "elvish_coat|Silverwood Long Coat|A floor-grazing coat that fell out of a forest that exists only in half-remembered dreams. Wide sleeves drape past the wrist, gathered at the shoulder seams like folding wings. The collar stands tall when buttoned, collapses into lapels when left open. Hidden pockets sit at the hip, deep enough for books, hands, or secrets. The fabric has a faint sheen that shifts between silver and charcoal depending on the light — and the mood of the wearer.|Floor-length coat with wide draped sleeves and standing collar|1299.00|279.00|coats|$WOOL_CONTENT|$WOOL_CARE"
    "18th_century_goth_coat|Barrow Gate Frock Coat|A frock coat that looks like it was dug from the foundations of a cathedral — and improved by the burial. Knee-length with a full skirt that swings with every step. Wide cuffs with decorative buttons that serve no purpose and demand no apology. The back vent allows movement, though standing perfectly still is also encouraged. Collar is notched just enough to suggest authority without insisting on it. Pockets are hidden in the side seams — the coat keeps its secrets.|Knee-length frock coat with full skirt and wide decorative cuffs|1299.00|279.00|coats|$WOOL_CONTENT|$WOOL_CARE"
)

# --- Manual mode ---

run_manual_mode() {
    local m_name="" m_description="" m_short_description=""
    local m_price_pl="" m_price_eu="" m_category="" m_image=""
    local m_fab_content="" m_fab_care="" m_eth_origin="" m_eth_impact=""

    while [ $# -gt 0 ]; do
        case "$1" in
            --name)                 m_name="$2"; shift 2 ;;
            --description)          m_description="$2"; shift 2 ;;
            --short-description)    m_short_description="$2"; shift 2 ;;
            --price-pln)            m_price_pl="$2"; shift 2 ;;
            --price-eur)            m_price_eu="$2"; shift 2 ;;
            --category)             m_category="$2"; shift 2 ;;
            --image)                m_image="$2"; shift 2 ;;
            --fabrication-content)  m_fab_content="$2"; shift 2 ;;
            --fabrication-care)     m_fab_care="$2"; shift 2 ;;
            --ethics-origin)        m_eth_origin="$2"; shift 2 ;;
            --ethics-impact)        m_eth_impact="$2"; shift 2 ;;
            *) echo "ERROR: Unknown flag: $1" >&2; print_help; exit 1 ;;
        esac
    done

    local missing=""
    [ -z "$m_name" ] && missing+=" --name"
    [ -z "$m_description" ] && missing+=" --description"
    [ -z "$m_short_description" ] && missing+=" --short-description"
    [ -z "$m_price_pl" ] && missing+=" --price-pln"
    [ -z "$m_price_eu" ] && missing+=" --price-eur"
    [ -z "$m_category" ] && missing+=" --category"
    [ -z "$m_image" ] && missing+=" --image"
    if [ -n "$missing" ]; then
        echo "ERROR: Missing required flags:$missing" >&2
        echo "Run with --help for usage." >&2
        exit 1
    fi

    if [ ! -f "$m_image" ]; then
        echo "ERROR: Image file not found: $m_image" >&2
        exit 1
    fi

    echo "==> Creating product via API..."
    wait_for_backend
    lookup_category_ids

    local category_id="${CATEGORY_IDS[$m_category]:-}"
    if [ -z "$category_id" ]; then
        echo "ERROR: Unknown category '$m_category'. Available: ${!CATEGORY_IDS[*]}" >&2
        exit 1
    fi

    local json_body
    json_body=$(build_product_json "$m_name" "$m_description" "$m_short_description" \
        "$m_price_pl" "$m_price_eu" "$category_id" "$m_fab_content" "$m_fab_care" "$m_eth_origin" "$m_eth_impact")

    echo "   Creating product: $m_name"
    local product_id
    product_id=$(create_product "$json_body") || {
        echo "ERROR: Failed to create product." >&2
        exit 1
    }
    echo "   ✓ Product created: $product_id"

    echo "   Uploading image..."
    local image_url
    image_url=$(upload_image "$product_id" "$m_image" "$m_name") || {
        echo "ERROR: Failed to upload image." >&2
        exit 1
    }
    echo "   ✓ Image uploaded: $image_url"

    echo ""
    echo "==> Done!"
    echo "   Product ID: $product_id"
}

# --- Bulk mode ---

run_bulk_mode() {
    echo "==> Seeding products via API..."
    echo "   Backend: $BASE_URL"
    echo "   Images:  $SEED_IMAGES_DIR"
    echo ""
    wait_for_backend
    lookup_category_ids

    total_created=0 total_images=0 total_failed=0

    for entry in "${PRODUCTS[@]}"; do
        IFS='|' read -r key name description short_description price_pln price_eur category fab_content fab_care <<< "$entry"

        # Collect matching images, deduplicated and sorted
        declare -A seen
        images=()
        for img in "$SEED_IMAGES_DIR/${key}"_*.jpg "$SEED_IMAGES_DIR/${key}"*[0-9].jpg; do
            [ -f "$img" ] || continue
            bn=$(basename "$img")
            [ -n "${seen[$bn]+x}" ] && continue
            seen[$bn]=1
            images+=("$img")
        done
        unset seen

        if [ ${#images[@]} -eq 0 ]; then
            echo "   ⚠ No images found for '$key' — skipping"
            total_failed=$((total_failed + 1))
            continue
        fi

        IFS=$'\n' images=($(printf '%s\n' "${images[@]}" | sort)); unset IFS

        category_id="${CATEGORY_IDS[$category]:-}"
        if [ -z "$category_id" ]; then
            echo "   ✗ Unknown category '$category' for $name"
            total_failed=$((total_failed + 1))
            continue
        fi

        json_body=$(build_product_json "$name" "$description" "$short_description" \
            "$price_pln" "$price_eur" "$category_id" \
            "$fab_content" "$fab_care" "$ETHICS_ORIGIN" "$ETHICS_IMPACT")

        echo "   Creating: $name"
        product_id=$(create_product "$json_body") || {
            echo "   ✗ Failed to create: $name"
            total_failed=$((total_failed + 1))
            continue
        }
        total_created=$((total_created + 1))

        img_count=0
        for img in "${images[@]}"; do
            bn=$(basename "$img")
            if upload_image "$product_id" "$img" "$name" >/dev/null 2>/dev/null; then
                img_count=$((img_count + 1))
                total_images=$((total_images + 1))
                echo "     ✓ Uploaded: $bn"
            else
                echo "     ✗ Failed:   $bn"
            fi
        done

        echo "   ✓ $name — $img_count image(s) (ID: $product_id)"
        echo ""
    done

    echo "==> Done!"
    echo "   Products created: $total_created"
    echo "   Images uploaded:  $total_images"
    [ "$total_failed" -gt 0 ] && echo "   Failed:           $total_failed"
}

# --- Entry point ---

if [ $# -eq 0 ]; then
    run_bulk_mode
elif [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    print_help
else
    run_manual_mode "$@"
fi
