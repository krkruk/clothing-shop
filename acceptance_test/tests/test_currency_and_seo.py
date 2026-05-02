"""Acceptance tests for landing-and-product-fixes change:
- Currency picker in header
- Currency display across pages
- User icon dropdown
- SEO metadata
- White content area on product detail
"""


def test_currency_picker_visible_in_header(page, base_url):
    """Currency picker (PLN/EUR) should be visible in the header."""
    page.goto(base_url)
    page.wait_for_selector("header", timeout=10000)
    # Default should show PLN
    currency_button = page.locator("button[aria-label='Select currency']")
    assert currency_button.is_visible()
    assert currency_button.inner_text().__contains__("PLN")


def test_currency_picker_switches_to_eur(page, base_url):
    """Clicking currency picker should show EUR option and switch."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # Click currency picker
    page.click("button[aria-label='Select currency']")
    page.wait_for_timeout(300)
    # Click EUR
    page.click("text=EUR")
    page.wait_for_timeout(300)
    # Verify button shows EUR
    currency_button = page.locator("button[aria-label='Select currency']")
    assert "EUR" in currency_button.inner_text()


def test_currency_display_on_landing_page(page, base_url):
    """Product prices on landing page should display currency code (PLN/EUR)."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # Prices should show "PLN" followed by a number (default currency)
    prices = page.locator("text=PLN")
    assert prices.count() >= 1, "No PLN prices found on landing page"


def test_currency_switch_updates_prices(page, base_url):
    """Switching currency to EUR should update prices on landing page."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # Switch to EUR
    page.click("button[aria-label='Select currency']")
    page.wait_for_timeout(300)
    page.click("text=EUR")
    page.wait_for_timeout(2000)
    # Prices should now show EUR
    prices = page.locator("text=EUR")
    assert prices.count() >= 1, "No EUR prices found after switching currency"


def test_user_dropdown_login_option(page, base_url):
    """User icon dropdown should show Login option when not authenticated."""
    page.goto(base_url)
    # Click user icon
    page.click("button[aria-label='Account']")
    page.wait_for_timeout(300)
    # Should show Login option
    assert page.locator("text=Login").is_visible()


def test_user_dropdown_closes_on_escape(page, base_url):
    """User dropdown should close when Escape is pressed."""
    page.goto(base_url)
    page.click("button[aria-label='Account']")
    page.wait_for_timeout(300)
    assert page.locator("text=Login").is_visible()
    # Press Escape
    page.keyboard.press("Escape")
    page.wait_for_timeout(300)
    # Login should no longer be visible
    assert page.locator("text=Login").is_visible() is False


def test_currency_dropdown_closes_on_escape(page, base_url):
    """Currency dropdown should close when Escape is pressed."""
    page.goto(base_url)
    page.click("button[aria-label='Select currency']")
    page.wait_for_timeout(300)
    assert page.locator("text=EUR").is_visible()
    # Press Escape
    page.keyboard.press("Escape")
    page.wait_for_timeout(300)
    # EUR option should no longer be visible
    assert page.locator("text=EUR").is_visible() is False


def test_seo_meta_tags_on_landing(page, base_url):
    """Landing page should have SEO meta tags."""
    page.goto(base_url)
    page.wait_for_selector("header", timeout=10000)
    description = page.locator('meta[name="description"]')
    assert description.count() == 1
    og_title = page.locator('meta[property="og:title"]')
    assert og_title.count() == 1
    og_type = page.locator('meta[property="og:type"]')
    assert og_type.count() == 1


def test_seo_meta_tags_on_product_page(page, base_url):
    """Product detail page should have SEO meta tags and JSON-LD."""
    # Get first product ID
    resp = page.request.get(f"{base_url}/api/v1/products?limit=1")
    data = resp.json()
    product_id = data["items"][0]["id"]
    page.goto(f"{base_url}/products/{product_id}")
    page.wait_for_timeout(1500)
    # Check meta description
    description = page.locator('meta[name="description"]')
    assert description.count() == 1
    # Check JSON-LD structured data
    jsonld = page.locator('script[type="application/ld+json"]')
    assert jsonld.count() == 1


def test_robots_txt_accessible(page, base_url):
    """robots.txt should be accessible."""
    resp = page.request.get(f"{base_url}/robots.txt")
    assert resp.ok
    body = resp.text()
    assert "User-agent:" in body
    assert "Allow: /" in body
    assert "Sitemap:" in body
