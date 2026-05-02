def test_cart_opens_from_header(page, base_url):
    page.goto(base_url)
    page.click("[aria-label='Cart']")
    page.wait_for_timeout(500)
    assert page.locator("text=CURRENT INVENTORY").is_visible()


def test_empty_state_visible(page, base_url):
    page.goto(base_url)
    page.click("[aria-label='Cart']")
    page.wait_for_timeout(500)
    assert page.locator("text=INVENTORY IS CURRENTLY EMPTY").is_visible()


def _get_first_product_id(page, base_url):
    """Get the first product ID from the API."""
    resp = page.request.get(f"{base_url}/api/v1/products?limit=1")
    data = resp.json()
    return data["items"][0]["id"]


def _add_product_to_cart(page, base_url):
    """Navigate to first product's detail page and click ACQUIRE ARTIFACT."""
    product_id = _get_first_product_id(page, base_url)
    page.goto(f"{base_url}/products/{product_id}")
    page.wait_for_timeout(1500)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.5)")
    page.wait_for_timeout(500)
    page.click("text=ACQUIRE ARTIFACT")
    page.wait_for_timeout(500)


def _open_cart(page):
    page.click("[aria-label='Cart']")
    page.wait_for_timeout(500)


def test_add_item_via_acquire_artifact(page, base_url):
    _add_product_to_cart(page, base_url)
    _open_cart(page)
    drawer = page.get_by_role("complementary")
    # Cart should have at least one item
    assert drawer.locator("text=TOTAL VALUE").is_visible()


def test_cart_item_appears_in_drawer(page, base_url):
    _add_product_to_cart(page, base_url)
    _open_cart(page)
    drawer = page.get_by_role("complementary")
    assert drawer.locator("text=TOTAL VALUE").is_visible()


def test_quantity_controls_work(page, base_url):
    _add_product_to_cart(page, base_url)
    _open_cart(page)
    plus_button = page.locator("[aria-label='Increase quantity']")
    plus_button.click()
    page.wait_for_timeout(300)
    # Should show quantity 2
    assert page.get_by_role("complementary").locator("text=2").first.is_visible()


def test_cart_persists_across_navigation(page, base_url):
    _add_product_to_cart(page, base_url)
    # Navigate to landing page
    page.goto(base_url)
    page.wait_for_timeout(1000)
    # Open cart
    _open_cart(page)
    assert page.get_by_role("complementary").locator("text=TOTAL VALUE").is_visible()
