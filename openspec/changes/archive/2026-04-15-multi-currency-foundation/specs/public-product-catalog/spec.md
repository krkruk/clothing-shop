## MODIFIED Requirements

### Requirement: Cursor pagination
The `GET /api/v1/products` endpoint SHALL only return products where `is_active = true`. Inactive (soft-deleted) products SHALL be excluded from public results. The response `ProductSummary` items SHALL include `price` and `currency` fields reflecting the currency specified by the `x-currency-code` header (defaulting to PLN).

#### Scenario: Inactive products hidden from public listing
- **WHEN** a product has been soft-deleted (is_active = false)
- **AND** a public GET request is made to `/api/v1/products`
- **THEN** the soft-deleted product does not appear in the results

#### Scenario: Inactive products hidden from product detail
- **WHEN** a public GET request is made to `/api/v1/products/{id}` for an inactive product
- **THEN** the response is 404 with ProblemDetail

#### Scenario: Products returned with EUR pricing
- **WHEN** a public GET request is made to `/api/v1/products` with header `x-currency-code: EUR`
- **THEN** each product in the response includes `price` as the EUR price and `currency` as "EUR"

#### Scenario: Products returned with PLN pricing by default
- **WHEN** a public GET request is made to `/api/v1/products` without a currency header
- **THEN** each product in the response includes `price` as the PLN price and `currency` as "PLN"
