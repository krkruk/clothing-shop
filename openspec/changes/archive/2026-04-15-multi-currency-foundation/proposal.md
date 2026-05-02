## Why

The store currently supports a single hardcoded currency (PLN) with a dollar sign displayed in the frontend. To serve European customers, we need multi-currency pricing where each product has distinct prices in PLN and EUR. The pricing model must be per-currency at the database level, and the API must serve the correct price based on the client's selected currency.

## What Changes

- **BREAKING**: Replace the single `price` + `currency` fields on `Product` with a new `ProductPrice` entity storing one price row per currency per product
- **BREAKING**: Add `x-currency-code` HTTP header support to `GET /api/v1/products` and `GET /api/v1/products/{id}` — backend returns the price for the requested currency
- Consolidate all Liquibase migrations into a single migration file (environment will be purged)
- Only two currencies supported: PLN (default) and EUR
- Update OpenAPI spec: `ProductSummary` and `ProductDetailResponse` return `price` + `currency` for the requested currency; `CreateProductRequest` and `UpdateProductRequest` accept a `prices` map with both PLN and EUR (both required)
- Update admin product registration form: two required price fields ("VALUATION (PLN)" and "VALUATION (EUR)"), remove the currency dropdown
- Update admin inventory management: edit modal mirrors the dual-price registration form

## Capabilities

### New Capabilities
- `multi-currency-pricing`: Database schema (ProductPrice entity), API header-based currency resolution, and price-per-currency query logic

### Modified Capabilities
- `admin-product-registration`: Replace single price + currency dropdown with dual required price fields (PLN + EUR)
- `admin-inventory-management`: Update edit modal to include dual price fields
- `product-api`: Add `x-currency-code` header parameter to public product endpoints; update request/response schemas for multi-currency
- `public-product-catalog`: Response includes price for the currency specified via `x-currency-code` header

## Impact

- **Database**: New `product_price` table. `price` and `currency` columns removed from `product` table. All Liquibase migrations consolidated into one file.
- **OpenAPI spec** (`openapi/spec.yaml`): `ProductSummary`, `ProductDetailResponse`, `ProductResponse`, `CreateProductRequest`, `UpdateProductRequest` schemas updated. New `ProductPriceDto` schema. `GET /products` and `GET /products/{id}` gain `x-currency-code` header parameter.
- **Backend entities**: New `ProductPrice` JPA entity. `Product` entity loses price/currency fields.
- **Backend mappers**: ProductMapper must resolve price from `ProductPrice` table based on requested currency.
- **Backend controllers**: `PublicProductController` reads `x-currency-code` header and passes to service layer.
- **Admin controllers**: `AdminProductController` handles create/update with price map.
- **Frontend generated client**: Regenerated from updated spec — must include `x-currency-code` header support.
- **Frontend admin UI**: Add-product and inventory edit forms updated for dual-price input.

## Non-goals

- No more than 2 currencies (PLN, EUR). Future expansion is out of scope.
- No currency conversion or exchange rates. Prices are manually set per currency.
- No geo-IP based currency detection.
- No frontend storefront changes in this change (landing page, product page, cart, header — those belong to the follow-up change `landing-and-product-fixes`).
- No SSR or SEO changes.
