## 1. OpenAPI Contract & Code Generation

- [x] 1.1 Update `openapi/spec.yaml`: add `currency` (string, default "PLN"), `sku` (string, nullable), `isActive` (boolean, default true) to `CreateProductRequest` and `ProductResponse` schemas; change `price` type from `number` to `string` with `format: decimal`
- [x] 1.2 Add `GET /api/v1/categories` endpoint to `openapi/spec.yaml` with `listCategories` operationId, `CategoryDto` response array
- [x] 1.3 Add `GET /api/v1/admin/products` endpoint (cursor pagination, returns all products including inactive) to `openapi/spec.yaml`
- [x] 1.4 Add `PUT /api/v1/admin/products/{id}` endpoint (partial update) to `openapi/spec.yaml`
- [x] 1.5 Add `DELETE /api/v1/admin/products/{id}` endpoint (soft delete, 204 response) to `openapi/spec.yaml`
- [x] 1.6 Add `DELETE /api/v1/admin/products/{id}/images/{imageId}` endpoint to `openapi/spec.yaml`
- [x] 1.7 Run `make generate` to regenerate backend interfaces/DTOs and frontend TypeScript client

## 2. Database Migrations

- [x] 2.1 Create Liquibase migration `V005__admin_product_fields.xml`: add `currency` column (VARCHAR(3) DEFAULT 'PLN'), `sku` column (VARCHAR(100) UNIQUE)
- [x] 2.2 Create Liquibase migration `V006__reseed_categories.xml`: drop existing categories, re-seed with tops, coats, bottoms, accessories (with en/pl/es names and display_order)
- [x] 2.3 Verify migrations run cleanly: `make clean && make infra-local && make dev`

## 3. Backend — Entity & Repository Updates

- [x] 3.1 Update `Product.java` entity: add `currency` (String), `sku` (String) fields; ensure `isActive` field maps correctly
- [x] 3.2 Update `ProductRepository.java`: add `findAllForAdmin()` native query for admin listing (includes inactive products, cursor-based, DESC ordering)
- [x] 3.3 Add `CategoryRepository` method `findAllByOrderByDisplayOrderAsc()` for the categories endpoint
- [x] 3.4 Add `ProductImageRepository` method `findByProductIdAndId()` for image deletion validation

## 4. Backend — Service Layer

- [x] 4.1 Update `ProductService.createProduct()`: map new fields (currency, sku, isActive) from request; validate unique SKU; set defaults (currency=PLN, isActive=true)
- [x] 4.2 Add `ProductService.updateProduct()`: partial update — only overwrite non-null fields from request; validate SKU uniqueness on change
- [x] 4.3 Add `ProductService.deleteProduct()`: soft-delete by setting `isActive = false`; idempotent (no error if already inactive)
- [x] 4.4 Add `ProductService.listAdminProducts()`: cursor-paginated listing of ALL products (including inactive), ordered by created_at desc
- [x] 4.5 Add `ProductService.deleteProductImage()`: remove image from DB and delete object from MinIO; validate image belongs to product
- [x] 4.6 Add `ProductService.updateImageOrder()`: update display_order for a list of product images
- [x] 4.7 Add `ProductService.listCategories()`: return all categories ordered by display_order
- [x] 4.8 Update `ProductService.listProducts()` (public): already has `WHERE p.is_active = true` filter — verified, no change needed

## 5. Backend — Controller Layer

- [x] 5.1 Add `CategoryController` implementing generated `CategoriesApi` interface: `GET /api/v1/categories`
- [x] 5.2 Update `AdminProductController`: implement `updateProduct()` from generated interface
- [x] 5.3 Update `AdminProductController`: implement `deleteProduct()` from generated interface
- [x] 5.4 Update `AdminProductController`: implement `listAdminProducts()` from generated interface
- [x] 5.5 Update `AdminProductController`: implement `deleteProductImage()` from generated interface
- [x] 5.6 Verify all controller methods return correct HTTP status codes and error responses per OpenAPI spec

## 6. Backend — Tests

