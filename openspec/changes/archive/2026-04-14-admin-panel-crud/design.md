## Context

The Clothingshop MVP has a working public storefront (product listing, product detail, cart) but no admin interface. Products can only be created via direct API calls (curl/Postman). The backend already supports `POST /admin/products` and `POST /admin/products/{id}/image` with HTTP Basic auth. The Product entity already has an `isActive` boolean field but it's unused in queries.

Current state:
- **Backend**: 5 admin endpoints exist (create product, upload image). No update, delete, or admin-list endpoints.
- **Frontend**: No admin routes, no auth UI. Generated `AdminProductsApi` TypeScript client exists but is unused.
- **Database**: Product entity has `isActive` but no `currency` or `sku` columns. Categories are seeded as: outerwear, tops, bottoms, accessories, footwear.
- **OpenAPI spec**: `price` is `number` (double). No currency, sku, or isActive fields in request schemas.

## Goals / Non-Goals

**Goals:**
- Full CRUD admin panel accessible at `/admin` with dark-themed UI matching the Obsidian Monolith design system
- Contract-first: update OpenAPI spec first, then generate code for both backend and frontend
- Backend-first implementation order
- Rich text editing for product descriptions using tiptap
- Image upload with seamless UX (select images in form, auto-upload after product creation)
- Cursor pagination for inventory table (consistent with public catalog)

**Non-Goals:**
- Role-based access or multi-user admin
- Product variants (size/color)
- Bulk operations
- Admin audit log
- Mobile-responsive admin
- Drag-and-drop image reordering (sequential API updates only)

## Decisions

### D1: OpenAPI spec changes drive all implementations

Update `openapi/spec.yaml` first with all new endpoints, schemas, and field changes. Then `make generate` produces:
- Backend: Spring interfaces and DTOs in `build/generated/`
- Frontend: TypeScript client in `src/api/generated/`

**Why:** Contract-first is the established convention (CLAUDE.md). Both sides implement against the generated interfaces.

**Alternatives considered:** Hand-written API client on frontend — rejected because it duplicates work and drifts from spec.

### D2: Price as BigDecimal (serialized as string)

Change `price` from `number` (double) to `string` in OpenAPI spec. Backend maps to `java.math.BigDecimal`. Frontend receives/parses as string.

**Why:** Floating-point precision loss with Double is unacceptable for pricing. BigDecimal is the standard for financial data. Serializing as string avoids IEEE 754 precision issues in JSON.

**Alternatives considered:** Keep Double — rejected because `0.01` becomes `0.010000000000000000208...` in floating point.

### D3: New Liquibase migration V003

Single migration adding:
- `currency` column: `VARCHAR(3) DEFAULT 'PLN' NOT NULL`
- `sku` column: `VARCHAR(100)` (nullable, unique)
- `price` column type change: `FLOAT` → `DECIMAL(10,2)`
- Category re-seed: drop footwear, rename outerwear → coats

**Why:** One migration keeps schema changes atomic. The price type change requires a data migration (`ALTER COLUMN TYPE`).

### D4: Admin routes as SvelteKit route group

```
src/routes/(admin)/admin/
├── +layout.svelte          # Sidebar + admin header, replaces main layout
├── login/+page.svelte      # Login form
├── add-product/+page.svelte
└── inventory/+page.svelte
```

The `(admin)` group has its own `+layout.svelte` that renders sidebar + admin header instead of the main site's Header/Footer/CartDrawer. A root `/admin` page redirects to `/admin/add-product`.

**Why:** Clean separation — no conditional logic in the main layout. SvelteKit route groups are designed for this pattern.

### D5: Auth stored in Svelte store with localStorage persistence

A new `src/lib/stores/auth.ts` stores `{username, password}` in localStorage under a key like `dw_admin_auth`. The admin layout checks this store — if empty, redirects to `/admin/login`. All admin API calls configure the generated client with stored credentials.

**Why:** Simple, matches HTTP Basic auth model. No JWT complexity needed for single-admin MVP. localStorage survives page reloads.

**Security note:** Credentials are stored in localStorage (accessible via XSS). Acceptable for MVP with admin/admin demo credentials. Not suitable for production without additional hardening.

### D6: Tiptap for rich text editing

Use `@tiptap/core` with `@tiptap/starter-kit` for the three rich text fields (description/narrative, materiality, preservation). Store HTML content as-is in the backend (string column). Render with `{@html}` on the public product detail page.

**Why:** Tiptap is modular, lightweight, works well with Svelte, and produces clean HTML. The backend already stores description as a string — no schema change needed.

**XSS mitigation:** Backend should sanitize HTML on write (strip `<script>`, event handlers). Frontend renders with `{@html}` which is safe for sanitized content.

### D7: Image upload flow (seamless UX)

1. User selects images in the form (file input + drag-and-drop)
2. On submit: `POST /admin/products` → get product ID
3. For each selected image: `POST /admin/products/{id}/image`
4. Show progress/success state

For updates:
1. Load existing images in the form
2. User can add new images (uploaded on save)
3. User can remove images (`DELETE /admin/products/{id}/images/{imageId}`)
4. User can reorder (PATCH displayOrder, or re-send order on save)

**Why:** The API requires product ID before image upload. This two-step flow is hidden from the user — they see one "save" action.

### D8: Backend endpoint patterns

Follow existing patterns from `AdminProductController` and `ProductService`:
- Controller implements generated OpenAPI interface
- Service handles business logic
- Repository uses Spring Data JPA with `@Query`
- Admin listing reuses cursor pagination pattern from public listing
- Update endpoint accepts partial updates (only non-null fields are changed)
- Delete uses soft delete (`isActive = false`) — already supported by entity

New repository queries needed:
- `findAllByOrderByCreatedAtDesc(cursorTs, cursorId, limit)` — admin listing (shows all, including inactive)
- `deleteByProductAndId(productId, imageId)` — image removal

### D9: curl scripts location

Store in `acceptance_test/curl/` as shell scripts, one per endpoint group. Named descriptively (e.g., `admin-create-product.sh`, `admin-list-products.sh`).

## Risks / Trade-offs

**[BigDecimal migration]** → `ALTER COLUMN TYPE float → decimal` requires a table rewrite on PostgreSQL. Mitigation: the table is empty in dev; in production this would need a online migration strategy (out of scope).

**[localStorage auth]** → Credentials visible to any script on the page. Acceptable for demo credentials, not production. Mitigation: documented as known limitation.

**[tiptap bundle size]** → Adds ~50KB gzipped. Acceptable for admin-only pages loaded on demand. Mitigation: dynamic import the tiptap components.

**[HTML in descriptions]** → XSS risk if unvalidated. Mitigation: server-side sanitization on write (OWASP Java HTML Sanitizer or equivalent).

**[Image upload sequential]** → N images = N sequential HTTP calls after product creation. For MVP (<10 images per product) this is fine. Mitigation: could parallelize later.
