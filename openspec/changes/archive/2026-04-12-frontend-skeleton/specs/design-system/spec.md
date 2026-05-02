## ADDED Requirements

### Requirement: Tailwind design tokens
The project SHALL configure Tailwind CSS with all Obsidian Monolith color tokens from `prd-ui-ux.md` color palette table, including: `surface_container_lowest` (#0e0e0e), `surface_dim` (#131313), `surface_container_low` (#1c1b1b), `surface_container` (#201f1f), `surface_container_high` (#2a2a2a), `surface_container_highest` (#353534), `primary` (#ffb4a8), `primary_container` (#5c0000), `on_primary_fixed_variant` (#920703), `on_surface` (#e5e2e1), `on_secondary_container` (#b6b5b4), `outline` (#a68a86), `outline_variant` (#57423e).

#### Scenario: Color tokens available in templates
- **WHEN** a Svelte component uses a Tailwind class like `bg-surface-container-lowest`
- **THEN** the rendered element SHALL have the correct background color (#0e0e0e)

### Requirement: Border radius override
All Tailwind default border radius values SHALL be overridden to 0px. The `full` token (9999px) SHALL be preserved for pill-shaped elements if needed.

#### Scenario: No rounded corners on any element
- **WHEN** a component uses `rounded`, `rounded-lg`, or `rounded-xl` Tailwind classes
- **THEN** the element SHALL render with 0px border radius

### Requirement: Font families
The project SHALL configure two font families: `headline` (Space Grotesk, weights 300-700) and `body`/`label` (Manrope, weights 200-600). Google Fonts SHALL be loaded via `<link>` tags in `app.html`. Material Symbols Outlined SHALL be loaded with wght 200 for icon usage.

#### Scenario: Font family classes available
- **WHEN** a component uses `font-headline` class
- **THEN** the element SHALL render with Space Grotesk font family
- **WHEN** a component uses `font-body` class
- **THEN** the element SHALL render with Manrope font family

### Requirement: Global styles
The project SHALL define global CSS for: custom scrollbar (4px width, #0e0e0e track, #5c0000 thumb), text selection colors (`primary_container` background, `on_surface` text), and Material Symbols Outlined font-variation-settings (FILL 0, wght 200).

#### Scenario: Custom scrollbar visible
- **WHEN** page content overflows vertically
- **THEN** the scrollbar SHALL be 4px wide with #0e0e0e track and #5c0000 thumb colors

### Requirement: Content white token
The design system SHALL include a `content-white` color token (#f5f5f5) used exclusively for the product detail page content area background.

#### Scenario: Product page content background
- **WHEN** the product detail page content area renders
- **THEN** the background SHALL be #f5f5f5
