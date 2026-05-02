## ADDED Requirements

### Requirement: Contract-first API via OpenAPI
The system SHALL define the API contract in `openapi/spec.yaml` as the single source of truth. The Gradle build SHALL use openapi-generator-gradle-plugin with the `spring` generator configured as `interfaceOnly=true` and `useTags=true` to generate API interfaces and model classes into `backend/src/main/generated/`. The generated code SHALL be gitignored and regenerated on each build. The controller SHALL implement the generated API interface and use generated model classes as method arguments and return types — no hand-written DTOs or mapper classes.

#### Scenario: Generated interface defines controller contract
- **WHEN** the Gradle build runs
- **THEN** the openapi-generator SHALL produce an `AdminProductsApi` interface (from the `admin-products` tag in the spec) with method signatures matching the OpenAPI operations, and model classes matching the OpenAPI schemas

#### Scenario: Controller implements generated interface
- **WHEN** the `AdminProductController` is created
- **THEN** it SHALL implement the generated `AdminProductsApi` interface, using generated model types (e.g., `CreateProductRequest`, `ProductResponse`) directly — no parallel hand-written DTO hierarchy

### Requirement: Create product endpoint
The system SHALL expose `POST /api/v1/admin/products` for creating a new product. The endpoint SHALL require HTTP Basic authentication with credentials `admin:admin`.

#### Scenario: Successful product creation
- **WHEN** a POST request is sent to `/api/v1/admin/products` with valid Basic auth, a JSON body containing `name` (2-200 chars), `description` (TEXT), `shortDescription` (TEXT), `price` (decimal > 0, 2 decimal places), `categoryId` (UUID referencing existing category), and optional `fabrication` and `ethics` objects
- **THEN** the system SHALL return HTTP 201 with a JSON body containing the server-generated `id` (UUID), all submitted fields, a `category` object with `slug` and `name`, and an empty `images` array

#### Scenario: Unauthenticated request
- **WHEN** a POST request is sent to `/api/v1/admin/products` without Basic auth credentials
- **THEN** the system SHALL return HTTP 401 with `application/problem+json` and `WWW-Authenticate: Basic realm="Clothingshop Admin"` header

#### Scenario: Category not found
- **WHEN** a POST request is sent with a `categoryId` that does not exist in the database
- **THEN** the system SHALL return HTTP 422 with `application/problem+json`, error type `validation`, and detail indicating the category was not found

#### Scenario: Validation failure on required fields
- **WHEN** a POST request is sent with missing or invalid required fields (name too short, price <= 0, etc.)
- **THEN** the system SHALL return HTTP 422 with `application/problem+json`, error type `validation`, and a `fields` array listing each invalid field with its error message

### Requirement: Upload product image endpoint
The system SHALL expose `POST /api/v1/admin/products/{id}/image` for uploading an image to MinIO and linking it to a product. The endpoint SHALL require HTTP Basic authentication. The request SHALL use `multipart/form-data` with a `file` part (the image binary) and an optional `alt` text field (max 200 chars). The system SHALL upload the file to MinIO via the S3 SDK, insert a `product_image` database record, and return the image metadata.

#### Scenario: Successful image upload
- **WHEN** a POST request is sent to `/api/v1/admin/products/{id}/image` with valid Basic auth, a multipart file of type `image/jpeg`, `image/png`, or `image/webp`, under 5MB, and an optional `alt` text
- **THEN** the system SHALL upload the file to MinIO with object key `products/{productId}/{imageId}/original.{ext}`, insert a `product_image` row with `variant=ORIGINAL`, and return HTTP 201 with `{ imageId, imageUrl }` where `imageUrl` is the relative path `/{objectKey}`. The uploaded image is publicly downloadable at `http://localhost:8080/images/{objectKey}` via the nginx proxy

#### Scenario: Product not found
- **WHEN** a POST request is sent with a product ID that does not exist in the database
- **THEN** the system SHALL return HTTP 404 with `application/problem+json`

#### Scenario: File exceeds size limit
- **WHEN** a POST request is sent with a file larger than 5MB
- **THEN** the system SHALL return HTTP 422 with `application/problem+json` and an error indicating the file size exceeds the limit

#### Scenario: Unsupported content type
- **WHEN** a POST request is sent with a file of type other than `image/jpeg`, `image/png`, or `image/webp`
- **THEN** the system SHALL return HTTP 422 with `application/problem+json` and an error indicating the content type is not supported

### Requirement: RFC 9457 error responses
All error responses from admin endpoints SHALL use RFC 9457 Problem Details format with content type `application/problem+json`. Each error response SHALL contain `type` (URI), `title` (string), `status` (integer), and `detail` (string).

#### Scenario: Any error response format
- **WHEN** any request results in an error (4xx or 5xx)
- **THEN** the response SHALL have content type `application/problem+json` with fields `type`, `title`, `status`, and `detail`

#### Scenario: Validation error with field details
- **WHEN** a validation error occurs
- **THEN** the response SHALL include a `fields` extension member listing per-field errors as `{ "field": "...", "message": "..." }`
