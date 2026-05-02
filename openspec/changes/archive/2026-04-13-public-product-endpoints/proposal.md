## Why

The backend currently only exposes admin endpoints for product creation and image upload. There is no way for the public-facing frontend to browse or view products. The `GET /api/v1/products` (paginated list) and `GET /api/v1/products/{id}` (detail) endpoints are the first public API endpoints needed to make the storefront functional.

## What Changes

- Add `GET /api/v1/products` — cursor-paginated list of active products with primary image, category info, and optional category slug filter. Uses keyset pagination with opaque base64-encoded cursors for stable infinite-scroll behavior under concurrent writes.
- Add `GET /api/v1/products/{id}` — full product detail with all images (ordered by `display_order`, including `alt` text), fabrication, and ethics data.
- Update `openapi/spec.yaml` with new public endpoints, `ProductListResponse`, `ProductSummary`, and `ProductDetailResponse` schemas. Update existing `ProductResponse.images` entries to include `alt` and `displayOrder` fields.
- Create native SQL projection query for the list endpoint to avoid N+1 queries (single query with JOIN to category + LEFT JOIN LATERAL for primary image).
- Create seed data (`infra/postgres/seed/dev-data.sql`) with ~25 realistic products spread across the 5 existing categories, and placeholder images in MinIO.
- Add acceptance tests: curl scripts for quick smoke testing, Python tests for the full suite, and a component test for the projection query and pagination logic.

**PRD Deviation**: The PRD specifies using Spring `Slice<>` for pagination. This change uses a custom `ProductListResponse` DTO instead, because keyset (cursor-based) pagination does not fit the offset-based `Pageable`/`Slice` abstraction. The response shape (`items`, `nextCursor`, `hasMore`) is identical to the PRD specification. This deviation will be documented in design.md.

## Capabilities

### New Capabilities
- `public-product-catalog`: Public read-only endpoints for browsing products — cursor-paginated list with category filtering and full product detail retrieval

### Modified Capabilities
- `product-api`: OpenAPI spec gains new public paths and response schemas; existing `ProductResponse.images` entries gain `alt` and `displayOrder` fields

## Non-goals

- Product i18n / Accept-Language support (deferred)
- Product search beyond category slug filtering
- Product sorting options beyond `created_at DESC`
- Image variant/thumbnail support (schema-ready, not used yet)
- Frontend integration of the new endpoints (separate change)
- Admin endpoints for product updates (PUT, DELETE) — out of scope

## Impact

- **OpenAPI spec**: New paths `/products` and `/products/{id}`; new schemas `ProductListResponse`, `ProductSummary`, `ProductDetailResponse`; updated image schema in `ProductResponse`
- **Backend**: New `PublicProductController`, new repository projection interface, new service methods for listing and detail retrieval
- **Database**: No schema changes — uses existing `product`, `product_image`, and `category` tables
- **Seed data**: New file `infra/postgres/seed/dev-data.sql` with ~25 products; new placeholder images in `infra/minio/seed/images/`
- **Testing**: New curl scripts, Python acceptance tests, component test
- **Generated code**: `make generate` will produce new Java interfaces and TypeScript types from the updated OpenAPI spec
