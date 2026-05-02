## 1. Currency Store & API Integration

- [x] 1.1 Create `$lib/stores/currency.ts`: writable store with localStorage persistence (`clothingshop-currency`), default "PLN", values "PLN" or "EUR". Export `currency` store and derived helpers.
- [x] 1.2 Update frontend API client configuration in `$lib/api/products.ts` (and any other API modules): add request interceptor that reads the current currency value and injects `x-currency-code` header on every request.

## 2. Header Updates

- [x] 2.1 Add currency picker dropdown in `Header.svelte` between user icon and cart icon: compact selector showing PLN/EUR, underline style, Space Grotesk text-[10px] uppercase, bound to currency store.
- [x] 2.2 Add user icon dropdown in `Header.svelte`: on click, show dropdown with login/admin/logout options based on `$isAuthenticated` state. Import `auth` and `isAuthenticated` from `$lib/stores/auth`. Dropdown styling: `surface_container_high` bg, `primary_container` 2px top border, fade-in 300ms.
- [x] 2.3 Implement dropdown close-on-outside-click and close-on-escape for both dropdowns.

## 3. Landing Page Fixes

- [x] 3.1 Remove `max-w-7xl` constraint from chessboard grid container in `+page.svelte` landing page. Rows should span full viewport width.
- [x] 3.2 Update `ChessboardRow.svelte`: replace hardcoded `$` with currency from API response. Display format: "{currency} {price}" (e.g., "PLN 399.00").

## 4. Product Detail Page Rewrite

- [x] 4.1 Update `$lib/api/products.ts`: add `fetchProductDetail(id)` function calling `GET /api/v1/products/{id}` with currency header. Return `ProductDetailResponse`.
- [x] 4.2 Rewrite `/products/[id]/+page.svelte`: replace mock data with `fetchProductDetail()` in `onMount`. Add loading state and 404 error state.
- [x] 4.3 Implement scroll-gradient blend: CSS/JS transition from `#0e0e0e` (dark) to `#f5f5f5` (white) driven by scroll position, spanning approximately one viewport height.
- [x] 4.4 Style the content area with white background (`bg-[#f5f5f5]`), `max-w-7xl` container, dark text. Two-column grid: 2/3 narrative, 1/3 acquisition form.
- [x] 4.5 Update `ProductNarrative.svelte` (or create equivalent) to render API data: series/artifact label, product title, description with border-left accent, fabrication grid (content + care), ethics section (origin + impact). Handle missing optional fields gracefully.
- [x] 4.6 Update `AcquisitionForm.svelte`: use `price` and `currency` from API response. Format as "{currency} {price}". Ensure 3 measurement fields per PRD (Waist, Hips, Height).
- [x] 4.7 Update `DetailGrid.svelte` to use images from API response (not mock). Return to dark background below the white content area.

## 5. Cart Currency Handling

- [x] 5.1 Add `currency` field to cart items in `$lib/stores/cart.ts`. Track which currency each item is priced in.
- [x] 5.2 Implement currency conversion: when currency store changes, iterate cart items, fetch each product's price for the new currency via `GET /api/v1/products/{id}`, update item prices and currency. Track loading state during conversion.
- [x] 5.3 Update `CartDrawer.svelte` and `CartItem.svelte`: replace hardcoded `$` with cart item's currency. Format: "{currency} {price}". Show loading state during currency conversion.
- [x] 5.4 Ensure new items added to cart use the currently selected currency.

## 6. SEO Metadata

- [x] 6.1 Add `<svelte:head>` meta tags to landing page: `<title>`, `<meta name="description">`, Open Graph tags (`og:title`, `og:description`, `og:type`, `og:url`).
- [x] 6.2 Add `<svelte:head>` meta tags and JSON-LD Product schema to product detail page: `<title>` with product name, `<meta name="description">` with short description, JSON-LD `<script type="application/ld+json">` with Product schema including name, description, image, offers (price + currency).
- [x] 6.3 Create `frontend/static/robots.txt`: `User-agent: *`, `Allow: /`, `Sitemap: /sitemap.xml`.
- [x] 6.4 Create build-time sitemap generator script (`frontend/scripts/generate-sitemap.ts` or equivalent): fetch active products from API, generate sitemap.xml with landing page + product URLs, write to `frontend/static/sitemap.xml`.

## 7. Verification

- [x] 7.1 Run `make dev` and verify landing page chessboard spans full width with correct currency display.
- [x] 7.2 Navigate to a product detail page via ACQUIRE button — verify API fetch, white content area, scroll-gradient, dark text, and all narrative sections render from API data.
- [x] 7.3 Test currency switch: change from PLN to EUR, verify prices update on landing page, product page, and cart.
- [x] 7.4 Test user icon dropdown: verify login redirect, admin panel redirect when logged in, logout functionality.
- [x] 7.5 Verify JSON-LD structured data in page source for a product page.
- [x] 7.6 Verify `robots.txt` and `sitemap.xml` are accessible.
- [x] 7.7 Run `cd frontend && npx vitest run` to verify frontend tests pass.
