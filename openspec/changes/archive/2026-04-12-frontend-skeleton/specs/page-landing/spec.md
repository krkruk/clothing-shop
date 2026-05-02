## ADDED Requirements

### Requirement: Landing page hero carousel
The landing page SHALL display a hero carousel with combined height (header + carousel) of 33.33vh. The carousel SHALL auto-rotate every 4-5 seconds with a 300ms crossfade transition, pause on hover, and resume on mouse leave. Images SHALL be displayed with `grayscale brightness-50` treatment. Progress indicators SHALL be thin horizontal bars at bottom-right.

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

### Requirement: Chessboard product grid
The landing page SHALL display products in a chessboard pattern with alternating rows within a horizontally centered container. The entire product grid SHALL be wrapped in a `max-w-7xl` container with `mx-auto` to ensure exact horizontal centering on the viewport. Odd rows: image (1/3 width) left + content (2/3 width) right. Even rows: content (2/3 width) left + image (1/3 width) right. Each product row SHALL display: product name, editorial description, price, and an "ACQUIRE" button that navigates to the product detail page.

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

### Requirement: Category tonal shifts
Products SHALL be grouped by category (TOPS, BOTTOMS, ACCESSORIES). Category boundaries SHALL be signaled solely by background color tonal shifts: TOPS on `surface_container_lowest` (#0e0e0e), BOTTOMS on `surface_container` (#201f1f), ACCESSORIES on `surface_container_lowest` (#0e0e0e). No text labels or divider lines SHALL separate categories.

#### Scenario: Category background colors alternate
- **WHEN** the landing page renders with products from multiple categories
- **THEN** TOPS products SHALL have #0e0e0e background
- **THEN** BOTTOMS products SHALL have #201f1f background
- **THEN** ACCESSORIES products SHALL have #0e0e0e background

### Requirement: Image hover effect
Product images in the chessboard grid SHALL apply `opacity-80` and `mix-blend-luminosity` by default. On hover, the image SHALL scale to 105% over 700ms.

#### Scenario: Product image hover animation
- **WHEN** the user hovers over a product image
- **THEN** the image SHALL scale to 105% with a 700ms transition duration

### Requirement: Lookbook fragment
The landing page SHALL include a lookbook section with an asymmetric 12-column grid layout: col-span-5 with a full-height image, col-span-7 with a 2-column sub-grid and editorial text. The section background SHALL be `surface_dim` (#131313).

#### Scenario: Lookbook section renders
- **WHEN** the landing page renders below the chessboard grid
- **THEN** a lookbook section SHALL appear with asymmetric image grid and editorial text

### Requirement: Infinite scroll indicator
Below the product grid, the landing page SHALL display a loading indicator with text "CONTINUE EXPLORING THE VOID" in `label-sm` all-caps and an animated `expand_more` icon in `primary` color.

#### Scenario: Scroll indicator visible
- **WHEN** the user scrolls to the bottom of the product grid
- **THEN** the "CONTINUE EXPLORING THE VOID" text and animated chevron SHALL be visible

### Requirement: Scroll hint below hero
Below the hero carousel, a subtle animated vertical line SHALL be displayed using a `primary` to transparent gradient, pulsing to indicate scrollability.

#### Scenario: Scroll hint animation
- **WHEN** the hero carousel is visible
- **THEN** a 1px-wide vertical line SHALL pulse below the carousel with primary-to-transparent gradient
