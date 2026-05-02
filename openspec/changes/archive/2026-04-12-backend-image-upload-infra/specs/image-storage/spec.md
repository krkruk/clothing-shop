## ADDED Requirements

### Requirement: MinIO bucket auto-creation
The system SHALL automatically create the configured MinIO bucket on application startup if it does not already exist.

#### Scenario: Bucket does not exist at startup
- **WHEN** the application starts and the configured bucket does not exist in MinIO
- **THEN** the system SHALL create the bucket before accepting any requests

#### Scenario: Bucket already exists at startup
- **WHEN** the application starts and the configured bucket already exists in MinIO
- **THEN** the system SHALL proceed normally without errors

### Requirement: MinIO bucket public read policy
The system SHALL set an anonymous read policy on the configured MinIO bucket on every startup, allowing unauthenticated `s3:GetObject` access to all objects in the bucket.

#### Scenario: Bucket policy set on startup
- **WHEN** the application starts
- **THEN** the system SHALL apply a bucket policy granting `s3:GetObject` to all principals (`*`) on the configured bucket, enabling public image serving through the nginx proxy

### Requirement: Image upload via AWS S3 SDK
The system SHALL use the AWS S3 SDK v2 (`software.amazon.awssdk:s3`) configured against MinIO as the S3-compatible backend. All MinIO connection details (endpoint, access key, secret key, bucket name, region) SHALL be injectable via Spring environment variables.

#### Scenario: Upload image to MinIO
- **WHEN** the product image upload endpoint is called with a valid file
- **THEN** the system SHALL call `s3Client.putObject()` with the object key `products/{productId}/{imageId}/original.{ext}` and the file bytes

#### Scenario: MinIO connection configured via environment variables
- **WHEN** the application starts
- **THEN** the S3 client SHALL be configured with `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`, and `MINIO_REGION` from the Spring environment

### Requirement: Image object key convention
The system SHALL use the object key format `products/{productId}/{imageId}/{variant}.ext` where `{variant}` is lowercase (e.g., `original`, `gallery`, `thumbnail`) and `{ext}` is the file extension derived from the content type.

#### Scenario: Original image key generation
- **WHEN** an image is uploaded with content type `image/jpeg` for product ID `abc-123` and generated image ID `def-456`
- **THEN** the object key SHALL be `products/abc-123/def-456/original.jpg`

### Requirement: Product image database record
The system SHALL persist image metadata in a `product_image` table with columns: `id` (UUID PK), `product_id` (UUID FK), `object_key` (VARCHAR), `variant` (ENUM: ORIGINAL, GALLERY, THUMBNAIL, default ORIGINAL), `alt` (VARCHAR 200), `display_order` (INTEGER), `created_at` (TIMESTAMPTZ).

#### Scenario: Image record created on upload
- **WHEN** an image is successfully uploaded to MinIO
- **THEN** the system SHALL insert a `product_image` row with `variant=ORIGINAL`, the generated object key, and auto-incremented `display_order`

### Requirement: Image variant schema readiness
The `product_image` table SHALL include a `variant` ENUM column with values `ORIGINAL`, `GALLERY`, `THUMBNAIL` to support future image resizing. Only `ORIGINAL` SHALL be used in this change.

#### Scenario: Future variant support
- **WHEN** image resizing is implemented in a future change
- **THEN** the system SHALL insert additional rows with `variant=GALLERY` and `variant=THUMBNAIL` using the same object key prefix without requiring a database migration
