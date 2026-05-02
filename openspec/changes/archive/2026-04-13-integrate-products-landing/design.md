## Context

The landing page (`+page.svelte`) currently imports all data from `$lib/mock/` — hardcoded products with Unsplash URLs. The backend's `GET /api/v1/products` endpoint is fully implemented with cursor pagination, category filtering, and primary image resolution. The frontend has no generated API client (`src/api/generated/` is empty). The frontend is a static SPA built with `adapter-static` — no SvelteKit server process exists at runtime.

Current data flow:
```
mock/index.ts → mock/products.ts → +page.svelte → ChessboardRow/HeroCarousel
```

Target data flow:
```
Browser → fetch /api/v1/products → generated TypeScript client → +page.svelte → components
```

No backend changes, no OpenAPI spec changes, no new entities. This is purely a frontend integration + seed data upgrade.

## Goals / Non-Goals

**Goals:**
- Generate a TypeScript API client from `openapi/spec.yaml` for type-safe frontend→backend communication
- Replace mock data on the landing page with real API calls using cursor pagination
- Implement infinite scroll that loads 7 products per page via IntersectionObserver
- Hero carousel and lookbook section render from API product data
- Seed MinIO with real goth/18th-century fashion photography (21 products, ~21 images)
- Subtle dark spinner during loading; Footer appears naturally when products exhausted

**Non-Goals:**
- Category grouping with tonal background shifts (deferred)
- Product detail page API integration
- Server-side rendering (keep `adapter-static`)
- Image optimization or responsive variants
- Removing mock data files (still needed by product detail page)

## Decisions

### D1: OpenAPI code generation tooling

**Decision**: Use `openapi-generator-cli` via npm, invoked from the Makefile's `generate` target.

**Rationale**: The backend already uses `openApiGenerate` via Gradle. Adding a parallel frontend generation step keeps the pattern consistent. The `openapi-generator-cli` npm package supports TypeScript fetch client generation out of the box. Using `@openapitools/openapi-generator-cli` with the `typescript-fetch` generator produces a typed client with no framework dependencies.

**Alternatives considered**:
- `hey-api/openapi-ts`: Lighter, better DX, but adds a framework-specific dependency
- Hand-rolled fetch wrapper: Simpler initially but loses type safety and drifts from spec
- `orval`: React-focused, doesn't fit SvelteKit well

**Implementation**: Add `@openapitools/openapi-generator-cli` as a dev dependency in `frontend/package.json`. Add a `generate:api` npm script that reads `../openapi/spec.yaml` and outputs to `src/api/generated/`. Update `Makefile` generate target to also run `cd frontend && npm run generate:api`.

### D2: Data fetching strategy — hybrid static shell + client-side hydration

**Decision**: Keep `adapter-static`. The HTML shell (header, footer, layout) renders immediately. Product data is fetched on `onMount` via `GET /api/v1/products?limit=7`. Subsequent pages load via IntersectionObserver triggering the same endpoint with `cursor` parameter.

**Rationale**: Switching to `adapter-node` would require a new container service in compose.yml, a running Node process, and significant infra changes. The static SPA approach means the page structure renders fast, and product data appears after a single API round-trip. For a dark fashion brand landing page, the brief loading state is acceptable.

**Alternatives considered**:
- `adapter-node` with true SSR: Better first paint but heavy infra change for one page
- Static prerendering with `+page.ts` load: Won't work — `adapter-static` with `fallback: 'index.html'` means load functions run client-side only

### D3: Product state management — Svelte 5 runes in the page component

**Decision**: Use `$state` and `$derived` runes directly in `+page.svelte` to manage the product list, loading state, and pagination cursor. No separate store file.

**Rationale**: The product list is page-local state. It doesn't need to be shared across routes (the cart store is different — it persists across pages). Svelte 5 runes provide reactive state without the boilerplate of a writable store. If other pages need the same data later, it can be extracted into a store at that point.

