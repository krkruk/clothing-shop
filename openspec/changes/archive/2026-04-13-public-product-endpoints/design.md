## Context

The backend has working admin endpoints for product creation (`POST /admin/products`) and image upload (`POST /admin/products/{id}/image`), backed by `product`, `product_image`, and `category` tables. The `ProductRepository` is a bare `JpaRepository` with no query methods. No public controller or read endpoints exist.

The frontend SvelteKit SPA needs infinite-scroll product browsing and product detail pages. Both require public GET endpoints that return product data with category info and images.

Existing patterns to follow:
- `AdminProductController` implements the generated `AdminProductsApi` interface, uses `@RequestMapping("/api/v1")`
- `ProductService.toProductResponse()` maps entity to generated `ProductResponse` DTO (hardcodes `category.getNameEn()`)
- `GlobalExceptionHandler` produces RFC 9457 `application/problem+json` responses
- `SecurityConfig` permits all requests except `/api/v1/admin/**`

## Goals / Non-Goals

**Goals:**
- Provide two public read endpoints for product browsing with correct data, pagination, and error handling
- Use a single native SQL projection query for the list endpoint to avoid N+1 queries
- Create seed data for development and testing
- Test at three levels: component (query logic), curl (smoke), Python acceptance (full stack)

**Non-Goals:**
- Product i18n / Accept-Language support
- Product search beyond category slug filtering
- Image variant/thumbnail selection for list views
- Frontend integration of these endpoints
- Admin product update/delete endpoints

## Decisions

### 1. Keyset cursor pagination with custom response DTO

**Decision**: Use keyset pagination with a custom `ProductListResponse` DTO instead of Spring `Slice<>`.

**Rationale**: The PRD specifies `Slice<>` but keyset pagination (required for stable infinite scroll under concurrent writes) does not fit the offset-based `Pageable`/`Slice` abstraction. With keyset:
- Cursor encodes `(createdAt, id)` of the last item as base64 JSON
- Query uses `WHERE (created_at, id) < (cursor_ts, cursor_id)` for stable ordering
- `LIMIT + 1` trick detects `hasMore` without a COUNT query
- Response shape is identical to PRD: `{ items, nextCursor, hasMore }`

**Alternative considered**: Offset-based `Slice<>` with `PageRequest.of()` — simpler but produces duplicate/gap items when new products are inserted while the user scrolls.

### 2. Native projection query for list endpoint

**Decision**: Use a single native SQL query with interface-based projection for `GET /products`.

**Rationale**: The list endpoint needs data from 3 tables (product, category, product_image). A single query with `JOIN category` and `LEFT JOIN LATERAL` (for primary image) avoids the N+1 problem entirely. The projection returns exactly the fields needed — no lazy-loading, no extra queries.

Query shape:
```sql
SELECT p.id, p.name, p.price, p.short_description,
       c.slug AS category_slug, c.name_en AS category_name,
       pi.object_key AS image_object_key
FROM product p
JOIN category c ON p.category_id = c.id
LEFT JOIN LATERAL (
    SELECT pi.object_key FROM product_image pi
    WHERE pi.product_id = p.id
    ORDER BY pi.display_order ASC LIMIT 1
) pi ON true
WHERE p.is_active = true
  AND (:categorySlug IS NULL OR c.slug = :categorySlug)
  AND (:cursorTs IS NULL
       OR (p.created_at < :cursorTs)
       OR (p.created_at = :cursorTs AND p.id < :cursorId))
ORDER BY p.created_at DESC, p.id DESC
LIMIT :pageSize + 1
```

**Alternative considered**: `@EntityGraph` with `JOIN FETCH` on category + batch image loading — more JPA-idiomatic but still generates multiple queries and loads full entities we don't need.

### 3. Detail endpoint uses JPQL with entity graph

**Decision**: The detail endpoint fetches a single product with `JOIN FETCH` on category and a separate query for all images. This is appropriate for a single-item lookup where N+1 is not a concern.

### 4. Image URLs as relative paths

**Decision**: Image URLs in responses are relative paths like `/images/products/{pid}/{iid}/original.jpg`.

**Rationale**: The frontend SPA is served on the same origin via nginx. Image URLs go through nginx → MinIO proxy. Relative paths are environment-agnostic (localhost, staging, production).

### 5. Primary image = lowest display_order

**Decision**: The primary image for list views is the `product_image` with the lowest `display_order` value for that product. No variant filtering (THUMBNAIL variant not yet populated by the upload pipeline).

### 6. English-only for category names

**Decision**: Use `category.name_en` for the `category.name` field in responses. The `Category` entity has multilingual columns but i18n is deferred.

### 7. Seed data in SQL file

**Decision**: Create `infra/postgres/seed/dev-data.sql` with ~25 realistic products. Seed images as small placeholder files in `infra/minio/seed/images/` uploaded via the existing `make seed` pattern.

## Risks / Trade-offs

- **[PRD deviation on Slice<>]** → Documented in proposal and here. Response shape matches PRD exactly; only the internal mechanism differs. Can revisit if offset-based pagination is needed later.
- **[LEFT JOIN LATERAL PostgreSQL-specific]** → LATERAL is standard SQL, supported by Postgres 9.3+. Not portable to other databases, but the stack is locked to PostgreSQL 16.
- **[No image variant filtering]** → List endpoint picks the ORIGINAL variant's object key via `display_order`. When image resizing is implemented, the query can be updated to prefer THUMBNAIL variant. This is a future change, not a blocker.
- **[Seed data coupling to acceptance tests]** → Acceptance tests depend on specific seed product names/categories. If seed data changes, tests may break. Accepted for PoC scope.
- **[No product count in response]** → Intentional per PRD. `hasMore` boolean replaces the need for total count. Frontend infinite scroll doesn't need it.

## OpenAPI spec changes

### New paths
- `GET /products` — list products (public, no auth)
- `GET /products/{id}` — product detail (public, no auth)

### New schemas
- `ProductListResponse`: `{ items: ProductSummary[], nextCursor: string|null, hasMore: boolean }`
- `ProductSummary`: `{ id, name, price, imageUrl, shortDescription, category: CategoryDto }`
- `ProductDetailResponse`: full product with all images (each with `imageId`, `imageUrl`, `alt`, `displayOrder`), fabrication, ethics

### Updated schemas
- `ProductResponse.images[]` entries gain `alt` (string|null) and `displayOrder` (integer) fields

### New tag
- `products` — for public product endpoints (distinct from `admin-products`)

## Migration Plan

1. No Liquibase migrations needed — uses existing tables
2. Update `openapi/spec.yaml` → run `make generate`
3. Create `PublicProductController` implementing generated interface
4. Add projection interface and repository methods
5. Add service methods
6. Create seed data
7. Test at all three levels

## Open Questions

None — all design decisions resolved during explore phase.
