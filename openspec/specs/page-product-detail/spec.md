## MODIFIED Requirements

### Requirement: Product data fetched from API
The product detail page SHALL fetch product data from `GET /api/v1/products/{id}` using the product ID from the URL. The page SHALL NOT use mock data. While loading, a loading state SHALL be displayed. If the product is not found (404), a "Artifact not found" message SHALL be displayed.

#### Scenario: Product loaded from API
- **WHEN** user navigates to `/products/{id}` for a valid product
- **THEN** the page fetches product data from the API and renders the product detail view

#### Scenario: Product not found
- **WHEN** user navigates to `/products/{id}` for a non-existent product
- **THEN** the page displays "Artifact not found" message

#### Scenario: Loading state
- **WHEN** the product data is being fetched
- **THEN** a loading indicator is displayed

### Requirement: Product page hero carousel
The hero section SHALL display a carousel of product-specific images with `33.33vh` height, `grayscale brightness-50 contrast-125` treatment, and a bottom gradient overlay (`bg-gradient-to-t from-surface-container-lowest/80 to-transparent`). The carousel SHALL use 4-5s auto-rotation with crossfade.

#### Scenario: Product images in carousel
- **WHEN** the product has 3 images
- **THEN** the hero carousel cycles through all 3 images with crossfade transitions

### Requirement: Scroll-gradient blend
The page SHALL implement a scroll-driven gradient transition from dark (`#0e0e0e`) to white (`#f5f5f5`). As the user scrolls past the hero, the background gradually shifts over approximately one viewport height.

#### Scenario: Scroll transition visible
- **WHEN** user scrolls from the hero section downward
- **THEN** the background gradually transitions from dark to white (#f5f5f5)

### Requirement: Content area with white background
The main content area SHALL have a white background (`#f5f5f5`) with dark text. The layout SHALL be a `max-w-7xl` container with a two-column grid: left column (2/3 width) for product narrative, right column (1/3 width) for acquisition form.

#### Scenario: White content area renders
- **WHEN** the product page content area is visible
- **THEN** the background is #f5f5f5 with dark text (not the dark surface palette)

### Requirement: Product narrative (left column)
The left column SHALL display: Series/Artifact label, product title (Space Grotesk, `text-6xl`, bold, tracking-tighter, uppercase), manifesto description with `border-l-2 border-primary-container`, fabrication grid (2-column), and ethics section.

#### Scenario: Narrative populated from API
- **WHEN** the product has description, fabrication content/care, and ethics origin/impact
- **THEN** all sections are rendered with the data from the API response

#### Scenario: Missing optional fields handled
- **WHEN** the product has no fabrication or ethics data
- **THEN** those sections are not rendered (no empty placeholders)

### Requirement: Acquisition form (right column, sticky)
The right column SHALL contain a sticky (`sticky top-32`) acquisition form with: silhouette dropdown, waist input, hips input (in same row as waist), height input, price display ("Transaction Value" + currency + price), ACQUIRE ARTIFACT button, and shipping note. All inputs SHALL use underline-only style with dark text variants.

#### Scenario: Form displays product price
- **WHEN** the product page loads with EUR selected
- **THEN** the "Transaction Value" shows the EUR price with "EUR" currency label

#### Scenario: ACQUIRE ARTIFACT adds to cart
- **WHEN** user fills personalization and clicks ACQUIRE ARTIFACT
- **THEN** the item is added to the cart with the product's current currency and price

### Requirement: Detail grid section
Below the white content area, the page SHALL return to dark background and display an asymmetric 12-column grid: 7 columns with a large product image (grayscale opacity-60) and 5 columns with a secondary image offset by `mt-24` on `surface_container_high` background.

#### Scenario: Detail grid renders product images
- **WHEN** the product has multiple images
- **THEN** the detail grid displays up to 2 images in the asymmetric layout

### Requirement: Currency display
All prices on the product page SHALL display using the currency code from the API response (not hardcoded dollar sign). The format SHALL be "{currency} {price}" (e.g., "PLN 399.00" or "EUR 89.00").

#### Scenario: Price in selected currency
- **WHEN** the user has EUR selected and views a product
- **THEN** the transaction value shows "EUR 89.00" (or the EUR price)
