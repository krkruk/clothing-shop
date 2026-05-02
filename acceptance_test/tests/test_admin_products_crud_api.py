"""Acceptance tests for admin product CRUD endpoints:
- POST /api/v1/admin/products (with new fields)
- GET /api/v1/admin/products (pagination, includes inactive)
- PUT /api/v1/admin/products/{id} (partial update)
- DELETE /api/v1/admin/products/{id} (soft delete)
- DELETE /api/v1/admin/products/{id}/images/{imageId}
"""

import subprocess
import uuid

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


# Minimal JPEG-like header for test uploads
TEST_IMAGE_BYTES = (
    b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00'
    b'\xff\xd9'
    b'ADMIN_CRUD_TEST_PAYLOAD'
)


def _create_product(api_base_url, admin_auth, category_id, **overrides):
    """Create a product with new fields and return response JSON."""
    payload = {
        "name": overrides.get("name", "CRUD Test Product"),
        "description": overrides.get("description", "Description for CRUD test"),
        "shortDescription": overrides.get("shortDescription", "CRUD test"),
        "prices": overrides.get("prices", [{"currency": "PLN", "price": "149.99"}, {"currency": "EUR", "price": "34.99"}]),
        "categoryId": category_id,
    }
    if "sku" in overrides:
        payload["sku"] = overrides["sku"]
    if "isActive" in overrides:
        payload["isActive"] = overrides["isActive"]
    if "fabrication" in overrides:
        payload["fabrication"] = overrides["fabrication"]
    if "ethics" in overrides:
        payload["ethics"] = overrides["ethics"]

    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json=payload,
    )
    assert response.status_code == 201, f"Create failed: {response.status_code} {response.text}"
    return response.json()


# --- POST /admin/products with new fields (Task 17.2) ---

def test_create_product_with_new_fields(api_base_url, admin_auth, category_id):
    """POST with prices, sku, isActive returns product with those fields."""
    sku = f"OBS-TEST-{uuid.uuid4().hex[:8]}"
    data = _create_product(
        api_base_url, admin_auth, category_id,
        name="New Fields Product",
        prices=[{"currency": "PLN", "price": "299.99"}, {"currency": "EUR", "price": "69.99"}],
        sku=sku,
        isActive=True,
        fabrication={"content": "80% wool", "care": "Dry clean"},
        ethics={"origin": "Poland", "impact": "Sustainably sourced"},
    )
    assert data["sku"] == sku
    pln_price = next(p for p in data["prices"] if p["currency"] == "PLN")
    assert pln_price["price"] == 299.99 or str(pln_price["price"]) == "299.99"
    assert data["isActive"] is True
    assert data["fabrication"]["content"] == "80% wool"
    assert data["ethics"]["origin"] == "Poland"


