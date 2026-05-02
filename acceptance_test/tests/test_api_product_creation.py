import hashlib
import subprocess

import pytest
import requests

# Fixed test image bytes — minimal JPEG-like header + deterministic payload
TEST_IMAGE_BYTES = (
    b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00'
    b'\xff\xd9'
    b'CLOTHINGSHOP_TEST_PAYLOAD_2024'
)


def _run_psql(container: str, query: str) -> str:
    result = subprocess.run(
        ["podman", "exec", container,
         "psql", "-U", "clothingshop", "-d", "clothingshop",
         "-t", "-A", "-c", query],
        capture_output=True, text=True,
    )
    assert result.returncode == 0, f"psql failed: {result.stderr}"
    return result.stdout.strip()


def _download_image(api_base_url: str, object_key: str) -> bytes:
    response = requests.get(f"{api_base_url}/images/{object_key}")
    assert response.status_code == 200, f"Image download failed: {response.status_code}"
    return response.content


def test_create_product(api_base_url, admin_auth, category_id):
    """Test creating a product via the admin API."""
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Integration Test Product",
            "description": "A product created by the integration test suite",
            "shortDescription": "Test product",
            "prices": [{"currency": "PLN", "price": "42.00"}, {"currency": "EUR", "price": "9.99"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 201, f"Expected 201, got {response.status_code}: {response.text}"
    data = response.json()
    assert "id" in data
    assert data["name"] == "Integration Test Product"
    assert data["category"]["slug"] == "tops"


def test_full_product_with_image_e2e(api_base_url, admin_auth, category_id, postgres_container):
    """End-to-end: create product, upload image, verify in DB, verify file in MinIO."""
    # Step 1: Create product
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "E2E Test Product",
            "description": "End-to-end test with image upload and verification",
            "shortDescription": "E2E test",
            "prices": [{"currency": "PLN", "price": "99.99"}, {"currency": "EUR", "price": "22.99"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 201, f"Create product failed: {response.text}"
    product = response.json()
    product_id = product["id"]

    # Step 2: Upload image
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products/{product_id}/image",
        auth=admin_auth,
        files={"file": ("test.jpg", TEST_IMAGE_BYTES, "image/jpeg")},
        data={"alt": "E2E test image"},
    )
    assert response.status_code == 201, f"Upload image failed: {response.text}"
    image_data = response.json()
    image_id = image_data["imageId"]
    image_url = image_data["imageUrl"]

    # Step 3: Verify product in postgres
    product_name = _run_psql(postgres_container, f"SELECT name FROM product WHERE id = '{product_id}'")
    assert product_name == "E2E Test Product", f"Product name mismatch: {product_name}"

    # Step 4: Verify image record in postgres
    object_key = _run_psql(
        postgres_container,
        f"SELECT object_key FROM product_image WHERE product_id = '{product_id}' LIMIT 1",
    )
    assert object_key == f"products/{product_id}/{image_id}/original.jpg", \
        f"Object key mismatch: {object_key}"

    # Step 5: Download image via nginx proxy and compare sha256
    uploaded_sha256 = hashlib.sha256(TEST_IMAGE_BYTES).hexdigest()
    downloaded_bytes = _download_image(api_base_url, object_key)
    downloaded_sha256 = hashlib.sha256(downloaded_bytes).hexdigest()

    assert uploaded_sha256 == downloaded_sha256, \
        f"SHA256 mismatch: uploaded={uploaded_sha256} downloaded={downloaded_sha256}"
