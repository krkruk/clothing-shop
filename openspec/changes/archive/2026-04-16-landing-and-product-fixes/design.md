## Context

The frontend storefront has accumulated several issues after the admin panel and API integration work:

1. **Currency display** is hardcoded as `$` in 4 components despite the backend (after `multi-currency-foundation`) returning currency-aware prices
2. **Chessboard layout** is constrained to `max-w-7xl` (1280px) but should span full viewport width per the PRD
3. **Product detail page** still uses mock data from `$lib/mock/products.ts` instead of the API
4. **Header user icon** is a non-functional button — needs login/admin/logout dropdown
5. **No currency selector** exists for customers to switch between PLN and EUR
6. **Cart** has no awareness of currency and doesn't handle currency changes
7. **SEO** is minimal — no meta descriptions, no structured data, no sitemap

The `multi-currency-foundation` change (dependency) provides the backend API with `x-currency-code` header support and the `ProductDetailResponse` DTO with all needed fields (description, fabrication, ethics, images, price per currency).

## Goals / Non-Goals

**Goals:**
- Display prices in the correct currency (PLN/EUR) across all storefront components
- Full-width chessboard grid on the landing page
- Product detail page fetched from API, styled per PRD mockup
- Header with working user icon dropdown (login/admin/logout) and currency picker
- Cart converts to new currency on currency switch
- Basic SEO: meta tags, JSON-LD, sitemap.xml, robots.txt

**Non-Goals:**
- Backend changes (handled by `multi-currency-foundation`)
- SSR or adapter changes
- Authorization/permissions for page access
- Checkout/payment flow
- More than 2 currencies

## Decisions

### Decision 1: Currency state as Svelte store

**Choice:** Create a `currency` store (similar to existing `cart` and `auth` stores) that holds the selected currency code. Persisted to localStorage as `clothingshop-currency`.

**Rationale:** The existing pattern in the codebase uses Svelte stores (`cart.ts`, `auth.ts`). A currency store makes the selected currency available everywhere without prop drilling. The API client interceptor reads from this store to inject the `x-currency-code` header.

### Decision 2: API client interceptor for currency header

**Choice:** Add a request interceptor to the generated API client's Configuration that reads the current currency and injects `x-currency-code` header on every request.

**Rationale:** Centralizes the header injection. All API calls automatically include the correct currency. No need to manually pass currency to every fetch call.

### Decision 3: Cart conversion on currency change

**Choice:** When the user switches currency, iterate cart items, fetch each product's price for the new currency via `GET /api/v1/products/{id}`, update the cart item prices.

**Rationale:** The backend is the authority on pricing (per CLAUDE.md: "Server-authoritative pricing"). The cart must not do local conversion — it must get the real price from the backend.

### Decision 4: Product detail page — API fetch in onMount

**Choice:** Use `onMount` to fetch product data from `GET /api/v1/products/{id}`. No SSR (keeping `adapter-static`).

**Rationale:** The app uses `adapter-static` with SPA fallback. True SSR would require `adapter-node` (deferred). The API fetch approach is consistent with how the landing page works.

### Decision 5: Product page styling follows PRD mockup

**Choice:** Implement the signature scroll-gradient blend (dark → white #f5f5f5) and white content area as described in prd-ui-ux.md Section 3 and shown in the HTML mockup at `docs/frontend/product_selection_page/`.

**Key styling elements:**
- Hero carousel: `33.33vh`, grayscale brightness-50 contrast-125
- Scroll-gradient: CSS gradient from `#0e0e0e` → `#f5f5f5` driven by scroll position
- Content area: `bg-[#f5f5f5]` white background, `max-w-7xl` container
- Left column (2/3): Series label, product title, manifesto with border-left, fabrication grid, ethics grid — all in dark text on white background
- Right column (1/3): Sticky acquisition form with underline-only inputs (dark text variant)
- Detail grid: returns to dark background

### Decision 6: User icon dropdown reuses existing auth store

**Choice:** The Header component imports the existing `auth` store and `isAuthenticated` derived store. The dropdown shows:
- Not logged in: "LOGIN" → navigates to `/admin/login`
- Logged in: "ADMIN PANEL" → navigates to `/admin/add-product`, "LOG OFF" → calls `auth.logout()`

**Rationale:** The auth store and login page already exist. No new auth infrastructure needed. This is explicitly temporary until proper authorization is implemented.

### Decision 7: Build-time sitemap generation

**Choice:** A Node.js script (`frontend/scripts/generate-sitemap.ts`) that calls `GET /api/v1/products?limit=100` during build and writes `frontend/static/sitemap.xml`. Static `robots.txt` in `frontend/static/`.

**Rationale:** Products rarely change (monthly). A build-time script is sufficient. The static directory is served by SvelteKit automatically.

## Risks / Trade-offs

**[API client regeneration dependency]** → This change assumes `multi-currency-foundation` has been applied first, regenerating the frontend API client with `x-currency-code` support and updated DTOs.

**[Cart conversion latency]** → Fetching prices for each cart item on currency switch could be slow with many items. Mitigation: batch or parallel fetch, show loading state during conversion.

**[No SSR]** → SEO is limited to meta tags and JSON-LD. Google renders JS but other engines may not. Acceptable per user decision (Option A).

**[White content area contrast]** → The product page switches from dark to white, which means text colors must also switch (dark text on white). Components need to handle both dark and light contexts.
