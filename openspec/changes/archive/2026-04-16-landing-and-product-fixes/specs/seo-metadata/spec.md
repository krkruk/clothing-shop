## ADDED Requirements

### Requirement: Meta tags on landing page
The landing page SHALL include `<meta name="description">` and Open Graph tags (`og:title`, `og:description`, `og:type`, `og:url`) via `<svelte:head>`.

#### Scenario: Landing page meta tags rendered
- **WHEN** the landing page loads
- **THEN** the HTML `<head>` contains `<meta name="description" content="...">` and Open Graph meta tags with "CLOTHINGSHOP" branding

### Requirement: JSON-LD Product schema on product detail page
The product detail page SHALL include a JSON-LD `<script type="application/ld+json">` block with Product schema (name, description, image, offers with price and currency) for search engine rich results.

#### Scenario: Product page includes structured data
- **WHEN** a product detail page loads for product "The Obsidian Structure Coat" priced at PLN 399.00
- **THEN** the page includes a JSON-LD script with `@type: Product`, the product name, description, image URL, and `offers` containing price "399.00" and currency "PLN"

#### Scenario: JSON-LD uses selected currency
- **WHEN** the product page is viewed with EUR selected
- **THEN** the JSON-LD offers show the EUR price and currency "EUR"

### Requirement: Build-time sitemap generation
A build script SHALL generate a `sitemap.xml` file by fetching all active products from the API and creating URL entries for the landing page and each product detail page. The sitemap SHALL be placed in `frontend/static/sitemap.xml`.

#### Scenario: Sitemap contains all product URLs
- **WHEN** the build script runs
- **THEN** `sitemap.xml` contains `<url>` entries for `/` and `/products/{id}` for every active product

### Requirement: Static robots.txt
A `robots.txt` file SHALL be placed in `frontend/static/` allowing all crawlers and referencing the sitemap.

#### Scenario: Robots.txt allows crawling
- **WHEN** a crawler requests `/robots.txt`
- **THEN** it receives `User-agent: *`, `Allow: /`, and `Sitemap: /sitemap.xml`

### Requirement: Page title and meta on product detail
The product detail page SHALL set `<title>` to "{product name} — CLOTHINGSHOP" and include a `<meta name="description">` with the product's short description.

#### Scenario: Product page title set
- **WHEN** the product detail page loads for "The Obsidian Structure Coat"
- **THEN** the page title is "The Obsidian Structure Coat — CLOTHINGSHOP"

#### Scenario: Product page meta description set
- **WHEN** the product detail page loads
- **THEN** the HTML `<head>` contains `<meta name="description" content="{shortDescription}">`
