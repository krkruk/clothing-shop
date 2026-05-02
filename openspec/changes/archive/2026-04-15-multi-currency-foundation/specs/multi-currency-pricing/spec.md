## ADDED Requirements

### Requirement: ProductPrice database entity
The system SHALL store product prices in a dedicated `product_price` table with columns: `id` (UUID, primary key), `product_id` (UUID, foreign key to `product`), `currency` (VARCHAR(3), non-null), `price` (DECIMAL(10,2), non-null). A UNIQUE constraint SHALL exist on `(product_id, currency)`.

#### Scenario: Product with two prices stored
- **WHEN** a product is created with PLN price 399.00 and EUR price 89.00
- **THEN** two rows exist in `product_price` — one with currency "PLN" and price 399.00, one with currency "EUR" and price 89.00

#### Scenario: Duplicate currency rejected
- **WHEN** an attempt is made to insert two prices with the same currency for the same product
- **THEN** the database UNIQUE constraint rejects the insertion

### Requirement: Consolidated Liquibase migration
All database migrations SHALL be consolidated into a single Liquibase changelog file. The `product` table SHALL NOT have `price` or `currency` columns. The `product_price` table SHALL be created with the schema above.

#### Scenario: Fresh environment setup
- **WHEN** the application starts against an empty database
- **THEN** a single migration creates all tables: `category`, `product` (without price/currency), `product_price`, `product_image`
- **AND** the seed data includes PLN and EUR prices for every product

### Requirement: Supported currencies are PLN and EUR
The system SHALL support exactly two currencies: PLN (default) and EUR. Product creation and update SHALL require prices for both currencies.

#### Scenario: Product created with only PLN price
- **WHEN** admin submits a product with a PLN price but no EUR price
- **THEN** the system returns a 422 validation error requiring EUR price

#### Scenario: Unsupported currency rejected
- **WHEN** a price is submitted with currency "USD"
- **THEN** the system returns a 422 validation error

### Requirement: Currency resolution via HTTP header
The public product endpoints (`GET /api/v1/products`, `GET /api/v1/products/{id}`) SHALL read the `x-currency-code` HTTP header to determine which currency's price to return. If the header is absent or contains an unsupported value, the system SHALL default to PLN.

#### Scenario: Request with EUR header
- **WHEN** a request is made with header `x-currency-code: EUR`
- **THEN** the response includes the product's EUR price and currency "EUR"

#### Scenario: Request with no currency header
- **WHEN** a request is made without the `x-currency-code` header
- **THEN** the response includes the product's PLN price and currency "PLN"

#### Scenario: Request with unsupported currency header
- **WHEN** a request is made with header `x-currency-code: JPY`
- **THEN** the response defaults to PLN price and currency "PLN"

### Requirement: ProductPrice JPA entity and repository
The system SHALL provide a `ProductPrice` JPA entity and a `ProductPriceRepository` Spring Data JPA interface. The repository SHALL support finding prices by product ID and currency.

#### Scenario: Find price by product and currency
- **WHEN** the service queries for a product's EUR price
- **THEN** the repository returns the `ProductPrice` entity with currency "EUR" for that product

### Requirement: Product price included in response DTOs
`ProductSummary` and `ProductDetailResponse` SHALL include `price` (decimal string) and `currency` (3-char string) fields populated based on the resolved currency from the `x-currency-code` header.

#### Scenario: ProductSummary includes currency-specific price
- **WHEN** the landing page fetches products with `x-currency-code: EUR`
- **THEN** each `ProductSummary` item includes `price` as the EUR price and `currency` as "EUR"
