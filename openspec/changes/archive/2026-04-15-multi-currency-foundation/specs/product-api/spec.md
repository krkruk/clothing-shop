## MODIFIED Requirements

### Requirement: Create product endpoint
The `POST /api/v1/admin/products` endpoint SHALL accept `name`, `description`, `shortDescription`, `prices` (array of `{currency, price}` objects, exactly 2 entries for PLN and EUR, both required), `categoryId` (UUID), `sku` (string, optional), `isActive` (boolean, default true), `fabrication` (content, care), and `ethics` (origin, impact). The `price` and `currency` top-level fields SHALL be removed from the request schema. Returns 201 with created product data including all prices.

#### Scenario: Create product with both currency prices
- **WHEN** admin submits a product with prices [{currency: "PLN", price: "399.00"}, {currency: "EUR", price: "89.00"}], sku "OBS-001-B", isActive true
- **THEN** the product is created with both prices stored in `product_price` table
- **AND** the response includes all fields and the full prices array

#### Scenario: Create product without required currency price
- **WHEN** admin submits a product with only PLN price (missing EUR)
- **THEN** the response is 422 with a ProblemDetail validation error requiring EUR price

#### Scenario: Validation rejects negative price
- **WHEN** admin submits a product with a negative price in any currency
- **THEN** the response is 422 with a ProblemDetail validation error on the price field

#### Scenario: Validation rejects duplicate SKU
- **WHEN** admin submits a product with an SKU that already exists
- **THEN** the response is 422 with a ProblemDetail error indicating duplicate SKU

### Requirement: Update product endpoint
The system SHALL provide a `PUT /api/v1/admin/products/{id}` endpoint (HTTP Basic auth) that accepts partial updates. The `prices` field, if provided, SHALL replace all existing prices for the product. Only non-null fields in the request body SHALL be updated.

#### Scenario: Update product name only
- **WHEN** admin sends PUT with only `name` field changed
- **THEN** only the name is updated, prices and other fields remain unchanged

#### Scenario: Update prices
- **WHEN** admin sends PUT with a new `prices` array
- **THEN** all existing prices for the product are replaced with the new prices

#### Scenario: Update returns updated product
- **WHEN** admin sends a valid update request
- **THEN** the response is 200 with the full updated product data including all prices

#### Scenario: Update non-existent product
- **WHEN** admin sends PUT for a product ID that doesn't exist
- **THEN** the response is 404 with ProblemDetail

#### Scenario: Update requires authentication
- **WHEN** admin sends PUT without credentials
- **THEN** the response is 401 with ProblemDetail

### Requirement: Price as BigDecimal
Each price in the `prices` array SHALL be stored and transmitted as `DECIMAL(10,2)` / BigDecimal to avoid floating-point precision loss. In OpenAPI spec, the type SHALL be `string` with `format: decimal`.

#### Scenario: Price precision preserved
- **WHEN** product is created with price "299.99" for PLN
- **THEN** the stored and returned price is exactly "299.99" with no floating-point artifacts

## ADDED Requirements

### Requirement: Product response includes all prices
The `ProductResponse` DTO (used by admin endpoints) SHALL include a `prices` field containing an array of `{currency, price}` objects for all currencies associated with the product.

#### Scenario: Admin product response shows all prices
- **WHEN** admin fetches a product via `GET /api/v1/admin/products`
- **THEN** each product item includes `prices: [{currency: "PLN", price: "399.00"}, {currency: "EUR", price: "89.00"}]`

### Requirement: OpenAPI spec currency header parameter
The `GET /products` and `GET /products/{id}` endpoints SHALL accept an optional `x-currency-code` HTTP header parameter (string, default "PLN") that determines which currency's price is returned in the response.

#### Scenario: Spec defines header parameter
- **WHEN** the OpenAPI spec is read
- **THEN** `GET /products` and `GET /products/{id}` define an `x-currency-code` header parameter in their parameters list

### Requirement: ProductPriceDto schema
The OpenAPI spec SHALL define a `ProductPriceDto` schema with `currency` (string, min 3, max 3) and `price` (string, format decimal).

#### Scenario: Schema available for code generation
- **WHEN** `make generate` is run
- **THEN** both backend and frontend generated code include the `ProductPriceDto` type

## REMOVED Requirements

### Requirement: Admin list products endpoint
**Reason**: Moved to `multi-currency-pricing` capability spec. The endpoint behavior (paginated list of all products including inactive) is unchanged — only the response shape changes to include `prices` array instead of single `price`/`currency`.
**Migration**: The admin list endpoint now returns `ProductResponse` items with a `prices` array field.