**State shape**:
```typescript
let products = $state<ProductSummary[]>([]);
let nextCursor = $state<string | null>(null);
let hasMore = $state(true);
let loading = $state(false);
```

### D4: Infinite scroll trigger — IntersectionObserver on sentinel element

**Decision**: Place an invisible sentinel `div` at the bottom of the product list. Attach an `IntersectionObserver` to it. When it enters the viewport and `hasMore && !loading`, trigger the next API call.

**Rationale**: IntersectionObserver is the standard approach for infinite scroll. Using a sentinel element (rather than observing the last product) simplifies the logic — the sentinel always exists at the bottom, regardless of product count. The spinner renders in place of the old `InfiniteScrollIndicator` while loading.

### D5: Hero carousel data mapping

**Decision**: Take the first 4 products from the initial API response. Map each to `{ url: product.imageUrl, label: product.name }`. The label renders at bottom-right via absolute positioning in the carousel component.

**Rationale**: The hero carousel already accepts `{ url: string; label?: string }[]`. Mapping the first 4 products gives category variety (products are ordered by `created_at DESC`, and seed data spans categories chronologically). The product name as label is product-forward and clickable-feeling.

### D6: LookbookFragment data source

**Decision**: Pass the first 4 loaded products' `imageUrl` values to the lookbook section. The component receives an array of image URLs and renders them in the existing 12-column grid layout.

**Rationale**: The lookbook was previously editorial Unsplash images. Replacing with real product images makes it part of the product discovery flow. Using already-loaded data avoids an extra API call. The lookbook serves as a visual break between product batches in the infinite scroll.

### D7: Seed image sourcing and upload

**Decision**: Download ~21 images from Unsplash/Pexels using their APIs (both allow commercial use without attribution). Search terms: "goth coat", "dark dress", "18th century clothing", "victorian shirt", "dark pants", "goth accessories". Resize to 800x800px minimum. The seed script downloads them, then uploads to MinIO via `mc` client.

**Seed data pruning**: Remove the 4 footwear products from `dev-data.sql` (IDs ending `...0022` through `...0025`). This leaves 21 products: 6 outerwear, 6 tops, 5 bottoms, 4 accessories.

**Rationale**: User specifically requested goth/18th century aesthetic, 500x500px minimum, no boots/hoodies. The seed script already handles MinIO upload — just replace the placeholder generation with real image downloads.

### D8: Loading and end-of-list UX

**Decision**: While loading, show a subtle spinner — a small dark-toned CSS animation (rotating circle or pulsing dot) consistent with the Obsidian Monolith design system. When `hasMore === false`, the `InfiniteScrollIndicator` component unmounts entirely. The Footer (already rendered by `+layout.svelte`) becomes the natural end of the page.

**Rationale**: No new components needed for the end state — the Footer already exists in the layout. The spinner should feel minimal and dark, matching the brand aesthetic. A simple CSS animation with `border-color: on-secondary-container` and `border-top-color: primary` works.

## Risks / Trade-offs

- **[Flash of empty content]** → The landing page shell renders immediately but products appear after the first API call (~200ms). Mitigation: the hero carousel is the first visual — if it loads fast, the empty product grid below is less noticeable. Could add skeleton placeholders later.

- **[Seed image licensing]** → While Unsplash and Pexels allow commercial use, the specific images must actually match the goth/18th century criteria. Mitigation: manually curate search terms and verify results before embedding in seed script. Document the source URLs in the seed script comments.

- **[OpenAPI generator output quality]** → Generated TypeScript clients can be verbose. Mitigation: use `typescript-fetch` generator which produces clean, minimal code. If the output is problematic, wrap it in a thin facade.

- **[21 products = only 3 page loads]** → With limit=7, 21 products means 3 loads then end. This is intentional for testing but may feel short. Mitigation: the seed can be expanded later. The infinite scroll mechanism is fully functional regardless of product count.