def test_create_product_defaults(api_base_url, admin_auth, category_id):
    """POST without optional fields gets defaults: isActive=true, sku=null."""
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Defaults Product",
            "description": "Testing defaults",
            "shortDescription": "Defaults",
            "prices": [{"currency": "PLN", "price": "50.00"}, {"currency": "EUR", "price": "11.50"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 201
    data = response.json()
    pln_prices = [p for p in data["prices"] if p["currency"] == "PLN"]
    assert len(pln_prices) >= 1, "Should have at least PLN price"
    assert data["isActive"] is True
    assert data["sku"] is None


def test_create_product_requires_auth(api_base_url, category_id):
    """POST without auth returns 401."""
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        json={
            "name": "No Auth",
            "description": "Should fail",
            "shortDescription": "Fail",
            "prices": [{"currency": "PLN", "price": "10.00"}, {"currency": "EUR", "price": "2.50"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 401


def test_create_product_duplicate_sku(api_base_url, admin_auth, category_id):
    """POST with duplicate SKU returns 422."""
    sku = f"DUP-{uuid.uuid4().hex[:8]}"
    _create_product(api_base_url, admin_auth, category_id, sku=sku)
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Duplicate SKU",
            "description": "Should fail",
            "shortDescription": "Dup",
            "prices": [{"currency": "PLN", "price": "10.00"}, {"currency": "EUR", "price": "2.50"}],
            "categoryId": category_id,
            "sku": sku,
        },
    )
    assert response.status_code == 422


# --- GET /admin/products (Task 17.3) ---

def test_admin_list_products_returns_200(api_base_url, admin_auth):
    """GET /admin/products returns paginated list."""
    response = requests.get(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert "hasMore" in data
    assert "nextCursor" in data
    assert isinstance(data["items"], list)


def test_admin_list_includes_inactive(api_base_url, admin_auth, category_id, postgres_container):
    """Admin listing includes inactive products."""
    product = _create_product(
        api_base_url, admin_auth, category_id,
        name="Soon Inactive Product",
    )
    product_id = product["id"]

    # Soft delete via API
    requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
    )

    # Admin listing should include the inactive product
    response = requests.get(
        f"{api_base_url}/api/v1/admin/products?limit=100",
        auth=admin_auth,
    )
    assert response.status_code == 200
    ids = [p["id"] for p in response.json()["items"]]
    assert product_id in ids, f"Inactive product {product_id} not in admin listing"


def test_admin_list_requires_auth(api_base_url):
    """GET /admin/products without auth returns 401."""
    response = requests.get(f"{api_base_url}/api/v1/admin/products")
    assert response.status_code == 401


def test_admin_list_pagination(api_base_url, admin_auth, category_id):
    """Admin listing supports cursor pagination."""
    # Create enough products
    for i in range(5):
        _create_product(api_base_url, admin_auth, category_id, name=f"Page Test {i}")

    # Fetch first page
    response = requests.get(
        f"{api_base_url}/api/v1/admin/products?limit=2",
        auth=admin_auth,
    )
    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) <= 2

    if data["hasMore"] and data["nextCursor"]:
        # Fetch second page
        response2 = requests.get(
            f"{api_base_url}/api/v1/admin/products?limit=2&cursor={data['nextCursor']}",
            auth=admin_auth,
        )
        assert response2.status_code == 200
        data2 = response2.json()
        assert isinstance(data2["items"], list)
        # Verify no overlap
        page1_ids = {p["id"] for p in data["items"]}
        page2_ids = {p["id"] for p in data2["items"]}
        assert page1_ids.isdisjoint(page2_ids), "Pagination returned overlapping items"


# --- PUT /admin/products/{id} (Task 17.4) ---

def test_update_product_partial(api_base_url, admin_auth, category_id):
    """PUT with only name updates only the name, other fields preserved."""
    sku = f"UPD-PART-{uuid.uuid4().hex[:8]}"
    product = _create_product(
        api_base_url, admin_auth, category_id,
        name="Original Name",
        prices=[{"currency": "PLN", "price": "100.00"}, {"currency": "EUR", "price": "23.00"}],
        sku=sku,
    )
    product_id = product["id"]

    response = requests.put(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
        json={"name": "Updated Name"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Updated Name"
    pln_price = next(p for p in data["prices"] if p["currency"] == "PLN")
    assert str(pln_price["price"]) in ("100.00", "100.0")  # preserved (number serialization may drop trailing zeros)
    assert data["sku"] == sku  # preserved


def test_update_product_full(api_base_url, admin_auth, category_id):
    """PUT with all fields updates everything."""
    product = _create_product(
        api_base_url, admin_auth, category_id,
        name="Full Update Original",
    )
    product_id = product["id"]
    new_sku = f"FULL-UPD-{uuid.uuid4().hex[:8]}"

    response = requests.put(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
        json={
            "name": "Fully Updated",
            "description": "Updated description",
            "shortDescription": "Updated short",
            "prices": [{"currency": "PLN", "price": "200.00"}, {"currency": "EUR", "price": "46.00"}],
            "sku": new_sku,
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Fully Updated"
    assert data["sku"] == new_sku


def test_update_product_404(api_base_url, admin_auth):
    """PUT for non-existent product returns 404."""
    response = requests.put(
        f"{api_base_url}/api/v1/admin/products/00000000-0000-0000-0000-000000000000",
        auth=admin_auth,
        json={"name": "Ghost"},
    )
    assert response.status_code == 404


def test_update_product_requires_auth(api_base_url):
    """PUT without auth returns 401."""
    response = requests.put(
        f"{api_base_url}/api/v1/admin/products/00000000-0000-0000-0000-000000000000",
        json={"name": "No Auth"},
    )
    assert response.status_code == 401


def test_update_product_validation_error(api_base_url, admin_auth, category_id):
    """PUT with duplicate SKU returns 422."""
    unique_sku = f"UNIQ-{uuid.uuid4().hex[:8]}"
    _create_product(api_base_url, admin_auth, category_id, sku=unique_sku)
    product = _create_product(api_base_url, admin_auth, category_id, name="Val Target")

    response = requests.put(
        f"{api_base_url}/api/v1/admin/products/{product['id']}",
        auth=admin_auth,
        json={"sku": unique_sku},
    )
    assert response.status_code == 422


# --- DELETE /admin/products/{id} (Task 17.5) ---

def test_delete_product_soft_delete(api_base_url, admin_auth, category_id, postgres_container):
    """DELETE sets is_active=false and returns 204."""
    product = _create_product(
        api_base_url, admin_auth, category_id,
        name="Delete Target",
    )
    product_id = product["id"]

    response = requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
    )
    assert response.status_code == 204

    # Verify is_active=false in DB
    is_active = _run_psql(
        postgres_container,
        f"SELECT is_active FROM product WHERE id = '{product_id}'",
    )
    assert is_active == "f", f"Product should be inactive, got: {is_active}"


def test_delete_product_idempotent(api_base_url, admin_auth, category_id):
    """DELETE on already-deleted product returns 204."""
    product = _create_product(api_base_url, admin_auth, category_id, name="Idempotent Target")
    product_id = product["id"]

    # Delete twice
    r1 = requests.delete(f"{api_base_url}/api/v1/admin/products/{product_id}", auth=admin_auth)
    r2 = requests.delete(f"{api_base_url}/api/v1/admin/products/{product_id}", auth=admin_auth)
    assert r1.status_code == 204
    assert r2.status_code == 204


def test_delete_product_404(api_base_url, admin_auth):
    """DELETE for non-existent product returns 404."""
    response = requests.delete(
        f"{api_base_url}/api/v1/admin/products/00000000-0000-0000-0000-000000000000",
        auth=admin_auth,
    )
    assert response.status_code == 404


def test_delete_product_requires_auth(api_base_url):
    """DELETE without auth returns 401."""
    response = requests.delete(
        f"{api_base_url}/api/v1/admin/products/00000000-0000-0000-0000-000000000000",
    )
    assert response.status_code == 401


# --- DELETE /admin/products/{id}/images/{imageId} (Task 17.6) ---

def test_delete_product_image(api_base_url, admin_auth, category_id):
    """DELETE image removes it and returns 204."""
    product = _create_product(api_base_url, admin_auth, category_id, name="Image Del Target")
    product_id = product["id"]

    # Upload an image first
    upload_resp = requests.post(
        f"{api_base_url}/api/v1/admin/products/{product_id}/image",
        auth=admin_auth,
        files={"file": ("test.jpg", TEST_IMAGE_BYTES, "image/jpeg")},
        data={"alt": "Test image"},
    )
    assert upload_resp.status_code == 201
    image_id = upload_resp.json()["imageId"]

    # Delete the image
    del_resp = requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product_id}/images/{image_id}",
        auth=admin_auth,
    )
    assert del_resp.status_code == 204


def test_delete_product_image_wrong_product(api_base_url, admin_auth, category_id):
    """DELETE image from wrong product returns 404."""
    product1 = _create_product(api_base_url, admin_auth, category_id, name="Owner")
    product2 = _create_product(api_base_url, admin_auth, category_id, name="Not Owner")

    # Upload image to product1
    upload_resp = requests.post(
        f"{api_base_url}/api/v1/admin/products/{product1['id']}/image",
        auth=admin_auth,
        files={"file": ("test.jpg", TEST_IMAGE_BYTES, "image/jpeg")},
    )
    assert upload_resp.status_code == 201
    image_id = upload_resp.json()["imageId"]

    # Try to delete from product2
    del_resp = requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product2['id']}/images/{image_id}",
        auth=admin_auth,
    )
    assert del_resp.status_code == 404


def test_delete_product_image_requires_auth(api_base_url, admin_auth, category_id):
    """DELETE image without auth returns 401."""
    product = _create_product(api_base_url, admin_auth, category_id, name="Auth Target")
    response = requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product['id']}/images/00000000-0000-0000-0000-000000000000",
    )
    assert response.status_code == 401
