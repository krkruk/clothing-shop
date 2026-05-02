## MODIFIED Requirements

### Requirement: Placeholder image URLs
The mock module's image URL exports (`heroImages`, `lookbookImages`, `getProductThumbnail`) are no longer consumed by the landing page. These exports SHALL remain available for other pages that still import from the mock module. The landing page SHALL NOT import from `$lib/mock`.

#### Scenario: Mock image exports still function
- **WHEN** other components import `heroImages` or `lookbookImages` from `$lib/mock`
- **THEN** the exports SHALL still return valid data structures

#### Scenario: Landing page does not use mock data
- **WHEN** the landing page (`+page.svelte`) is rendered
- **THEN** it SHALL NOT import from `$lib/mock` for product or image data
