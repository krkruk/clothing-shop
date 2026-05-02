## MODIFIED Requirements

### Requirement: Upload product image endpoint
The system SHALL expose `POST /api/v1/admin/products/{id}/image` for uploading an image to MinIO and linking it to a product. The endpoint SHALL require HTTP Basic authentication. The request SHALL use `multipart/form-data` with a `file` part (the image binary) and an optional `alt` text field (max 200 chars). The system SHALL upload the file to MinIO via the S3 SDK, insert a `product_image` database record, and return the image metadata.

#### Scenario: Successful image upload
- **WHEN** a POST request is sent to `/api/v1/admin/products/{id}/image` with valid Basic auth, a multipart file of type `image/jpeg`, `image/png`, or `image/webp`, under 5MB, and an optional `alt` text
- **THEN** the system SHALL upload the file to MinIO with object key `products/{productId}/{imageId}/original.{ext}`, insert a `product_image` row with `variant=ORIGINAL`, and return HTTP 201 with `{ imageId, imageUrl }` where `imageUrl` is the relative path `/images/{objectKey}` (changed from `/{objectKey}`). The uploaded image is publicly downloadable at `http://localhost:8080/images/{objectKey}` via the nginx proxy.

#### Scenario: Product not found
- **WHEN** a POST request is sent with a product ID that does not exist in the database
- **THEN** the system SHALL return HTTP 404 with `application/problem+json`

#### Scenario: File exceeds size limit
- **WHEN** a POST request is sent with a file larger than 5MB
- **THEN** the system SHALL return HTTP 422 with `application/problem+json` and an error indicating the file size exceeds the limit

#### Scenario: Unsupported content type
- **WHEN** a POST request is sent with a file of type other than `image/jpeg`, `image/png`, or `image/webp`
- **THEN** the system SHALL return HTTP 422 with `application/problem+json` and an error indicating the content type is not supported

## ADDED Requirements

### Requirement: Public product list endpoint in OpenAPI spec
The OpenAPI spec at `openapi/spec.yaml` SHALL define `GET /products` under a new `products` tag. The endpoint SHALL accept optional query parameters `cursor` (string), `limit` (integer, default 20, max 100), and `category` (string). The response SHALL use schema `ProductListResponse`.

#### Scenario: OpenAPI spec defines list endpoint
- **WHEN** the OpenAPI spec is generated
- **THEN** it SHALL contain a `GET /products` path with a `products` tag, query parameters for `cursor`, `limit`, and `category`, a 200 response with `ProductListResponse` schema, and a 400 response for invalid cursor.

### Requirement: Public product detail endpoint in OpenAPI spec
The OpenAPI spec SHALL define `GET /products/{id}` under the `products` tag. The path parameter `id` SHALL be a UUID. The response SHALL use schema `ProductDetailResponse`. A 404 response SHALL use `ProblemDetail`.

#### Scenario: OpenAPI spec defines detail endpoint
- **WHEN** the OpenAPI spec is generated
- **THEN** it SHALL contain a `GET /products/{id}` path with a `products` tag, a UUID path parameter `id`, a 200 response with `ProductDetailResponse` schema, and a 404 response with `ProblemDetail`.

### Requirement: ProductListResponse schema
The OpenAPI spec SHALL define a `ProductListResponse` schema with `items` (array of `ProductSummary`), `nextCursor` (string, nullable), and `hasMore` (boolean).

#### Scenario: Schema validates list response
- **WHEN** the backend returns a product list response
- **THEN** it SHALL conform to the `ProductListResponse` schema with all required fields present.

### Requirement: ProductSummary schema
The OpenAPI spec SHALL define a `ProductSummary` schema with `id` (UUID), `name` (string), `price` (number), `imageUrl` (string, nullable), `shortDescription` (string), and `category` (`CategoryDto`).

#### Scenario: Schema validates list item
- **WHEN** the backend returns a product summary
- **THEN** it SHALL conform to the `ProductSummary` schema.

### Requirement: ProductDetailResponse schema
The OpenAPI spec SHALL define a `ProductDetailResponse` schema with all fields from `ProductSummary` plus `description` (string), `images` (array of `ProductImageDto`), `fabrication` (`FabricationDto`, nullable), and `ethics` (`EthicsDto`, nullable).

#### Scenario: Schema validates detail response
- **WHEN** the backend returns a product detail response
- **THEN** it SHALL conform to the `ProductDetailResponse` schema with all required fields present.

### Requirement: ProductImageDto schema
The OpenAPI spec SHALL define a `ProductImageDto` schema with `imageId` (UUID), `imageUrl` (string, URI format), `alt` (string, nullable), and `displayOrder` (integer).

#### Scenario: Schema validates image entry
- **WHEN** the backend returns a product image entry
- **THEN** it SHALL conform to the `ProductImageDto` schema.
