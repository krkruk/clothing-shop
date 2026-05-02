## Why

The MVP has no admin interface — products can only be created via direct API calls. The Clothingshop brand needs a browser-based admin panel for managing the product catalog: adding, viewing, updating, and deleting products with rich content (HTML descriptions, fabrication details, ethics info) and image management. This is the operational backbone for the store.

## What Changes

- **Admin panel UI** — new SvelteKit route group at `/admin` with sidebar navigation, dark-themed login form, and two main views: Product Registration (add) and Inventory Management (list/update/delete)
- **Product Registration form** — two-column form with tiptap rich text editor, category dropdown (fetched from API), image drag-and-drop with auto-upload after product creation
- **Inventory Management table** — paginated product listing with grayscale thumbnails, SKU display, update modal (pre-populated form), delete confirmation modal, and admin footer metadata cards
- **Basic auth login** — branded dark login page at `/admin`, credentials stored in Svelte store and attached to all admin API calls
- **Backend CRUD endpoints** — new `GET /categories`, `GET /admin/products`, `PUT /admin/products/{id}`, `DELETE /admin/products/{id}`, `DELETE /admin/products/{id}/images/{imageId}`
- **Schema changes** — `currency` field (default PLN), `sku` field (user-provided), `is_active` flag (default true, inactive hidden from public), price migrated from Double to BigDecimal
- **Category re-seed** — unify to: tops, coats, bottoms, accessories
- **OpenAPI spec updates** — all new endpoints, modified `CreateProductRequest`, new DTOs
- **Acceptance tests** — Playwright tests for all new admin endpoints
- **curl scripts** — complementary manual verification scripts
- **PRD updates** — unify category names across all prd-* documents

## Capabilities

### New Capabilities
- `admin-auth`: Basic auth login flow on the frontend — credential capture, storage, and attachment to admin API calls
- `admin-product-registration`: Add New Product form with rich text editing, image upload, and category selection
- `admin-inventory-management`: Inventory table with cursor pagination, update modal, delete confirmation, and image management
- `admin-category-api`: Public endpoint to list categories for dropdowns

### Modified Capabilities
- `product-api`: Add currency, SKU, isActive fields to product creation; add update, delete, and admin-list endpoints; change price to BigDecimal
- `public-product-catalog`: Filter out inactive products from public listing
- `image-storage`: Add image deletion endpoint for admin use
- `frontend-api-client`: Regenerate client from updated OpenAPI spec to include all new admin endpoints

## Impact

- **Backend**: New controller methods, service methods, repository queries, Liquibase migrations, entity changes (Product gains currency, sku, isActive columns)
- **OpenAPI spec**: New paths and schemas; **BREAKING** — `price` type changes from `number` (double) to `string` (BigDecimal serialized as string to avoid floating-point precision loss)
- **Frontend**: New route group `(admin)`, new layout, new pages, new components (sidebar, login form, rich text editor, product table, update modal, delete modal, image manager), new auth store, tiptap dependency
- **Database**: New Liquibase migration for schema changes + category re-seed
- **Dependencies**: `@tiptap/core` + extensions added to frontend
- **Tests**: New Playwright acceptance tests, curl scripts
- **Docs**: prd-ui-ux.md, prd-backend.md updated for unified categories

## Non-goals

- Role-based access control or multi-user admin (single admin/admin is sufficient for MVP)
- Product variant management (size, color variants)
- Bulk operations (bulk delete, bulk update)
- Admin audit log or activity history
- Order management admin (orders don't exist yet)
- Dashboard analytics or reporting
- Mobile-responsive admin panel (desktop-only for now)
- Image reordering via drag-and-drop (sequential displayOrder updates via API only)
