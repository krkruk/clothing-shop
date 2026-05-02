"""Acceptance tests verifying public GET /api/v1/products excludes inactive products."""

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


def test_public_list_excludes_inactive(api_base_url, admin_auth, category_id, postgres_container):
    """Public GET /products does not include soft-deleted products."""
    # Create a product
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Visible Product Test",
            "description": "This should appear in public listing",
            "shortDescription": "Visible",
            "prices": [{"currency": "PLN", "price": "75.00"}, {"currency": "EUR", "price": "17.50"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 201
    product_id = response.json()["id"]

    # Verify it appears in public listing (paginate through if needed)
    found = False
    cursor = None
    for _ in range(20):  # safety limit
        url = f"{api_base_url}/api/v1/products?limit=100"
        if cursor:
            url += f"&cursor={cursor}"
        public_resp = requests.get(url)
        assert public_resp.status_code == 200
        data = public_resp.json()
        public_ids = [p["id"] for p in data["items"]]
        if product_id in public_ids:
            found = True
            break
        if not data["hasMore"]:
            break
        cursor = data["nextCursor"]
    assert found, "Active product should appear in public listing"

    # Soft delete via DB
    _run_psql(postgres_container, f"UPDATE product SET is_active = false WHERE id = '{product_id}'")

    # Verify it no longer appears (check all pages)
    found_after_delete = False
    cursor = None
    for _ in range(20):
        url = f"{api_base_url}/api/v1/products?limit=100"
        if cursor:
            url += f"&cursor={cursor}"
        public_resp2 = requests.get(url)
        assert public_resp2.status_code == 200
        data2 = public_resp2.json()
        public_ids2 = [p["id"] for p in data2["items"]]
        if product_id in public_ids2:
            found_after_delete = True
            break
        if not data2["hasMore"]:
            break
        cursor = data2["nextCursor"]
    assert not found_after_delete, "Inactive product should NOT appear in public listing"
    assert public_resp2.status_code == 200
    public_ids2 = [p["id"] for p in public_resp2.json()["items"]]
    assert product_id not in public_ids2, "Inactive product should NOT appear in public listing"


def test_public_detail_excludes_inactive(api_base_url, admin_auth, category_id, postgres_container):
    """GET /products/{id} returns 404 for inactive product."""
    response = requests.post(
        f"{api_base_url}/api/v1/admin/products",
        auth=admin_auth,
        json={
            "name": "Detail Hide Test",
            "description": "Will be hidden",
            "shortDescription": "Hidden",
            "prices": [{"currency": "PLN", "price": "50.00"}, {"currency": "EUR", "price": "11.50"}],
            "categoryId": category_id,
        },
    )
    assert response.status_code == 201
    product_id = response.json()["id"]

    # Verify it exists first
    resp = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert resp.status_code == 200

    # Soft delete
    _run_psql(postgres_container, f"UPDATE product SET is_active = false WHERE id = '{product_id}'")

    # Now 404
    resp2 = requests.get(f"{api_base_url}/api/v1/products/{product_id}")
    assert resp2.status_code == 404
