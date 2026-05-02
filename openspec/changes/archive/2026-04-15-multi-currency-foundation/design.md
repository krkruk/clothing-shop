## Context

The Clothingshop store currently stores a single `price` (BigDecimal) and `currency` (String, default "PLN") per product. The frontend hardcodes a dollar sign (`$`) for display. The `ProductSummary` DTO (used by the landing page list) does not even include a currency field — only `ProductDetailResponse` does.

The business needs dual-currency support (PLN + EUR) where each product has independently set prices per currency. This requires a schema change from a single price to a price-per-currency model.

Current data model:
```
Product
├── price: BigDecimal(10,2)
├── currency: String(3) — "PLN"
└── ... (other fields)
```

Proposed data model:
```
Product
└── (no price/currency here)

ProductPrice  (new table)
├── id: UUID (PK)
├── product_id: UUID (FK → product)
├── currency: String(3) — "PLN" or "EUR"
├── price: BigDecimal(10,2)
└── UNIQUE(product_id, currency)
```

## Goals / Non-Goals

**Goals:**
- Store prices per currency in a dedicated `product_price` table
- Serve the correct price via `x-currency-code` HTTP header on public product endpoints
- Admin UI accepts both PLN and EUR prices as required fields
- Consolidate all Liquibase migrations into a single file (environment purge)
- Regenerate OpenAPI client after spec update

**Non-Goals:**
- More than 2 currencies (PLN, EUR only)
- Automatic currency conversion or exchange rate integration
- Geo-IP based currency detection
- Frontend storefront changes (header currency picker, cart conversion — deferred to `landing-and-product-fixes`)
- SSR or SEO changes

## Decisions

### Decision 1: HTTP header `x-currency-code` instead of query parameter

**Choice:** Custom HTTP header `x-currency-code: PLN` (or `EUR`)
**Alternatives considered:**
- Query parameter `?currency=PLN` — simpler, but pollutes URLs and caching. Also less RESTful for a "context" concern.
- Return all prices, let frontend pick — larger payloads, more frontend logic.

**Rationale:** Currency is a client context (like Accept-Language), not a resource filter. A header keeps the URL space clean and makes caching straightforward (vary on header). Default to PLN if header is absent or invalid.

### Decision 2: Dedicated `product_price` table instead of JSON column

**Choice:** Separate JPA entity `ProductPrice` with `product_id`, `currency`, `price`
**Alternatives considered:**
- JSONB column on `product` — flexible but harder to validate and query with JPA.
- Columns `price_pln` + `price_eur` on `product` — simple but doesn't scale if currencies are added.

**Rationale:** The entity approach uses standard JPA patterns already in the codebase, supports the UNIQUE constraint, and is queryable. The two-currency limit is a business constraint enforced at validation level, not schema level.

### Decision 3: Single consolidated Liquibase migration

**Choice:** Replace all existing migration XML files with one `V001__initial_schema.xml`
**Rationale:** Environment is being purged. A single file simplifies the baseline and avoids migration chain issues. Contains: categories, products (without price/currency), product_price, product_images tables.

### Decision 4: Both currencies required at product creation

**Choice:** Admin must provide both PLN and EUR prices when creating/updating a product
**Rationale:** The store only has 2 currencies. Making both required avoids the complexity of "product visible in PLN but not EUR" — which would require per-currency visibility logic in the public catalog.

### Decision 5: OpenAPI spec changes

**Changes to `openapi/spec.yaml`:**
- Remove `price` and `currency` from `ProductSummary`, `ProductDetailResponse`, `ProductResponse`
- Add `price` and `currency` fields back to `ProductSummary` and `ProductDetailResponse` (populated by backend based on `x-currency-code` header)
- Add `prices` field (array of `{currency, price}`) to `CreateProductRequest` and `UpdateProductRequest`
- Add `prices` field to `ProductResponse` (admin view shows all prices)
- Add `x-currency-code` header parameter to `GET /products` and `GET /products/{id}`
- Add new `ProductPriceDto` schema: `{currency: string(3), price: string(decimal)}`
- `AdminProductListResponse` items (`ProductResponse`) include full prices array

## Risks / Trade-offs

**[Breaking API change]** → All existing clients must update. Acceptable since this is pre-release and environment is being purged.

**[Generated code regeneration]** → `make generate` will regenerate both backend interfaces/DTOs and frontend API client. Backend implementations must be updated to match new interfaces. Manual testing after regeneration is essential.

**[Admin form complexity]** → Two price fields instead of one. Minimal increase — the form already has a currency dropdown that gets replaced by two labeled inputs.

**[Default currency fallback]** → If `x-currency-code` header is missing or invalid, backend defaults to PLN. This ensures backward compatibility for any unmodified clients.

## Migration Plan

1. Stop all services (`make clean`)
2. Replace Liquibase migrations with single consolidated file
3. Update OpenAPI spec
4. Run `make generate` to regenerate backend interfaces/DTOs and frontend client
5. Update backend: new entity, repository, mapper logic, controller header handling
6. Update frontend admin forms
7. Run `make infra-local` + `make seed` to verify fresh environment
8. Run `make test` + `make test-component` to validate
