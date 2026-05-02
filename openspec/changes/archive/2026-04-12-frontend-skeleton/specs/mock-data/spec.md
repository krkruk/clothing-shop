## ADDED Requirements

### Requirement: TypeScript type definitions
The mock data module SHALL export TypeScript interfaces for: `Product` (id, name, description, shortDescription, price, categoryId, fabrication, ethics, images[]), `Category` (id, name, slug, description, displayOrder, subCategories[]), `CartItem` (id, productId, productName, price, quantity, thumbnail, personalization), `Personalization` (silhouette, waistCm, hipsCm, heightCm), `Silhouette` enum (BOXY, CURVY, OTHER).

#### Scenario: Types are importable
- **WHEN** a component imports `Product` from `$lib/mock/types`
- **THEN** TypeScript SHALL recognize the type without errors

### Requirement: Sample product data
The mock module SHALL provide at least 6 sample products distributed across 3 categories (2+ per category). Each product SHALL have all required fields populated with realistic editorial content matching the Clothingshop brand voice.

#### Scenario: Products cover all categories
- **WHEN** the mock data is loaded
- **THEN** at least 2 products SHALL exist per category (TOPS, BOTTOMS, ACCESSORIES)

### Requirement: Sample category data
The mock module SHALL provide 3 categories: TOPS (sub-categories: Shirts, Outerwear, Knitwear), BOTTOMS (sub-categories: Pants, Skirts, Trousers), ACCESSORIES (sub-categories: Belts, Bags, Jewelry).

#### Scenario: Category structure matches PRD
- **WHEN** the mock categories are loaded
- **THEN** each category SHALL have the correct sub-categories as defined in prd-ui-ux.md

### Requirement: Placeholder image URLs
The mock module SHALL provide image URLs for products. These MAY be external placeholder URLs or local assets. All images SHALL be accessible (no broken images).

#### Scenario: Product images load
- **WHEN** a product's image URL is used in an `<img>` tag
- **THEN** an image SHALL render without a broken image indicator
