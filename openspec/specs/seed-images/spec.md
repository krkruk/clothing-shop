## ADDED Requirements

### Requirement: Personal product photography in seed data
The seed images SHALL be committed to the repository in `infra/minio/seed/images/`. Images are personal product photographs grouped by product key prefix. Each product may have multiple images. Images SHALL be uploaded to MinIO with corresponding `product_image` database records.

#### Scenario: Seed images are committed photographs
- **WHEN** the seed script completes successfully
- **THEN** each product SHALL have all its matching images in MinIO, with corresponding `product_image` records in the database

### Requirement: Seed data contains 5 products
The seed SQL SHALL create exactly 5 products distributed across 2 categories: tops (3), coats (2). Each product has PLN and EUR prices, fabrication details, and ethics information.

#### Scenario: Product count after seeding
- **WHEN** the seed SQL is executed against a fresh database
- **THEN** exactly 5 active products SHALL exist

#### Scenario: Each product has multiple seed images
- **WHEN** the seed script completes
- **THEN** each product SHALL have its matching images (by filename prefix) uploaded to MinIO, with one `product_image` record per image and incrementing `display_order`

### Requirement: Seed script uploads local images to MinIO
The seed script SHALL match images from `infra/minio/seed/images/` by product key prefix, upload each to MinIO at the correct object key path (`products/{productId}/{imageId}/original.jpg`). The script SHALL create the corresponding `product_image` database records.

#### Scenario: Seed script handles image upload
- **WHEN** `make seed` is executed with MinIO and PostgreSQL running
- **THEN** the script SHALL upload all matching images to MinIO and create `product_image` records for all 5 products

#### Scenario: Seed script is idempotent
- **WHEN** `make seed` is executed on an already-seeded database
- **THEN** the script SHALL either skip existing records or complete without errors (INSERT ... ON CONFLICT or cleanup logic)