- [x] 6.1 Run `make test` — fix any compilation errors from entity/DTO changes
- [x] 6.2 Add unit tests for `ProductService.updateProduct()` (partial update, null fields preserved, SKU uniqueness)
- [x] 6.3 Add unit tests for `ProductService.deleteProduct()` (soft delete, idempotent)
- [x] 6.4 Add unit tests for `ProductService.listAdminProducts()` (pagination, includes inactive)
- [x] 6.5 Add unit tests for `ProductService.deleteProductImage()` (image removal, wrong product)
- [x] 6.6 Add component test for `PUT /api/v1/admin/products/{id}` (success, 401, 404, 422 validation)
- [x] 6.7 Add component test for `DELETE /api/v1/admin/products/{id}` (success, 401, 404, idempotent)
- [x] 6.8 Add component test for `GET /api/v1/admin/products` (pagination, includes inactive, 401)
- [x] 6.9 Add component test for `DELETE /api/v1/admin/products/{id}/images/{imageId}` (success, 401, 404)
- [x] 6.10 Add component test for `GET /api/v1/categories` (returns ordered categories)
- [x] 6.11 Verify `GET /api/v1/products` public endpoint excludes inactive products

## 7. PRD & Documentation Updates

- [x] 7.1 Update `prd-ui-ux.md`: replace category references (outerwear→coats, drop tailoring/objects/footwear) to unified set: Tops, Coats, Bottoms, Accessories
- [x] 7.2 Update `prd-backend.md`: document new endpoints (PUT, DELETE, admin list, categories), schema changes (currency, sku, isActive, BigDecimal price), unified categories
- [x] 7.3 Update `AGENTS.md` if any architectural conventions change

## 8. curl Scripts

- [x] 8.1 Create `acceptance_test/curl/admin-list-categories.sh`
- [x] 8.2 Create `acceptance_test/curl/admin-create-product.sh` (with new fields: currency, sku, isActive)
- [x] 8.3 Create `acceptance_test/curl/admin-list-products.sh` (with cursor pagination)
- [x] 8.4 Create `acceptance_test/curl/admin-update-product.sh`
- [x] 8.5 Create `acceptance_test/curl/admin-delete-product.sh`
- [x] 8.6 Create `acceptance_test/curl/admin-delete-product-image.sh`

## 9. Frontend — API Integration Layer

- [x] 9.1 Create `src/lib/api/admin.ts`: wrapper functions for all admin API calls (createProduct, updateProduct, deleteProduct, listAdminProducts, deleteProductImage) using generated client with Basic auth configuration
- [x] 9.2 Create `src/lib/api/categories.ts`: wrapper for `GET /categories` using generated client
- [x] 9.3 Verify generated TypeScript types include all new fields (currency, sku, isActive, BigDecimal price)

## 10. Frontend — Auth Store & Login

- [x] 10.1 Create `src/lib/stores/auth.ts`: Svelte store with `{username, password}`, localStorage persistence under `dw_admin_auth`, login/logout actions
- [x] 10.2 Create admin route group structure: `src/routes/(admin)/admin/+layout.svelte` with sidebar and admin header
- [x] 10.3 Create `src/routes/(admin)/admin/login/+page.svelte`: dark-themed login form with username/password inputs, error state, submit handler
- [x] 10.4 Add auth guard to admin layout: redirect to `/admin/login` if no credentials in store

## 11. Frontend — Admin Panel Shell (Shared Layout)

- [x] 11.1 Build admin sidebar component: Inventory title, "Corpo Goth Admin" subtitle, navigation links (Add new product, Modify/delete product), active state styling (bg-red-950), operator footer with SYS badge
- [x] 11.2 Build admin top nav bar: section title (dynamic), search input, admin_panel_settings + account_circle icons
- [x] 11.3 Add structural decoration: fixed right-edge gradient line (w-1 h-screen from-primary_container via-transparent)
- [x] 11.4 Create `src/routes/(admin)/admin/+page.svelte`: redirect to `/admin/add-product`

## 12. Frontend — Product Registration Page (Add Product)

