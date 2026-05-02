## MODIFIED Requirements

### Requirement: Cursor pagination
The `GET /api/v1/products` endpoint SHALL only return products where `is_active = true`. Inactive (soft-deleted) products SHALL be excluded from public results.

#### Scenario: Inactive products hidden from public listing
- **WHEN** a product has been soft-deleted (is_active = false)
- **AND** a public GET request is made to `/api/v1/products`
- **THEN** the soft-deleted product does not appear in the results

#### Scenario: Inactive products hidden from product detail
- **WHEN** a public GET request is made to `/api/v1/products/{id}` for an inactive product
- **THEN** the response is 404 with ProblemDetail

## ADDED Requirements

### Requirement: Unified category set
The system SHALL use exactly four categories: tops, coats, bottoms, accessories. The Liquibase seed migration SHALL create only these categories.

#### Scenario: Category filter works with new categories
- **WHEN** public listing is requested with `?category=coats`
- **THEN** only products in the coats category are returned
