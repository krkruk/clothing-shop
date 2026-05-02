## Why

The landing page currently renders from hardcoded mock data with Unsplash images. The `GET /api/v1/products` endpoint is implemented and tested in the backend, but the frontend has no API client and no data integration. This change wires the landing page to real product data, replaces placeholder seed images with goth/18th-century fashion photography, and implements cursor-based infinite scroll — making the landing page a functional product catalog surface.

## What Changes

- Replace mock data imports in the landing page with API calls to `GET /api/v1/products`
- Generate a TypeScript API client from `openapi/spec.yaml` using openapi-generator, integrated into the frontend build
- Implement client-side infinite scroll with cursor pagination (limit=7 per page, IntersectionObserver trigger)
- Hero carousel feeds from the first 4 products returned by the API, with product name labels positioned bottom-right
- LookbookFragment renders images from loaded product data instead of Unsplash URLs
- Upgrade seed script to download 21 real goth/18th-century fashion images (500x500px+, open license) and upload to MinIO
- Prune seed SQL from 25 to 21 products (remove 4 footwear items)
- Show a subtle dark spinner while loading products; replace the infinite scroll indicator with the existing Footer when `hasMore === false`
- Category grouping by tonal shifts is deferred (marked TODO) — products render as a flat stream ordered by `created_at DESC`

## Capabilities

### New Capabilities
- `frontend-api-client`: TypeScript API client generated from OpenAPI spec for frontend→backend communication
- `seed-images`: Real fashion photography for seed data, replacing 1x1px placeholders

### Modified Capabilities
- `page-landing`: Data source changes from mock to API; hero carousel labels become product names at bottom-right; lookbook uses product images; infinite scroll becomes real cursor pagination with loading spinner and footer at end; category grouping deferred
- `mock-data`: Deprecated as the primary data source for the landing page (mock files remain for other pages until migrated)

## Impact

- **Frontend**: `+page.svelte`, `HeroCarousel`, `ChessboardRow`, `LookbookFragment`, `InfiniteScrollIndicator` all modified to consume API data
- **Frontend build**: New openapi-generator step added to `Makefile` generate target; new npm dev dependency
- **OpenAPI**: No changes to `openapi/spec.yaml` — existing spec is consumed as-is
- **Seed infrastructure**: `infra/seed.sh` and `infra/postgres/seed/dev-data.sql` updated (pruned to 21 products, real image downloads)
- **Acceptance tests**: New Playwright tests for landing page product rendering, infinite scroll behavior, and image display
- **No backend changes**: The API endpoint, service, and repository are untouched

## Non-goals

- Category grouping with tonal shifts on the landing page (deferred to future change)
- Product detail page integration with API (separate change)
- Cart/checkout integration with backend (separate change)
- Changing the OpenAPI spec or backend endpoints
- Server-side rendering (frontend remains `adapter-static` SPA with client-side hydration)
- Image optimization, thumbnail generation, or responsive image variants
- SEO or meta tag improvements
- Removing mock data files entirely (kept for product detail page until it migrates)
