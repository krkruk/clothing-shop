"""Quick test to verify image upload works after the fix."""

import pytest
import requests

TEST_IMAGE_BYTES = (
    b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00'
    b'\xff\xd9'
    b'IMAGE_UPLOAD_VERIFICATION'
)


def test_image_upload_after_create(api_base_url, admin_auth, category_id):
    """Create product, upload image, verify it returns 201 with valid imageId."""
    # Create product
    resp = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Image Upload Verification",
            "description": "Verifying image upload works",
            "shortDescription": "Upload verify",
            "prices": [{"currency": "PLN", "price": "10.00"}, {"currency": "EUR", "price": "2.50"}],
            "categoryId": category_id,
        },
    )
    assert resp.status_code == 201, f"Create failed: {resp.text}"
    product_id = resp.json()["id"]

    # Upload image
    upload_resp = requests.post(
        f"{api_base_url}/api/v1/admin/products/{product_id}/image",
        auth=admin_auth,
        files={"file": ("test.jpg", TEST_IMAGE_BYTES, "image/jpeg")},
        data={"alt": "Verification image"},
    )
    assert upload_resp.status_code == 201, f"Upload failed ({upload_resp.status_code}): {upload_resp.text}"
    data = upload_resp.json()
    assert "imageId" in data
    assert "imageUrl" in data

    # Verify image shows in product detail
    detail_resp = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert detail_resp.status_code == 200
    images = detail_resp.json().get("images", [])
    assert len(images) >= 1, "Expected at least one image on product"
