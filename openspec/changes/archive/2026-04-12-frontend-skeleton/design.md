## Context

The Clothingshop e-commerce application has complete PRDs (product, UI/UX, backend, infrastructure) and two HTML mockups (landing page, product detail page) but no runnable code. This change creates the frontend skeleton: a SvelteKit SPA with Tailwind CSS matching the "Obsidian Monolith" design system, static mock data, cart state management, and test infrastructure.

The AGENTS.md defines the target architecture as Svelte + TypeScript + Vite behind nginx. The frontend communicates with a Spring Boot backend via nginx reverse proxy. In this skeleton step, there is no backend — all data is static.

## Goals / Non-Goals

**Goals:**
- Establish a runnable SvelteKit project that matches the Obsidian Monolith design system pixel-for-pixel when compared to the HTML mockups
- Create a component library that can be reused when real API data replaces mock data
- Implement cart state management that persists across page navigation
- Set up testing infrastructure (Vitest for unit/component, Playwright for acceptance)
- Provide a production-ready Dockerfile and clear README

**Non-Goals:**
- Backend API integration (no API exists yet)
- Checkout page implementation (cart's "PROCEED TO TRANSACTION" leads nowhere)
- Admin pages
- Real image assets (placeholder/gradient images or external URLs)
- CI/CD pipeline
- SvelteKit server-side rendering (SPA mode only)

## Decisions

### 1. SvelteKit with adapter-static over plain Svelte

**Choice:** SvelteKit configured as a static SPA (`adapter-static` with `fallback: 'index.html'`)

**Rationale:**
- File-based routing (`src/routes/`) gives clear URL structure without manual router setup
- SvelteKit is still Svelte + Vite under the hood — no deviation from AGENTS.md
- `adapter-static` produces a plain SPA that nginx can serve exactly as planned
- Built-in loading states, layouts, and params — useful when API integration comes later

**Alternatives considered:**
- Plain Svelte + `svelte-spa-router`: More manual wiring, less convention. No compelling advantage for this project size.
- SvelteKit with SSR: Overkill for a client-side SPA with no server data needs. Adds complexity for nginx deployment.

### 2. Tailwind CSS with design tokens in config

**Choice:** Tailwind CSS v4 with a custom theme extension containing all Obsidian Monolith color tokens, font families, and border-radius overrides.

**Rationale:**
- The HTML mockups already use Tailwind with the exact token set — direct port
- Custom colors (e.g., `surface-container-lowest`, `primary-container`) map to the design system one-to-one
- `borderRadius: { DEFAULT: "0px", lg: "0px", xl: "0px" }` enforces the "no rounded corners" rule globally
- Google Fonts loaded via `<link>` in `app.html` (Space Grotesk, Manrope, Material Symbols Outlined)

**Token source:** `prd-ui-ux.md` color palette table is the canonical source. The HTML mockups' Tailwind config is a secondary reference.

### 3. Mock data as typed objects

**Choice:** A `src/lib/mock/` directory with TypeScript interfaces and static data objects.

```
src/lib/mock/
├── types.ts          # Product, Category, CartItem, Personalization
├── products.ts       # 6-9 sample products across 3 categories
├── categories.ts     # TOPS, BOTTOMS, ACCESSORIES with sub-categories
└── images.ts         # Placeholder image URLs (gradient divs or unsplash)
```

**Rationale:**
- Typed interfaces (`Product`, `Category`) will be replaced by generated API types later — but the shape stays the same
- Mock data validates component props and data flow before any API exists
- Each product has the full field set: name, description, shortDescription, price, category, fabrication, ethics, images

### 4. Cart store architecture

**Choice:** Svelte writable store in `src/lib/stores/cart.ts` with localStorage subscription.

```
cartItems: Writable<CartItem[]>
  → addItem(product, personalization)
  → removeItem(cartItemId)
  → updateQuantity(cartItemId, quantity)
  → clearCart()

cartTotal: Derived<number>
cartCount: Derived<number>
```

**Rationale:**
- Svelte stores are the standard reactive state primitive — no external library needed
- localStorage sync happens on write (not on every tick) via a subscribe-on-write pattern
- Each cart item includes personalization data (silhouette, waist, hips, height) per PRD
- `cartItemId` is a client-generated UUID to distinguish identical products with different personalization
- No quantity field concept — each personalized configuration is a separate line (matching AGENTS.md: "each line = unique personalized item")

**Alternatives considered:**
- Svelte context API: Too scoped to component trees. Cart is global.
- External state library (nanostores, zustand-like): Unnecessary complexity. Svelte stores handle this.

### 5. Vitest + @testing-library/svelte for component tests

**Choice:** Vitest as test runner with `@testing-library/svelte` for component rendering.

**Test scope:**
- **Store tests** (unit): cart add/remove/update/total/localStorage sync
- **Component tests**: Header nav rendering, Footer links, CartDrawer empty/populated states, AcquisitionForm field presence and submission

**Rationale:**
- Vitest shares Vite config — components render with real Tailwind processing
- testing-library queries (`getByText`, `getByRole`) test user-facing behavior, not implementation
- Fast (jsdom, no browser needed)

### 6. Playwright + Python for acceptance tests

**Choice:** Playwright with Python (pytest + pytest-playwright) under `acceptance_test/`.

**Test scope:**
- Landing page: header present, hero section, chessboard rows, category tonal shifts, footer links
- Product detail page: hero, acquisition form with 4 fields, narrative content, detail grid
- Cart: opens from header, shows empty state, item appears after "ACQUIRE ARTIFACT", total updates, quantity controls work
- Navigation: landing → product detail via chessboard row click

**Rationale:**
- AGENTS.md specifies Playwright + Python for acceptance tests — consistent with planned architecture
- Tests run against the dev server (or Docker container) at `http://localhost:5173` (dev) or `http://localhost:8080` (containerized)

### 7. Dockerfile (multi-stage: build + nginx)

**Choice:**
```dockerfile
# Stage 1: Node.js — npm install + npm run build
# Stage 2: nginx:alpine — serve static output from adapter-static
```

**Rationale:**
- Matches the planned production architecture (nginx serves frontend static files)
- Final image is small (nginx:alpine + static assets, no Node.js runtime)
- `nginx.conf` configured for SPA fallback (all routes → index.html)

### 8. Measurement fields: 4 fields per PRD

**Choice:** Silhouette dropdown + Waist + Hips + Height (4 fields total)

**Rationale:**
- PRD section 4.2 explicitly lists: Silhouette (dropdown), Waist (cm), Hips (cm), Height (cm)
- AGENTS.md data model confirms: `silhouette (BOXY/CURVY/OTHER), waistCm, hipsCm, heightCm`
- The HTML mockup only shows 3 fields (missing Hips). Per exploration decision: follow the docs, mockups are reference only.

### 9. Hero carousel height: 33.33vh

**Choice:** Combined header + carousel = 33.33vh per PRD/UI-UX spec.

**Rationale:**
- PRD section 4.1: "1/3 screen width: product photo" and combined height
- UI-UX doc decision #1: "Hero height = 33.33vh (header + carousel combined). Confirmed PRD value."
- HTML mockup uses 921px fixed — overridden by spec
- Carousel height = `calc(33.33vh - 80px)` where 80px is header height

### 10. Cart drawer: always dark smoked glass

**Choice:** `surface_container_highest` (#353534) at 90% opacity + 12px backdrop blur, consistent across all pages.

**Rationale:**
- UI-UX doc decision #3: "Cart = always dark smoked glass. Mockups were inconsistent (dark on landing, white on product). Unified to dark."
- The landing page mockup shows the correct dark implementation; the product page mockup incorrectly uses white (#f5f5f5). Follow the spec.

### 11. Product grid horizontal centering

**Choice:** The chessboard product grid SHALL use `mx-auto` with a `max-w-7xl` container to ensure exact horizontal centering on the viewport.

**Rationale:**
- Current implementation renders the grid left-aligned, which creates visual imbalance on wider screens
- The chessboard rows already have a defined column split (1/3 + 2/3), so the centering is at the container level
- `max-w-7xl` (80rem / 1280px) matches the design system's content width constraint and keeps rows from stretching too wide on ultrawide monitors

### 12. Product detail page background: dark throughout

**Choice:** The product detail page SHALL maintain a dark background (`surface_container_lowest` #0e0e0e) for the entire page. The scroll-gradient section transitions from dark hero to a slightly lighter dark tone (NOT white #f5f5f5). All text remains white/light throughout.

**Rationale:**
- The current implementation uses a scroll-gradient that transitions to #f5f5f5 (near-white), creating white-on-white text visibility issues
- The "Obsidian Monolith" design system is fundamentally dark — product detail pages should remain dark to match the brand aesthetic
- The scroll-gradient should transition between dark tones (e.g., #0e0e0e → #201f1f) to add subtle depth without breaking the dark theme
- White text on a dark background maintains readability and stays true to the design language

## Risks / Trade-offs

**Tailwind v4 adoption risk** → Tailwind v4 is relatively new. If v4 causes issues with SvelteKit or Vitest, fall back to v3 with equivalent config. The token structure is identical.

**Mock data shape vs real API** → Mock types may not perfectly match generated OpenAPI types. Mitigation: mock types are intentionally minimal and will be replaced wholesale. Components accept typed props — only the data source changes.

**Static SPA and SEO** → SPA with client-side routing means no server-rendered HTML. Not a concern for v1 (no public SEO requirement for a niche brand MVP). Can add SSR later if needed.

**Carousel complexity** → A full crossfade carousel with auto-rotation, pause-on-hover, and progress indicators is non-trivial. Mitigation: implement a minimal but correct version. No carousel library — keep it custom to match the specific design behavior (4-5s rotation, thin bar progress indicators).

**No real images** → Placeholder images won't match the dark editorial photography style. Mitigation: use dark gradient backgrounds or grayscale stock photos as placeholders. The component structure supports real images via URL swap later.

## Open Questions

1. **Lookbook Fragment** — The UI-UX doc lists it as "deferred to post-MVP" but the landing page mockup includes it. Include in skeleton or skip?
   - **Recommendation:** Include. It's a static section with no interactivity — low effort, high visual fidelity.

2. **Responsive breakpoints** — The UI-UX doc specifies mobile behavior at 320px+ but doesn't define tablet breakpoints. Should the skeleton only handle desktop (>768px) and mobile (<768px), or add tablet?
   - **Recommendation:** Two breakpoints only: desktop (md: 768px+) and mobile (<768px). Matches mockup behavior.
