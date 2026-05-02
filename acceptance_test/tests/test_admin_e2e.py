"""End-to-end acceptance test: admin full product lifecycle.

Tests the complete admin flow:
1. Login (Basic auth verification)
2. Create product with images
3. List products (verify in inventory)
4. Update product
5. Delete product
"""

import uuid

import pytest
import requests


# Minimal JPEG for uploads
TEST_IMAGE_BYTES = (
    b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00'
    b'\xff\xd9'
    b'E2E_ADMIN_LIFECYCLE_TEST'
)


def test_admin_full_product_lifecycle(api_base_url, admin_auth, category_id):
    """E2E: create → list → update → delete product."""
    headers_no_auth = {}
    headers_auth = {}  # requests `auth` param handles this

    # --- Step 1: Verify auth is required ---
    r = requests.get(f"{api_base_url}/api/v1/admin/products")
    assert r.status_code == 401, "Unauthenticated admin listing should return 401"

    r = requests.get(f"{api_base_url}/api/v1/admin/products", auth=admin_auth)
    assert r.status_code == 200, "Authenticated admin listing should return 200"

    # --- Step 2: Create product ---
    sku = f"E2E-{uuid.uuid4().hex[:8]}"
    create_resp = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "E2E Lifecycle Product",
            "description": "Testing the full admin lifecycle",
            "shortDescription": "E2E lifecycle",
            "prices": [{"currency": "PLN", "price": "199.99"}, {"currency": "EUR", "price": "46.99"}],
            "categoryId": category_id,
            "sku": sku,
            "isActive": True,
            "fabrication": {"content": "100% cotton", "care": "Machine wash cold"},
            "ethics": {"origin": "Poland", "impact": "Ethically sourced"},
        },
    )
    assert create_resp.status_code == 201, f"Create failed: {create_resp.text}"
    product = create_resp.json()
    product_id = product["id"]
    assert product["name"] == "E2E Lifecycle Product"
    assert product["sku"] == sku
    pln_price = next(p for p in product["prices"] if p["currency"] == "PLN")
    assert str(pln_price["price"]) in ("199.99", "199.990")
    assert product["isActive"] is True

    # --- Step 3: Upload image ---
    upload_resp = requests.post(
        f"{api_base_url}/api/v1/admin/products/{product_id}/image",
        auth=admin_auth,
        files={"file": ("lifecycle.jpg", TEST_IMAGE_BYTES, "image/jpeg")},
        data={"alt": "Lifecycle test image"},
    )
    assert upload_resp.status_code == 201, f"Upload failed: {upload_resp.text}"
    image_id = upload_resp.json()["imageId"]

    # --- Step 4: Verify in admin listing ---
    list_resp = requests.get(
        f"{api_base_url}/api/v1/admin/products?limit=100",
        auth=admin_auth,
    )
    assert list_resp.status_code == 200
    listed_ids = [p["id"] for p in list_resp.json()["items"]]
    assert product_id in listed_ids, "Created product should appear in admin listing"

    # Find the product in the listing and verify fields
    listed_product = next(p for p in list_resp.json()["items"] if p["id"] == product_id)
    assert listed_product["name"] == "E2E Lifecycle Product"
    assert listed_product["sku"] == sku

    # --- Step 5: Verify in public listing (paginate if needed) ---
    found_public = False
    pub_cursor = None
    for _ in range(20):
        pub_url = f"{api_base_url}/api/v1/products?limit=100"
        if pub_cursor:
            pub_url += f"&cursor={pub_cursor}"
        public_resp = requests.get(pub_url)
        assert public_resp.status_code == 200
        pub_data = public_resp.json()
        public_ids = [p["id"] for p in pub_data["items"]]
        if product_id in public_ids:
            found_public = True
            break
        if not pub_data["hasMore"]:
            break
        pub_cursor = pub_data["nextCursor"]
    assert found_public, "Active product should appear in public listing"

    # --- Step 6: Update product ---
    new_sku = f"E2E-UPD-{uuid.uuid4().hex[:8]}"
    update_resp = requests.put(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
        json={
            "name": "E2E Updated Product",
            "prices": [{"currency": "PLN", "price": "249.99"}, {"currency": "EUR", "price": "59.99"}],
            "sku": new_sku,
        },
    )
    assert update_resp.status_code == 200, f"Update failed: {update_resp.text}"
    updated = update_resp.json()
    assert updated["name"] == "E2E Updated Product"
    upd_pln = next(p for p in updated["prices"] if p["currency"] == "PLN")
    assert str(upd_pln["price"]) in ("249.99", "249.990")
    assert updated["sku"] == new_sku

    # --- Step 7: Delete product ---
    delete_resp = requests.delete(
        f"{api_base_url}/api/v1/admin/products/{product_id}",
        auth=admin_auth,
    )
    assert delete_resp.status_code == 204

    # --- Step 8: Verify deleted from public listing (check all pages) ---
    found_deleted = False
    pub_cursor2 = None
    for _ in range(20):
        pub_url2 = f"{api_base_url}/api/v1/products?limit=100"
        if pub_cursor2:
            pub_url2 += f"&cursor={pub_cursor2}"
        public_resp2 = requests.get(pub_url2)
        assert public_resp2.status_code == 200
        pub_data2 = public_resp2.json()
        public_ids2 = [p["id"] for p in pub_data2["items"]]
        if product_id in public_ids2:
            found_deleted = True
            break
        if not pub_data2["hasMore"]:
            break
        pub_cursor2 = pub_data2["nextCursor"]
    assert not found_deleted, "Deleted product should NOT appear in public listing"

    # --- Step 9: Verify still in admin listing (as inactive) ---
    list_resp2 = requests.get(
        f"{api_base_url}/api/v1/admin/products?limit=100",
        auth=admin_auth,
    )
    admin_ids2 = [p["id"] for p in list_resp2.json()["items"]]
    assert product_id in admin_ids2, "Soft-deleted product should still appear in admin listing"

    # Verify it's marked inactive
    admin_product = next(p for p in list_resp2.json()["items"] if p["id"] == product_id)
    assert admin_product["isActive"] is False
