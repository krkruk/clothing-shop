## MODIFIED Requirements

### Requirement: Landing page hero carousel
The landing page SHALL display a hero carousel with combined height (header + carousel) of 33.33vh. The carousel SHALL auto-rotate every 4-5 seconds with a 300ms crossfade transition, pause on hover, and resume on mouse leave. Images SHALL be displayed with `grayscale brightness-50` treatment. Progress indicators SHALL be thin horizontal bars at bottom-right.

The carousel images SHALL come from the first 4 products returned by `GET /api/v1/products?limit=7`. Each carousel slide SHALL display the product name as a label positioned at the bottom-right corner of the image. The image source SHALL be the product's `imageUrl` field.

#### Scenario: Hero carousel renders at correct height
- **WHEN** the landing page loads
- **THEN** the header plus hero carousel combined SHALL occupy 33.33vh
- **THEN** the carousel height alone SHALL be `calc(33.33vh - 80px)`

#### Scenario: Carousel auto-rotation
- **WHEN** the carousel is visible and not hovered
- **THEN** images SHALL crossfade every 4-5 seconds with 300ms transition

#### Scenario: Carousel pause on hover
- **WHEN** the user hovers over the carousel
- **THEN** auto-rotation SHALL pause
- **WHEN** the user moves the mouse away
- **THEN** auto-rotation SHALL resume

#### Scenario: Hero carousel shows product data
- **WHEN** the first API response returns 4+ products
- **THEN** the carousel SHALL display up to 4 slides, each with the product's `imageUrl` and product name as a label at bottom-right

#### Scenario: Hero carousel with fewer than 4 products
- **WHEN** the API returns fewer than 4 products
- **THEN** the carousel SHALL display all available products without error

### Requirement: Chessboard product grid
The landing page SHALL display products in a chessboard pattern with alternating rows within a horizontally centered container. The entire product grid SHALL be wrapped in a `max-w-7xl` container with `mx-auto` to ensure exact horizontal centering on the viewport. Odd rows: image (1/3 width) left + content (2/3 width) right. Even rows: content (2/3 width) left + image (1/3 width) right. Each product row SHALL display: product name, editorial description, price, and an "ACQUIRE" button that navigates to the product detail page.

Products SHALL be fetched from `GET /api/v1/products?limit=7` on page mount. Each `ChessboardRow` SHALL receive a `ProductSummary` object from the generated API client. The image source SHALL be the product's single `imageUrl` field (not an array). Products SHALL render as a flat stream without category grouping.

#### Scenario: Alternating row layout
- **WHEN** the landing page renders with multiple products
- **THEN** odd-numbered rows SHALL show image on the left (1/3) and content on the right (2/3)
- **THEN** even-numbered rows SHALL show content on the left (2/3) and image on the right (1/3)

#### Scenario: Grid is horizontally centered
- **WHEN** the landing page renders on any viewport width
- **THEN** the product grid container SHALL be centered horizontally (equal left and right margins)
- **THEN** the grid SHALL have a maximum width of `max-w-7xl` (80rem)

#### Scenario: Product row content
- **WHEN** a product row renders
- **THEN** it SHALL display the product name in Space Grotesk bold uppercase
- **THEN** it SHALL display a short editorial description in Manrope light
- **THEN** it SHALL display an "ACQUIRE" button with `primary_container` background

#### Scenario: ACQUIRE button navigation
- **WHEN** the user clicks an "ACQUIRE" button on a product row
- **THEN** the application SHALL navigate to the product detail page for that product

#### Scenario: Products render from API data
- **WHEN** the landing page mounts and the API responds successfully
- **THEN** product rows SHALL render using `ProductSummary` data (id, name, price, imageUrl, shortDescription, category)

### Requirement: Lookbook fragment
The landing page SHALL include a lookbook section with an asymmetric 12-column grid layout. The section SHALL render images from the first 4 products loaded from the API. The section background SHALL be `surface_dim` (#131313).

#### Scenario: Lookbook section renders with product images
- **WHEN** the landing page renders with loaded products
- **THEN** the lookbook section SHALL display up to 4 product images in the asymmetric grid layout

#### Scenario: Lookbook with fewer than 4 products
- **WHEN** fewer than 4 products are loaded
- **THEN** the lookbook section SHALL render with available images without error

### Requirement: Infinite scroll indicator
Below the product grid, the landing page SHALL display a subtle dark-toned spinner while products are loading. When no more products exist (`hasMore === false`), the spinner SHALL be removed and the Footer component (from `+layout.svelte`) SHALL be the natural end of the page.

#### Scenario: Loading spinner during fetch
- **WHEN** a product fetch is in progress
- **THEN** a subtle dark spinner SHALL be visible below the last loaded product

#### Scenario: Infinite scroll triggers next page
- **WHEN** the user scrolls near the bottom of the loaded products and `hasMore` is true
- **THEN** the system SHALL fetch the next 7 products using the `nextCursor` from the previous response
- **THEN** new products SHALL append to the existing list

#### Scenario: End of product list
- **WHEN** `hasMore` becomes false after a fetch
- **THEN** the spinner and scroll indicator SHALL disappear
- **THEN** the Footer SHALL be visible as the natural end of the page

## REMOVED Requirements

### Requirement: Category tonal shifts
**Reason**: Category grouping with background tonal shifts is deferred to a future change. Products will render as a flat stream without category sectioning.
**Migration**: When category grouping is re-implemented, add back the category-based background color logic. The backend API already supports `?category=slug` filtering.

### Requirement: Mock data as product source
**Reason**: The landing page now fetches from the real API. Mock data imports are replaced with generated API client calls.
**Migration**: Mock data files remain in `$lib/mock/` for other pages (e.g., product detail) until they are migrated in future changes.
