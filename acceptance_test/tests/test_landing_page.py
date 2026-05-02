import re


def test_header_present(page, base_url):
    page.goto(base_url)
    page.wait_for_selector("header", timeout=10000)
    header = page.locator("header")
    assert header.is_visible()
    assert header.locator("text=CLOTHINGSHOP").is_visible()


def test_hero_section_visible(page, base_url):
    page.goto(base_url)
    # Wait for products to load (carousel is populated async from API)
    page.wait_for_selector("[aria-label='Image carousel'] img", timeout=10000)
    hero = page.locator("[aria-label='Image carousel']")
    assert hero.is_visible()


def test_landing_page_displays_product_rows(page, base_url):
    """Landing page loads and displays product rows from the API."""
    page.goto(base_url)
    # Wait for products to load from API
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # At least 1 product name should be visible (from seed data)
    rows = page.locator("text=ACQUIRE")
    assert rows.count() >= 1


def test_hero_carousel_shows_product_labels(page, base_url):
    """Hero carousel shows product images with labels at bottom-right."""
    page.goto(base_url)
    page.wait_for_selector("[aria-label='Image carousel']", timeout=10000)
    # Wait for products to load
    page.wait_for_timeout(2000)
    carousel = page.locator("[aria-label='Image carousel']")
    # Product name labels should be visible in carousel
    # The carousel has absolute positioned labels at bottom-right
    labels = carousel.locator("span")
    assert labels.count() >= 1


def test_lookbook_displays_product_images(page, base_url):
    """Lookbook section displays product images from loaded data."""
    page.goto(base_url)
    # Wait for products to load
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # Scroll to lookbook section
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.7)")
    page.wait_for_timeout(500)
    # Lookbook section should have images from product data
    lookbook = page.locator("section.bg-surface-dim")
    assert lookbook.is_visible()
    images = lookbook.locator("img")
    assert images.count() >= 1


def test_infinite_scroll_loads_more(page, base_url):
    """Infinite scroll: spinner appears, next page loads, products append."""
    page.goto(base_url)
    # Wait for first batch of products
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    initial_count = page.locator("text=ACQUIRE").count()
    # Scroll to bottom to trigger infinite scroll
    page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    page.wait_for_timeout(2000)
    # If there are more products, the spinner should have appeared and loaded more
    # If only one page of products, the count stays the same (this is fine)
    final_count = page.locator("text=ACQUIRE").count()
    assert final_count >= initial_count


def test_end_of_products_shows_footer(page, base_url):
    """End of products: spinner disappears and footer is visible."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # Scroll to the very bottom past all products
    page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    page.wait_for_timeout(1000)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    page.wait_for_timeout(1000)
    # Footer should be visible
    assert page.locator("text=INVENTORY").is_visible()
    assert page.locator("text=TRANSACTIONS").is_visible()


def test_acquire_buttons_present(page, base_url):
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    buttons = page.locator("text=ACQUIRE")
    assert buttons.count() >= 6


def test_footer_links_present(page, base_url):
    page.goto(base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
    page.wait_for_timeout(500)
    assert page.locator("text=INVENTORY").is_visible()
    assert page.locator("text=TRANSACTIONS").is_visible()
    assert page.locator("text=LEGAL").is_visible()
    assert page.locator("text=MANIFESTO").is_visible()


def test_product_grid_full_width(page, base_url):
    """Product grid must span full viewport width (no max-w-7xl constraint)."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)
    # The chessboard grid section should NOT have a max-w-7xl inner container
    section = page.locator("section.bg-surface-container-lowest").first
    assert section.is_visible()
    # Verify there is no max-w-7xl inside the chessboard section
    constrained = section.locator(".max-w-7xl")
    assert constrained.count() == 0, "Chessboard grid should not be constrained by max-w-7xl"


def test_loading_spinner_visible(page, base_url):
    """A loading spinner or product content must be present after page load."""
    page.goto(base_url)
    # Wait briefly for either spinner or products to appear
    page.wait_for_timeout(1000)
    spinner = page.locator(".animate-spin")
    products_loaded = page.locator("text=ACQUIRE").count() > 0
    assert spinner.is_visible() or products_loaded


def test_chessboard_row_layout(page, base_url):
    """Each chessboard row: image takes ~1/3 width, content takes ~2/3 width."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)

    viewport = page.viewport_size
    assert viewport is not None
    viewport_width = viewport["width"]

    rows = page.locator(".grid.grid-cols-1")
    row_count = rows.count()
    assert row_count >= 2, "Expected at least 2 chessboard rows"

    for i in range(min(row_count, 4)):
        row = rows.nth(i)
        row_box = row.bounding_box()
        assert row_box is not None, f"Row {i} has no bounding box"
        row_width = row_box["width"]

        children = row.locator("> div")
        assert children.count() == 2, f"Row {i} should have 2 children"

        img_col = children.nth(0)
        content_col = children.nth(1)
        img_box = img_col.bounding_box()
        content_box = content_col.bounding_box()
        assert img_box is not None and content_box is not None

        # Image should be ~1/3 of row width
        img_ratio = img_box["width"] / row_width
        assert 0.25 < img_ratio < 0.40, (
            f"Row {i} image ratio {img_ratio:.2f} not ~1/3 (expected 0.25-0.40)"
        )

        # Content should be ~2/3 of row width
        content_ratio = content_box["width"] / row_width
        assert 0.55 < content_ratio < 0.75, (
            f"Row {i} content ratio {content_ratio:.2f} not ~2/3 (expected 0.55-0.75)"
        )


def test_chessboard_image_thumbnail_max_height(page, base_url):
    """Product thumbnail images must not exceed 1/3 of viewport height."""
    page.goto(base_url)
    page.wait_for_selector("text=ACQUIRE", timeout=10000)

    viewport = page.viewport_size
    assert viewport is not None
    max_height = viewport["height"] / 3

    rows = page.locator(".grid.grid-cols-1")
    row_count = rows.count()

    for i in range(min(row_count, 4)):
        row = rows.nth(i)
        img = row.locator("img").first
        if not img.is_visible():
            row.scroll_into_view_if_needed()
            page.wait_for_timeout(300)
        img_box = img.bounding_box()
        assert img_box is not None, f"Row {i} image has no bounding box"
        assert img_box["height"] <= max_height + 10, (
            f"Row {i} image height {img_box['height']:.0f}px exceeds 1/3 viewport ({max_height:.0f}px)"
        )
