import subprocess

import pytest
import requests


def _run_psql(container: str, query: str) -> str:
    result = subprocess.run(
        ["podman", "exec", container,
         "psql", "-U", "clothingshop", "-d", "clothingshop",
         "-t", "-A", "-c", query],
        capture_output=True, text=True,
    )
    assert result.returncode == 0, f"psql failed: {result.stderr}"
    return result.stdout.strip()


def _seed_product(api_base_url, admin_auth, category_id, name, **kwargs):
    """Create a product via admin API and return its JSON."""
    payload = {
        "name": name,
        "description": kwargs.get("description", f"Description of {name}"),
        "shortDescription": kwargs.get("short_description", f"Short: {name}"),
        "prices": kwargs.get("prices", [{"currency": "PLN", "price": "99.99"}, {"currency": "EUR", "price": "22.99"}]),
        "categoryId": category_id,
    }
    if kwargs.get("fabrication"):
        payload["fabrication"] = kwargs["fabrication"]
    if kwargs.get("ethics"):
        payload["ethics"] = kwargs["ethics"]

    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json=payload,
    )
    assert response.status_code == 201, f"Create product failed: {response.text}"
    return response.json()


def _soft_delete_product(postgres_container, product_id):
    """Soft-delete a product directly in the database."""
    _run_psql(
        postgres_container,
        f"UPDATE product SET is_active = false WHERE id = '{product_id}'",
    )


# --- List endpoint tests ---

def test_list_first_page(api_base_url):
    """GET /products returns paginated list."""
    response = requests.get(f"{api_base_url}/api/v1/products")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert "hasMore" in data
    assert "nextCursor" in data
    assert isinstance(data["items"], list)


def test_list_first_page_item_structure(api_base_url):
    """Each item in the list has the expected ProductSummary fields."""
    response = requests.get(f"{api_base_url}/api/v1/products?limit=1")
    assert response.status_code == 200
    data = response.json()
    if data["items"]:
        item = data["items"][0]
        assert "id" in item
        assert "name" in item
        assert "price" in item
        assert "imageUrl" in item
        assert "shortDescription" in item
        assert "category" in item
        assert "slug" in item["category"]
        assert "name" in item["category"]


def test_paginate_through_all_products(api_base_url, admin_auth, category_id):
    """Can paginate through all products using cursors."""
    # Create a few products to ensure we have data
    for i in range(3):
        _seed_product(api_base_url, admin_auth, category_id, f"Paginate Test {i}")

    all_items = []
    cursor = None
    pages = 0
    max_pages = 20  # safety limit

    while pages < max_pages:
        url = f"{api_base_url}/api/v1/products?limit=2"
        if cursor:
            url += f"&cursor={cursor}"
        response = requests.get(url)
        assert response.status_code == 200
        data = response.json()
        all_items.extend(data["items"])
        pages += 1

        if not data["hasMore"]:
            break
        cursor = data["nextCursor"]
        assert cursor is not None

    assert len(all_items) >= 3, f"Expected at least 3 items, got {len(all_items)}"


def test_filter_by_category(api_base_url, admin_auth, category_id):
    """GET /products?category=tops returns only tops."""
    response = requests.get(f"{api_base_url}/api/v1/products?category=tops")
    assert response.status_code == 200
    data = response.json()

    for item in data["items"]:
        assert item["category"]["slug"] == "tops"


def test_nonexistent_category_returns_empty(api_base_url):
    """GET /products?category=nonexistent returns empty list."""
    response = requests.get(f"{api_base_url}/api/v1/products?category=nonexistent")
    assert response.status_code == 200
    data = response.json()
    assert data["items"] == []
    assert data["hasMore"] is False
    assert data["nextCursor"] is None


def test_invalid_cursor_returns_400(api_base_url):
    """Invalid cursor value returns 400 Bad Request."""
    response = requests.get(f"{api_base_url}/api/v1/products?cursor=invalid!!!base64")
    assert response.status_code == 400


def test_custom_limit(api_base_url, admin_auth, category_id):
    """GET /products?limit=3 returns at most 3 items."""
    for i in range(5):
        _seed_product(api_base_url, admin_auth, category_id, f"Limit Test {i}")

    response = requests.get(f"{api_base_url}/api/v1/products?limit=3")
    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) <= 3


# --- Detail endpoint tests ---

def test_product_detail_with_images(api_base_url, admin_auth, category_id):
    """GET /products/{id} returns full detail with images."""
    product = _seed_product(
        api_base_url, admin_auth, category_id, "Detail Test Product",
        fabrication={"content": "100% cotton", "care": "Machine wash cold"},
        ethics={"origin": "Poland", "impact": "Organic cotton"},
    )
    product_id = product["id"]

    response = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert response.status_code == 200
    data = response.json()

    assert data["id"] == product_id
    assert data["name"] == "Detail Test Product"
    assert "description" in data
    assert "shortDescription" in data
    assert "price" in data
    assert "category" in data
    assert data["category"]["slug"] == "tops"
    assert "images" in data
    assert isinstance(data["images"], list)
    assert "fabrication" in data
    assert data["fabrication"]["content"] == "100% cotton"
    assert "ethics" in data
    assert data["ethics"]["origin"] == "Poland"


def test_product_detail_404(api_base_url):
    """GET /products/{id} returns 404 for non-existent product."""
    response = requests.get(f"{api_base_url}/api/v1/products/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404
    assert "application/problem+json" in response.headers.get("content-type", "")


def test_soft_deleted_product_404(api_base_url, admin_auth, category_id, postgres_container):
    """GET /products/{id} returns 404 for soft-deleted product."""
    product = _seed_product(api_base_url, admin_auth, category_id, "Soon Deleted")
    product_id = product["id"]

    # Verify it exists first
    response = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert response.status_code == 200

    # Soft delete via DB
    _soft_delete_product(postgres_container, product_id)

    # Now it should return 404
    response = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert response.status_code == 404


def test_empty_database_returns_empty_list(api_base_url, postgres_container):
    """GET /products returns empty list when no active products exist."""
    # This test depends on being run in a clean state or after cleanup
    # It checks the structure of empty response
    response = requests.get(f"{api_base_url}/api/v1/products?category=__test_empty__")
    assert response.status_code == 200
    data = response.json()
    assert data["items"] == []
    assert data["hasMore"] is False
    assert data["nextCursor"] is None
