## 1. Seed Data Upgrade

- [x] 1.1 Prune `infra/postgres/seed/dev-data.sql` to remove 4 footwear products (IDs ending `...0022` through `...0025`), leaving 21 products across 4 categories
- [x] 1.2 Curate and download 21 goth/18th-century fashion images from Unsplash/Pexels (coats, dresses, skirts, pants, shirts, belts, bags, gloves — no boots/hoodies, 500x500px minimum)
- [x] 1.3 Update `infra/seed.sh` to download the curated images instead of generating 1x1px placeholders, upload them to MinIO, and create `product_image` records
- [x] 1.4 Verify seed works end-to-end: `make clean && make infra-local && make seed` — confirm 21 products in DB and 21 images in MinIO
- [x] 1.5 Add acceptance test curl script to verify seed data: `GET /api/v1/products` returns 21 products with non-null `imageUrl` fields

## 2. Frontend API Client Generation

- [x] 2.1 Add `@openapitools/openapi-generator-cli` as a dev dependency in `frontend/package.json`
- [x] 2.2 Add `generate:api` npm script in `frontend/package.json` that runs openapi-generator with `typescript-fetch` generator, reading `../openapi/spec.yaml`, outputting to `src/api/generated/`
- [x] 2.3 Update `Makefile` `generate` target to also run `cd frontend && npm run generate:api`
- [x] 2.4 Run `make generate` and verify TypeScript client is generated with `ProductListResponse`, `ProductSummary`, and fetch function types
- [x] 2.5 Verify `frontend/src/api/generated/` is in `.gitignore` and no generated files are tracked

## 3. Landing Page API Integration

- [x] 3.1 Create a thin API wrapper module (e.g., `src/lib/api/products.ts`) that wraps the generated client's product list function with the correct base path (`/api/v1`) and returns typed responses
- [x] 3.2 Refactor `+page.svelte` to remove all `$lib/mock` imports and replace with reactive state (`$state`) for products, cursor, hasMore, and loading
- [x] 3.3 Add `onMount` logic to fetch the first page: `GET /api/v1/products?limit=7` — populate hero carousel data and initial product list
- [x] 3.4 Update `HeroCarousel` to accept product-derived data (`{ url: imageUrl, label: name }`) and position labels at bottom-right
- [x] 3.5 Update `ChessboardRow` to accept a `ProductSummary` type from the generated client instead of the mock `Product` type — use `imageUrl` (string) instead of `images[0]` (array)
- [x] 3.6 Update `LookbookFragment` to receive product image URLs from loaded data instead of importing `$lib/mock` — render first 4 products' images in the existing grid layout
- [x] 3.7 Remove `InfiniteScrollIndicator` component import from landing page (replaced by inline loading spinner + footer)
- [x] 3.8 Add a subtle dark CSS spinner component (or inline styles) for loading state — consistent with Obsidian Monolith design (dark tones, no shadows)

## 4. Infinite Scroll

- [x] 4.1 Implement IntersectionObserver logic in `+page.svelte` with a sentinel element at the bottom of the product list
- [x] 4.2 When sentinel intersects viewport and `hasMore && !loading`, fetch next page: `GET /api/v1/products?cursor={nextCursor}&limit=7`
- [x] 4.3 Append new products to the existing list, update `nextCursor` and `hasMore` from response
- [x] 4.4 When `hasMore === false`, unmount the spinner — Footer from `+layout.svelte` becomes natural end of page
- [x] 4.5 Handle edge cases: empty product list (show empty state), API error during scroll (show error, allow retry), rapid scroll (debounce or skip duplicate loads)

## 5. Acceptance Tests

- [x] 5.1 Add Playwright test: landing page loads and displays product rows from the API (verify at least 1 product name is visible)
- [x] 5.2 Add Playwright test: hero carousel shows product images with labels at bottom-right
- [x] 5.3 Add Playwright test: lookbook section displays product images from loaded data
- [x] 5.4 Add Playwright test: infinite scroll — verify loading spinner appears, next page loads, products append to list
- [x] 5.5 Add Playwright test: end of products — verify spinner disappears and footer is visible after all products loaded
- [x] 5.6 Add curl test script: verify `GET /api/v1/products?limit=7` returns correct pagination shape (items, nextCursor, hasMore)
- [x] 5.7 Run full acceptance test suite and verify all pass
