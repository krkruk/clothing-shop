import re
import json


def _get_first_product_id(page, base_url):
    """Get the first product ID from the API."""
    resp = page.request.get(f"{base_url}/api/v1/products?limit=1")
    data = resp.json()
    return data["items"][0]["id"]


def _navigate_to_product(page, base_url):
    """Navigate to the first product's detail page."""
    product_id = _get_first_product_id(page, base_url)
    page.goto(f"{base_url}/products/{product_id}")
    page.wait_for_timeout(1500)
    return product_id


def test_product_hero_carousel(page, base_url):
    _navigate_to_product(page, base_url)
    hero = page.locator("[aria-label='Image carousel']")
    assert hero.is_visible()


def test_acquisition_form_fields(page, base_url):
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.5)")
    page.wait_for_timeout(500)
    assert page.locator("label[for='silhouette']").is_visible()
    assert page.locator("label[for='waist']").is_visible()
    assert page.locator("label[for='hips']").is_visible()
    assert page.locator("label[for='height']").is_visible()


def test_narrative_content(page, base_url):
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.4)")
    page.wait_for_timeout(500)
    # At minimum, product title should be visible (fabrication/ethics are conditional)
    title = page.locator("h1")
    assert title.is_visible()


def test_detail_grid(page, base_url):
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.8)")
    page.wait_for_timeout(500)
    # Detail grid section should be visible
    grid = page.locator("section").last
    assert grid.is_visible()


def test_acquire_artifact_button(page, base_url):
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.5)")
    page.wait_for_timeout(500)
    button = page.locator("text=ACQUIRE ARTIFACT")
    assert button.is_visible()


def test_content_area_has_white_background(page, base_url):
    """Content area (scroll-gradient area) must have a white/light background (#f5f5f5)."""
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.5)")
    page.wait_for_timeout(500)
    # Find the white content area
    content = page.locator("div.bg-\\[\\#f5f5f5\\]")
    assert content.count() >= 1, "White content area not found"
    is_white = content.first.evaluate("""el => {
        const bg = getComputedStyle(el).backgroundColor;
        const match = bg.match(/rgb[a]?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);
        if (match) return parseInt(match[1]) > 200 && parseInt(match[2]) > 200 && parseInt(match[3]) > 200;
        // oklch fallback: lightness > 0.8 means light
        const oklch = bg.match(/oklch\\(([\\d.]+)/);
        if (oklch) return parseFloat(oklch[1]) > 0.8;
        return false;
    }""")
    assert is_white, "Content area background is not white/light enough"


def test_text_is_dark_on_white_bg(page, base_url):
    """Text in the white content area must be dark for readability."""
    _navigate_to_product(page, base_url)
    page.evaluate("window.scrollTo(0, document.body.scrollHeight * 0.5)")
    page.wait_for_timeout(500)
    # Product title in white content area should be dark
    title = page.locator("h1.text-neutral-900")
    assert title.count() >= 1, "Dark text title not found"
    is_dark = title.first.evaluate("""el => {
        const color = getComputedStyle(el).color;
        const match = color.match(/rgb[a]?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);
        if (match) return parseInt(match[1]) < 100 && parseInt(match[2]) < 100 && parseInt(match[3]) < 100;
        // oklch fallback: parse lightness
        const oklch = color.match(/oklch\\(([\\d.]+)/);
        if (oklch) return parseFloat(oklch[1]) < 0.4;
        return false;
    }""")
    assert is_dark, "Text is too light on white background"


def test_hero_carousel_fills_area(page, base_url):
    """Hero carousel should fill ~2/3 of viewport height with no black gap below."""
    _navigate_to_product(page, base_url)

    viewport = page.viewport_size
    assert viewport is not None
    viewport_height = viewport["height"]

    hero = page.locator("[aria-label='Image carousel']")
    assert hero.is_visible(), "Hero carousel not visible"
    hero_box = hero.bounding_box()
    assert hero_box is not None, "Hero carousel has no bounding box"

    hero_ratio = hero_box["height"] / viewport_height
    assert hero_ratio >= 0.55, (
        f"Hero height ratio {hero_ratio:.2f} is too small (expected >= 0.55, ~2/3 viewport)"
    )

    # Verify no black gap between hero and content area
    # The content area with white bg should start right below the hero
    content_area = page.locator("div.bg-\\[\\#f5f5f5\\]")
    assert content_area.count() >= 1, "White content area not found"
    content_box = content_area.first.bounding_box()
    assert content_box is not None, "Content area has no bounding box"

    # Gap between hero bottom and content top should be minimal (< 50px)
    gap = content_box["y"] - (hero_box["y"] + hero_box["height"])
    assert gap < 50, (
        f"Gap between hero and content is {gap:.0f}px (expected < 50px, no black gap)"
    )
