## ADDED Requirements

### Requirement: Real fashion photography in seed data
The seed script SHALL download real images from open-license sources (Unsplash/Pexels) and upload them to MinIO. Images SHALL be goth and 18th-century clothing themed, dark and moody, at least 500x500px. Image subjects SHALL include coats, dresses, skirts, pants, shirts, and basic accessories (belts, bags, gloves). Images SHALL NOT include boots, hoodies, sneakers, or other excluded items.

#### Scenario: Seed images are real photographs
- **WHEN** the seed script completes successfully
- **THEN** each of the 21 products SHALL have a real image in MinIO that is at least 500x500px and visually matches the product category

#### Scenario: Image licensing is compliant
- **WHEN** images are downloaded from external sources
- **THEN** all images SHALL be from sources that permit commercial use without attribution requirements (Unsplash License or Pexels License)

### Requirement: Seed data contains 21 products
The seed SQL SHALL create exactly 21 products distributed across 4 categories: outerwear (6), tops (6), bottoms (5), accessories (4). The footwear category SHALL be excluded entirely.

#### Scenario: Product count after seeding
- **WHEN** the seed SQL is executed against a fresh database
- **THEN** exactly 21 active products SHALL exist, with none in the footwear category

#### Scenario: Each product has one seed image
- **WHEN** the seed script completes
- **THEN** each of the 21 products SHALL have exactly 1 `product_image` record in the database and 1 corresponding image file in MinIO

### Requirement: Seed script downloads and uploads images
The seed script SHALL download images to a temporary directory, then upload each to MinIO at the correct object key path (`products/{productId}/{imageId}/original.jpg`). The script SHALL create the corresponding `product_image` database records.

#### Scenario: Seed script handles image download and upload
- **WHEN** `make seed` is executed with MinIO and PostgreSQL running
- **THEN** the script SHALL download images, upload to MinIO, and create `product_image` records for all 21 products

#### Scenario: Seed script is idempotent
- **WHEN** `make seed` is executed on an already-seeded database
- **THEN** the script SHALL either skip existing records or complete without errors (INSERT ... ON CONFLICT or cleanup logic)
