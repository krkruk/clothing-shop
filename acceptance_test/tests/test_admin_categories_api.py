"""Acceptance tests for GET /api/v1/categories endpoint."""

import pytest
import requests


def test_list_categories_returns_200(api_base_url):
    """GET /categories returns 200 with array of categories."""
    response = requests.get(f"{api_base_url}/api/v1/categories")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_list_categories_structure(api_base_url):
    """Each category has id, slug, and name fields."""
    response = requests.get(f"{api_base_url}/api/v1/categories")
    assert response.status_code == 200
    categories = response.json()

    assert len(categories) > 0, "Expected at least one category"
    for cat in categories:
        assert "id" in cat, f"Missing 'id' in category: {cat}"
        assert "slug" in cat, f"Missing 'slug' in category: {cat}"
        assert "name" in cat, f"Missing 'name' in category: {cat}"


def test_list_categories_ordered_by_display_order(api_base_url):
    """Categories are returned in display_order sequence: tops, coats, bottoms, accessories."""
    response = requests.get(f"{api_base_url}/api/v1/categories")
    assert response.status_code == 200
    categories = response.json()

    expected_slugs = ["tops", "coats", "bottoms", "accessories"]
    actual_slugs = [cat["slug"] for cat in categories]
    assert actual_slugs == expected_slugs, f"Category order mismatch: {actual_slugs}"


def test_list_categories_no_auth_required(api_base_url):
    """GET /categories works without authentication."""
    response = requests.get(f"{api_base_url}/api/v1/categories")
    assert response.status_code == 200


def test_list_categories_expected_names(api_base_url):
    """Category names match the expected set."""
    response = requests.get(f"{api_base_url}/api/v1/categories")
    categories = response.json()

    expected = {"tops": "Tops", "coats": "Coats", "bottoms": "Bottoms", "accessories": "Accessories"}
    for cat in categories:
        assert cat["slug"] in expected, f"Unexpected slug: {cat['slug']}"
        assert cat["name"] == expected[cat["slug"]], f"Name mismatch for {cat['slug']}: {cat['name']}"
