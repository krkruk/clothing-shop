## ADDED Requirements

### Requirement: Cart drawer slide-in behavior
The cart SHALL render as a slide-in drawer from the right side of the viewport. On desktop, the drawer SHALL be 450px wide. On mobile (<768px), the drawer SHALL be full-width. The drawer SHALL transition using `translate-x-full` to `translate-x-0` over 500ms. A backdrop (`bg-black/60 backdrop-blur-sm`) SHALL appear behind the drawer and close the drawer on click.

#### Scenario: Open cart drawer
- **WHEN** the user clicks the cart icon in the header
- **THEN** the drawer SHALL slide in from the right over 500ms
- **THEN** a semi-transparent backdrop SHALL appear

#### Scenario: Close cart drawer via backdrop
- **WHEN** the cart drawer is open and the user clicks the backdrop
- **THEN** the drawer SHALL slide out and the backdrop SHALL fade

#### Scenario: Close cart drawer via close button
- **WHEN** the user clicks the close button inside the drawer
- **THEN** the drawer SHALL slide out

### Requirement: Cart drawer styling
The drawer background SHALL be `surface_container_highest` (#353534) at 90% opacity with 12px backdrop blur (smoked glass effect). The header SHALL display "CURRENT INVENTORY" in Space Grotesk bold, text-2xl, tracking-tighter, uppercase. No dividers SHALL separate cart items — 24px vertical padding instead.

#### Scenario: Smoked glass styling
- **WHEN** the cart drawer is open
- **THEN** the background SHALL be semi-transparent with backdrop blur

### Requirement: Cart item display
Each cart item SHALL display: a 96x128px thumbnail with grayscale brightness-50 treatment, series/artifact label in `primary_container`, item name in Space Grotesk bold uppercase, size/variant text in Manrope 10px uppercase, inline quantity controls ([−] count [+]), and price in Space Grotesk bold.

#### Scenario: Cart item renders all fields
- **WHEN** a cart item is displayed
- **THEN** thumbnail, name, variant, quantity controls, and price SHALL all be visible

#### Scenario: Quantity controls function
- **WHEN** the user clicks [+] on a cart item
- **THEN** the item quantity SHALL increment by 1
- **WHEN** the user clicks [−] on a cart item with quantity > 1
- **THEN** the item quantity SHALL decrement by 1
- **WHEN** the user clicks [−] on a cart item with quantity 1
- **THEN** the item SHALL be removed from the cart

### Requirement: Cart footer
The drawer footer SHALL display "TOTAL VALUE" label and derived total amount. A "PROCEED TO TRANSACTION" button SHALL be present with `primary_container` background. A separator SHALL use `outline/20` top border.

#### Scenario: Cart footer with total
- **WHEN** the cart drawer is open with items
- **THEN** "TOTAL VALUE" label and computed total SHALL be visible
- **THEN** "PROCEED TO TRANSACTION" button SHALL be visible

### Requirement: Cart empty state
When the cart has no items, the drawer SHALL display a centered `inventory_2` icon (60px, 30% opacity) and text "INVENTORY IS CURRENTLY EMPTY" in Manrope text-sm uppercase tracking-widest.

#### Scenario: Empty cart display
- **WHEN** the cart store has zero items and the drawer is opened
- **THEN** the empty state icon and text SHALL be displayed
- **THEN** no cart items or total SHALL be shown

### Requirement: Cart accessible from all pages
The cart drawer SHALL be accessible from the header cart icon on every page (landing page, product detail page). The cart state SHALL persist across page navigation.

#### Scenario: Cart opens from product detail page
- **WHEN** the user is on the product detail page and clicks the cart icon
- **THEN** the cart drawer SHALL open with the same state as on the landing page
