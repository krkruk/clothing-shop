## 1. Project Scaffolding

- [x] 1.1 Scaffold SvelteKit project under `frontend/` with TypeScript, using `npx sv create` (or `npm create svelte@latest`), selecting skeleton template + TypeScript
- [x] 1.2 Install and configure `adapter-static` for SPA mode with `fallback: 'index.html'` in `svelte.config.js`
- [x] 1.3 Install and configure Tailwind CSS (v4 or v3) with the full Obsidian Monolith token set: all color tokens, font families (`headline` = Space Grotesk, `body`/`label` = Manrope), and border-radius overrides (DEFAULT/lg/xl = 0px)
- [x] 1.4 Add Google Fonts `<link>` tags in `app.html` for Space Grotesk (300-700), Manrope (200-600), and Material Symbols Outlined (wght 200)
- [x] 1.5 Create `src/app.css` with global styles: custom scrollbar (4px, #0e0e0e track, #5c0000 thumb), text selection colors, Material Symbols font-variation-settings
- [x] 1.6 Create `.gitkeep` in `src/api/generated/` and add `src/api/generated/` to `.gitignore`
- [x] 1.7 Add npm scripts: `dev`, `build`, `preview`, `test`, `test:watch`, `check`

## 2. Mock Data & Types

- [x] 2.1 Create `src/lib/mock/types.ts` with TypeScript interfaces: `Product`, `Category`, `CartItem`, `Personalization`, `Silhouette` enum
- [x] 2.2 Create `src/lib/mock/categories.ts` with 3 categories (TOPS, BOTTOMS, ACCESSORIES) and sub-categories per prd-ui-ux.md
- [x] 2.3 Create `src/lib/mock/products.ts` with 6-9 sample products distributed across categories, each with full editorial content (name, description, shortDescription, price, fabrication, ethics, images)
- [x] 2.4 Create `src/lib/mock/images.ts` with placeholder image URLs (external stock photos or gradient placeholders) for products, carousel, and lookbook

## 3. Cart Store

- [x] 3.1 Create `src/lib/stores/cart.ts` with Svelte writable store, localStorage initialization, and round-trip serialization
- [x] 3.2 Implement `addItem(product, personalization)` — creates unique cart item with client-generated ID
- [x] 3.3 Implement `removeItem(cartItemId)` — removes item, no-op if not found
- [x] 3.4 Implement `updateQuantity(cartItemId, quantity)` — updates quantity, removes if <= 0
- [x] 3.5 Implement derived stores: `cartTotal` (sum of price * quantity), `cartCount` (sum of quantities)
- [x] 3.6 Add localStorage persistence on every mutation (add, remove, update)

## 4. Shared Components

- [x] 4.1 Create `Header.svelte` — fixed header with brand name link, category nav links with hover dropdowns (sub-categories), person icon, cart icon with reactive badge count, mobile hamburger menu
- [x] 4.2 Create `Footer.svelte` — INVENTORY/TRANSACTIONS/LEGAL/MANIFESTO links, MANIFESTO in primary color, copyright text, hover transitions
- [x] 4.3 Create `HeroCarousel.svelte` — configurable carousel with crossfade transition (4-5s auto-rotation), pause-on-hover, progress indicator bars, grayscale image treatment, accept image array as prop
- [x] 4.4 Create `CartDrawer.svelte` — slide-in drawer (450px desktop, full-width mobile), smoked glass backdrop, "CURRENT INVENTORY" header, close button, item list with empty state, footer with total and PROCEED TO TRANSACTION button
- [x] 4.5 Create `CartItem.svelte` — 96x128px thumbnail, item name/variant, inline quantity controls ([−] count [+]), price, remove on quantity=0
- [x] 4.6 Create `CartEmptyState.svelte` — centered `inventory_2` icon (60px, 30% opacity), "INVENTORY IS CURRENTLY EMPTY" text

## 5. Landing Page

- [x] 5.1 Create `src/routes/+layout.svelte` — wraps all pages with Header, Footer, and CartDrawer
- [x] 5.2 Create `src/routes/+page.svelte` (landing page) — hero carousel with panoramic images, scroll hint
- [x] 5.3 Implement chessboard product grid section — alternating row layout (image-left/image-right), products grouped by category with tonal background shifts, **wrapped in a `max-w-7xl mx-auto` container for exact horizontal centering**
- [x] 5.4 Create `ChessboardRow.svelte` — single product row with image column (1/3, opacity-80, mix-blend-luminosity, hover scale-105/700ms) and content column (2/3, name, description, price, ACQUIRE button)
- [x] 5.5 Implement category tonal shifts: TOPS on #0e0e0e, BOTTOMS on #201f1f, ACCESSORIES on #0e0e0e
- [x] 5.6 Create `LookbookFragment.svelte` — asymmetric 12-column grid on `surface_dim` background
- [x] 5.7 Create `InfiniteScrollIndicator.svelte` — "CONTINUE EXPLORING THE VOID" text with animated expand_more icon

## 6. Product Detail Page

- [x] 6.1 Create `src/routes/products/[id]/+page.svelte` — loads mock product by route param
- [x] 6.2 Implement product hero carousel (33.33vh, product-specific images, grayscale brightness-50 contrast-125, bottom gradient overlay)
- [x] 6.3 Implement scroll-gradient blend — scroll-triggered background transition from #0e0e0e to #201f1f (dark-to-dark, NOT white) over ~1 viewport height
- [x] 6.3a Change product detail page content area background from white (#f5f5f5) to dark (#201f1f), keep all text white
- [x] 6.4 Create `ProductNarrative.svelte` — left column (2/3) with series label, product title, manifesto (border-left primary_container), fabrication grid, ethics section
- [x] 6.5 Create `AcquisitionForm.svelte` — right column (1/3, sticky) with silhouette dropdown, waist/hips row, height input, price display, ACQUIRE ARTIFACT button that calls cart.addItem(), shipping note. All four fields (silhouette + waist + hips + height) present
- [x] 6.6 Create `DetailGrid.svelte` — dark section (consistent with dark page theme) with asymmetric 12-column grid (col-span-7 + col-span-5 with mt-24 offset)
- [x] 6.7 Implement mobile responsive layout — single column at <768px, no horizontal scroll at 320px

## 7. Component & Unit Tests (Vitest)

- [x] 7.1 Configure Vitest with `@testing-library/svelte`, `@testing-library/jest-dom`, jsdom environment, and Svelte/Vite preprocessing
- [x] 7.2 Create `tests/stores/cart.test.ts` — test addItem, removeItem, updateQuantity, cartTotal, cartCount, localStorage round-trip, edge cases (empty cart, non-existent item removal)
- [x] 7.3 Create `tests/components/Header.test.ts` — verify brand name renders, nav links present, cart badge reflects store count
- [x] 7.4 Create `tests/components/Footer.test.ts` — verify all 4 links present, MANIFESTO has distinct color
- [x] 7.5 Create `tests/components/CartDrawer.test.ts` — verify empty state renders, items render after adding, total displays correctly, quantity controls function
- [x] 7.6 Create `tests/components/AcquisitionForm.test.ts` — verify 4 input fields present (silhouette, waist, hips, height), form submission adds to cart

## 8. Acceptance Tests (Playwright + Python)

- [x] 8.1 Create `acceptance_test/` project with `pyproject.toml` (uv: pytest, pytest-playwright), `conftest.py` with base_url fixture
- [x] 8.2 Create `tests/test_landing_page.py` — header present, hero section visible, chessboard rows render, category tonal shifts, ACQUIRE buttons present, footer links present, lookbook section
- [x] 8.3 Create `tests/test_product_page.py` — hero carousel, acquisition form with all 4 fields, narrative content, detail grid, ACQUIRE ARTIFACT button, scroll-gradient behavior
- [x] 8.4 Create `tests/test_cart.py` — cart opens from header, empty state visible, add item via ACQUIRE ARTIFACT, item appears in drawer, total updates, quantity controls work, remove item, cart persists across page navigation

## 9. Infrastructure & Documentation

- [x] 9.1 Create `frontend/Dockerfile` — multi-stage: Node.js build + nginx:alpine serve with SPA fallback config
- [x] 9.2 Create `frontend/nginx.conf` (or inline in Dockerfile) — serves static files, fallback all routes to index.html
- [x] 9.3 Create `frontend/README.md` — project overview, prerequisites, setup, dev, test, build, Docker usage, project structure
