## ADDED Requirements

### Requirement: Product hero carousel
The product detail page SHALL display a hero carousel at 33.33vh height with product-specific images. Images SHALL be treated with `grayscale brightness-50 contrast-125`. A bottom gradient overlay (`surface_container_lowest` at 80% opacity to transparent) SHALL blend into the content area. Progress indicators SHALL match the landing page style.

#### Scenario: Product hero renders
- **WHEN** the product detail page loads
- **THEN** a hero carousel SHALL render at 33.33vh with product images

### Requirement: Scroll-gradient blend
The product detail page SHALL implement a scroll-triggered background transition from dark (#0e0e0e) to a slightly lighter dark tone (#201f1f) spanning approximately one viewport height. The transition SHALL be gradual as the user scrolls, not snap. The page SHALL remain dark throughout — no white or near-white backgrounds SHALL appear.

#### Scenario: Background transitions on scroll
- **WHEN** the user scrolls past the hero section
- **THEN** the page background SHALL gradually shift from #0e0e0e to #201f1f
- **THEN** all text SHALL remain white/light colored with sufficient contrast against the dark background

### Requirement: Two-column content layout
Below the scroll-gradient, the product detail page SHALL render a dark (#201f1f) background content area within a `max-w-7xl` container. The left column (2/3 width) SHALL contain the product narrative. The right column (1/3 width) SHALL contain the acquisition form with sticky positioning. All text SHALL be white/light.

#### Scenario: Content area layout
- **WHEN** the product detail page content area renders
- **THEN** the content area background SHALL be #201f1f (dark)
- **THEN** the left column SHALL occupy 2/3 width with product narrative
- **THEN** the right column SHALL occupy 1/3 width with acquisition form
- **THEN** the acquisition form SHALL have sticky positioning

### Requirement: Product narrative content
The left column SHALL display: series label (e.g., "Series 01 / Artifact 04"), product title (Space Grotesk, text-6xl, bold, uppercase), manifesto section with `primary_container` left border, fabrication grid (2-column), and ethics section.

#### Scenario: Narrative elements present
- **WHEN** the product narrative section renders
- **THEN** series label, product title, manifesto, fabrication, and ethics SHALL all be visible

### Requirement: Acquisition form fields
The acquisition form SHALL contain four fields: Silhouette (dropdown with options: Boxy/Architectural, Tailored/Clinical, Oversized/Monastic), Waist measurement (cm, numeric input), Hips measurement (cm, numeric input), and Height measurement (cm, numeric input). Waist and Hips SHALL be in the same row (2-column grid). All inputs SHALL use underline-only style with `surface_container_low` background.

#### Scenario: All form fields present
- **WHEN** the acquisition form renders
- **THEN** silhouette dropdown, waist input, hips input, and height input SHALL all be visible
- **THEN** waist and hips SHALL be in the same row

#### Scenario: Input focus states
- **WHEN** an input field receives focus
- **THEN** the underline SHALL transition from default to `primary_container` color
- **THEN** no focus ring SHALL appear

### Requirement: Acquisition form submission
The form SHALL display a "Transaction Value" label with the product price, an "ACQUIRE ARTIFACT" button with `primary_container` background and gradient hover effect, and a shipping note. Clicking "ACQUIRE ARTIFACT" SHALL add the product with personalization to the cart store and update the header cart badge.

#### Scenario: ACQUIRE ARTIFACT adds to cart
- **WHEN** the user clicks "ACQUIRE ARTIFACT"
- **THEN** the product SHALL be added to the cart with selected personalization
- **THEN** the header cart badge count SHALL increment

#### Scenario: ACQUIRE ARTIFACT button styling
- **WHEN** the ACQUIRE ARTIFACT button renders
- **THEN** it SHALL have `primary_container` background, `on_surface` text, `py-6` padding
- **WHEN** hovered
- **THEN** a gradient from `primary_container` to #920703 SHALL appear

### Requirement: Detail grid section
Below the content area, the page SHALL maintain the dark aesthetic (`surface_container_lowest` #0e0e0e) and display an asymmetric 12-column grid: col-span-7 with a large product detail image (grayscale, opacity-60), col-span-5 with a secondary image offset by `mt-24` on a `surface_container_high` background.

#### Scenario: Detail grid renders
- **WHEN** the user scrolls below the white content area
- **THEN** a dark detail grid SHALL render with the asymmetric image layout

### Requirement: Mobile layout
On viewports below 768px, the product detail page SHALL collapse to single column: hero carousel, then description, then acquisition form below. All content SHALL remain accessible without horizontal scrolling at 320px width.

#### Scenario: Mobile single column
- **WHEN** the viewport is 320px wide
- **THEN** the two-column layout SHALL collapse to a single vertical column
- **THEN** no horizontal scrolling SHALL occur