- [x] 12.1 Create page structure at `src/routes/(admin)/admin/add-product/+page.svelte` with two-column grid layout, page header "PRODUCT REGISTRATION" with accent bar
- [x] 12.2 Build Identification Cluster (left column): Product Identity input, Precise Abstract input, Valuation dropdown (currency), Taxonomy dropdown (categories from API), Status checkbox
- [x] 12.3 Install tiptap dependencies: `@tiptap/core`, `@tiptap/starter-kit`, `@tiptap/extension-bold`, `@tiptap/extension-italic`, `@tiptap/extension-bullet-list`
- [x] 12.4 Build TiptapEditor Svelte component: toolbar with bold/italic/list Material Symbol icons, dark-themed editing area with border-l-2 accent
- [x] 12.5 Build Rich Text Cluster (right column): Narrative editor (min-h-160px), Materiality + Preservation editors (2-col sub-grid, h-32 each)
- [x] 12.6 Build Ethics row (full-width): Provenance + Societal Resonance underline inputs in 2-col grid with top border separator
- [x] 12.7 Build Visual Documentation section: drag-and-drop area (h-64, dashed border), file input handler, 4-column thumbnail preview grid with grayscale treatment, image removal X button
- [x] 12.8 Build ADD PRODUCT submit button: primary_container bg, full-width, hover lift, active scale-95
- [x] 12.9 Implement form submission flow: validate required fields -> POST product -> upload images sequentially -> success/error handling
- [x] 12.10 Add SKU input field to the form (in Identification Cluster)

## 13. Frontend — Inventory Management Page (Modify/Delete)

- [x] 13.1 Create page at `src/routes/(admin)/admin/inventory/+page.svelte` with header, stats display, and table container
- [x] 13.2 Build inventory table component: column headers (ASSET, IDENTITY, CLASSIFICATION, MODIFIED, DIRECTIVES), row rendering with hover effects (bg shift + thumbnail color reveal)
- [x] 13.3 Build pagination footer: range display "DISPLAYING X-Y OF Z UNITS", page number buttons with active state, chevron navigation using cursor from API
- [x] 13.4 Build admin footer fragment: 3-column grid with System Status, Access Level, Data Integrity cards
- [x] 13.5 Implement data fetching: load products from `GET /api/v1/admin/products` on mount and on pagination events

## 14. Frontend — Update Modal

- [x] 14.1 Build update modal overlay: dark background, same form structure as Product Registration, pre-populated with product data
- [x] 14.2 Load existing product data into form: fetch from `GET /api/v1/admin/products/{id}` or use table row data
- [x] 14.3 Load existing images into the image manager with remove buttons
- [x] 14.4 Implement update submission: `PUT /api/v1/admin/products/{id}` with changed fields -> upload new images -> delete removed images -> update image order
- [x] 14.5 On successful update: close modal, refresh table

## 15. Frontend — Delete Confirmation Modal

- [x] 15.1 Build delete confirmation modal: "CONFIRM REMOVAL OF ASSET?" with product name, confirm/cancel buttons in dark theme
- [x] 15.2 Implement delete action: `DELETE /api/v1/admin/products/{id}` on confirm -> refresh table on success
- [x] 15.3 Handle cancel: close modal with no action

## 16. Frontend — Integration & Polish

- [x] 16.1 Wire sidebar navigation: Add new product -> `/admin/add-product`, Modify/delete product -> `/admin/inventory`, with active state tracking
- [x] 16.2 Ensure all form inputs follow underline-only design system: border-b border-outline/30, focus:border-primary-container, no focus ring
- [x] 16.3 Ensure no rounded corners on any interactive element (buttons, inputs, dropdowns)
- [x] 16.4 Ensure all labels use Space Grotesk text-[10px] uppercase tracking-[0.3em] with outline color
- [x] 16.5 Test full admin flow end-to-end: login -> add product with images -> view in inventory -> update -> delete

## 17. Acceptance Tests (Playwright)

- [x] 17.1 Add Playwright test for `GET /api/v1/categories`: verify response structure and ordering
- [x] 17.2 Add Playwright test for `POST /api/v1/admin/products` with new fields (currency, sku, isActive)
- [x] 17.3 Add Playwright test for `GET /api/v1/admin/products`: pagination, includes inactive products
- [x] 17.4 Add Playwright test for `PUT /api/v1/admin/products/{id}`: partial update, full update, validation errors
- [x] 17.5 Add Playwright test for `DELETE /api/v1/admin/products/{id}`: soft delete, 404, idempotent
- [x] 17.6 Add Playwright test for `DELETE /api/v1/admin/products/{id}/images/{imageId}`: success, wrong product, 401
- [x] 17.7 Add Playwright test verifying public `GET /api/v1/products` excludes inactive products
- [x] 17.8 Add Playwright E2E test: admin login -> add product -> verify in inventory -> update -> delete
