## ADDED Requirements

### Requirement: Footer links
The footer SHALL display four links: INVENTORY, TRANSACTIONS, LEGAL, MANIFESTO. All links SHALL use Manrope font, text-[10px], uppercase, tracking-[0.3em]. Default color SHALL be #353534. On hover, color SHALL transition to #e5e2e1 over 700ms. The MANIFESTO link SHALL always be #ffb4a8 (primary color).

#### Scenario: Footer links present
- **WHEN** any page loads and the user scrolls to the bottom
- **THEN** INVENTORY, TRANSACTIONS, LEGAL, MANIFESTO links SHALL be visible

#### Scenario: MANIFESTO link distinct color
- **WHEN** the footer renders
- **THEN** the MANIFESTO link SHALL be #ffb4a8, distinct from the other links

#### Scenario: Footer link hover effect
- **WHEN** the user hovers over INVENTORY, TRANSACTIONS, or LEGAL
- **THEN** the link color SHALL transition to #e5e2e1 over 700ms

### Requirement: Footer copyright
The footer SHALL display "© MMXXIV CLOTHINGSHOP. ALL RIGHTS RESERVED." in Manrope text-[10px] uppercase tracking-[0.3em] with #353534 color (or #5c0000 bold per mockup).

#### Scenario: Copyright text present
- **WHEN** the footer renders
- **THEN** the copyright text SHALL be visible

### Requirement: Footer on all pages
The footer SHALL appear at the bottom of every page (landing page, product detail page).

#### Scenario: Footer on landing page
- **WHEN** the user scrolls to the bottom of the landing page
- **THEN** the footer SHALL be visible

#### Scenario: Footer on product detail page
- **WHEN** the user scrolls to the bottom of the product detail page
- **THEN** the footer SHALL be visible
