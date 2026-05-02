## MODIFIED Requirements

### Requirement: Create product endpoint
The `POST /api/v1/admin/products` endpoint SHALL accept `name`, `description`, `shortDescription`, `price` (BigDecimal), `currency` (string, default "PLN"), `categoryId` (UUID), `sku` (string, optional), `isActive` (boolean, default true), `fabrication` (content, care), and `ethics` (origin, impact). Returns 201 with created product data including the new fields.

#### Scenario: Create product with all new fields
- **WHEN** admin submits a product with price "299.99", currency "PLN", sku "OBS-001-B", isActive true
- **THEN** the product is created with price stored as BigDecimal, currency "PLN", sku "OBS-001-B", isActive true
- **AND** the response includes all new fields

#### Scenario: Create product without optional fields
- **WHEN** admin submits a product without sku and ethics fields
- **THEN** the product is created with sku null, ethics null, currency defaults to "PLN", isActive defaults to true

#### Scenario: Validation rejects negative price
- **WHEN** admin submits a product with price -10.00
- **THEN** the response is 422 with a ProblemDetail validation error on the price field

#### Scenario: Validation rejects duplicate SKU
- **WHEN** admin submits a product with an SKU that already exists
- **THEN** the response is 422 with a ProblemDetail error indicating duplicate SKU

## ADDED Requirements

### Requirement: Update product endpoint
The system SHALL provide a `PUT /api/v1/admin/products/{id}` endpoint (HTTP Basic auth) that accepts partial updates. Only non-null fields in the request body SHALL be updated.

#### Scenario: Update product name only
- **WHEN** admin sends PUT with only `name` field changed
- **THEN** only the name is updated, all other fields remain unchanged

#### Scenario: Update returns updated product
- **WHEN** admin sends a valid update request
- **THEN** the response is 200 with the full updated product data

#### Scenario: Update non-existent product
- **WHEN** admin sends PUT for a product ID that doesn't exist
- **THEN** the response is 404 with ProblemDetail

#### Scenario: Update requires authentication
- **WHEN** admin sends PUT without credentials
- **THEN** the response is 401 with ProblemDetail

### Requirement: Delete product endpoint
The system SHALL provide a `DELETE /api/v1/admin/products/{id}` endpoint (HTTP Basic auth) that performs a soft delete by setting `is_active = false`.

#### Scenario: Soft delete product
- **WHEN** admin sends DELETE for an active product
- **THEN** the product's `is_active` is set to false
- **AND** the response is 204 No Content

#### Scenario: Delete already inactive product
- **WHEN** admin sends DELETE for an already soft-deleted product
- **THEN** the response is still 204 No Content (idempotent)

#### Scenario: Delete non-existent product
- **WHEN** admin sends DELETE for a product ID that doesn't exist
- **THEN** the response is 404 with ProblemDetail

### Requirement: Admin list products endpoint
The system SHALL provide a `GET /api/v1/admin/products` endpoint (HTTP Basic auth) that returns a paginated list of ALL products (including inactive), ordered by `created_at` desc, using cursor-based pagination.

#### Scenario: List all products including inactive
- **WHEN** admin sends GET to `/api/v1/admin/products`
- **THEN** the response includes both active and inactive products

#### Scenario: Cursor pagination for admin listing
- **WHEN** admin sends GET with a `cursor` parameter
- **THEN** products after the cursor are returned with `nextCursor` and `hasMore` fields

#### Scenario: Admin listing requires authentication
- **WHEN** unauthenticated request is made to `/api/v1/admin/products`
- **THEN** the response is 401

### Requirement: Price as BigDecimal
The `price` field SHALL be stored and transmitted as `DECIMAL(10,2)` / BigDecimal to avoid floating-point precision loss. In OpenAPI spec, the type SHALL be `string` with `format: decimal` to ensure exact decimal representation in JSON.

#### Scenario: Price precision preserved
- **WHEN** product is created with price "299.99"
- **THEN** the stored and returned price is exactly "299.99" with no floating-point artifacts
