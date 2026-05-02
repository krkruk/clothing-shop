## ADDED Requirements

### Requirement: Header layout and positioning
The header SHALL be fixed at the top of the viewport with `z-50`, full width, 80px height, and `#0e0e0e` background. It SHALL display on all pages. Main content SHALL have `pt-20` (80px) top padding to account for the fixed header.

#### Scenario: Header fixed positioning
- **WHEN** any page loads
- **THEN** the header SHALL be fixed at the top with 80px height
- **THEN** page content SHALL start below the header with no overlap

### Requirement: Brand name display
The header SHALL display "CLOTHINGSHOP" in Space Grotesk, bold, tracking-[0.2em], uppercase, #e5e2e1 color. The brand name SHALL be a link to the landing page.

#### Scenario: Brand name navigates home
- **WHEN** the user clicks "CLOTHINGSHOP"
- **THEN** the application SHALL navigate to the landing page

### Requirement: Category navigation links
The header SHALL display three navigation links: TOPS, BOTTOMS, ACCESSORIES in Space Grotesk, tracking-tighter, uppercase, text-sm. On hover, text SHALL transition to #ffb4a8 and background SHALL shift to #1c1c1c.

#### Scenario: Nav links visible on desktop
- **WHEN** the viewport is 768px or wider
- **THEN** TOPS, BOTTOMS, ACCESSORIES links SHALL be visible in the header

### Requirement: Category dropdown menus
Each category link SHALL reveal a dropdown on hover with sub-categories: TOPS (Shirts, Outerwear, Knitwear), BOTTOMS (Pants, Skirts, Trousers), ACCESSORIES (Belts, Bags, Jewelry). The dropdown SHALL have `surface_container_high` background, 2px `primary_container` top border, and fade-in over 300ms.

#### Scenario: Dropdown appears on hover
- **WHEN** the user hovers over "TOPS"
- **THEN** a dropdown SHALL appear with Shirts, Outerwear, Knitwear links
- **THEN** the dropdown SHALL have a 2px `primary_container` top border

### Requirement: User icon button
The header SHALL display a Material Symbols Outlined `person` glyph (wght 200) in #e5e2e1 color. On hover, color SHALL transition to #ffb4a8.

#### Scenario: User icon present
- **WHEN** any page loads
- **THEN** a person icon SHALL be visible in the header

### Requirement: Cart icon with badge
The header SHALL display a Material Symbols Outlined `shopping_bag` glyph (wght 200) in #e5e2e1 color. A badge SHALL display the current cart item count using `primary_container` background and 8px font. When cart is empty, the badge SHALL show "0".

#### Scenario: Cart badge shows count
- **WHEN** the cart contains 3 items
- **THEN** the cart icon badge SHALL display "3"

#### Scenario: Cart icon opens drawer
- **WHEN** the user clicks the cart icon
- **THEN** the cart drawer SHALL open

### Requirement: Mobile hamburger menu
On viewports below 768px, the navigation links SHALL collapse. A hamburger menu icon SHALL appear. Clicking it SHALL reveal a slide-out menu with the category links.

#### Scenario: Mobile nav collapsed
- **WHEN** the viewport is below 768px
- **THEN** the category links SHALL be hidden
- **THEN** a hamburger menu icon SHALL be visible

#### Scenario: Hamburger opens mobile nav
- **WHEN** the user taps the hamburger icon
- **THEN** a menu SHALL appear with TOPS, BOTTOMS, ACCESSORIES links
