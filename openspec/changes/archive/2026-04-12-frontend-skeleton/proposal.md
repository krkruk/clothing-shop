## Why

The repository contains PRDs, design system docs, and HTML mockups for Clothingshop but no runnable application code. This change jump-starts the frontend as a Svelte + TypeScript SPA with static content matching the "Obsidian Monolith" design system. It establishes the project skeleton, reusable component library, cart state management, and testing infrastructure so that subsequent changes (API integration, checkout, admin) build on a solid foundation.

## What Changes

- Scaffold a **SvelteKit** project under `frontend/` with Vite, TypeScript, and Tailwind CSS configured with the full Obsidian Monolith design token set (colors, typography, border-radius overrides)
- Create reusable Svelte components: `Header`, `Footer`, `HeroCarousel`, `ChessboardRow`, `AcquisitionForm`, `CartDrawer`, `CartEmptyState`, `CartItem`, `LookbookFragment`, `InfiniteScrollIndicator`, `ProductNarrative`, `DetailGrid`, `ScrollGradient`
- Implement a **cart store** (Svelte writable store + localStorage persistence) with add, remove, update quantity, and derived total
- Wire the **AcquisitionForm** to the cart store (adds personalized items)
- Create two pages with static mock data: **Landing Page** (hero + chessboard product grid + lookbook + infinite scroll indicator) and **Product Detail Page** (hero + scroll-gradient blend + narrative + acquisition form + detail grid)
- Ensure the product grid on the landing page is **exactly horizontally centered** (not left-aligned)
- Ensure product detail subpages have a **dark background** throughout (not white-on-white), keeping all text white
- Add component/unit tests using **Vitest + @testing-library/svelte** for stores and key components
- Add **Playwright + Python** acceptance tests under `acceptance_test/` verifying page structure, navigation, and cart behavior
- Provide a **Dockerfile** (multi-stage: build + nginx serve) for containerized deployment
- Generate a **README.md** under `frontend/` with setup, dev, test, and build instructions

## Capabilities

### New Capabilities
- `design-system`: Tailwind configuration with Obsidian Monolith tokens (colors, fonts, border-radius), global CSS (scrollbar, selection, Material Symbols font config), and typographic hierarchy
- `page-landing`: Landing page with hero carousel (33.33vh), chessboard product grid with category tonal shifts, lookbook fragment, infinite scroll indicator
- `page-product-detail`: Product detail page with hero carousel, scroll-gradient blend (dark to white), two-column content area (narrative + sticky acquisition form), detail grid
- `cart-store`: Client-side cart state management with localStorage persistence, add/remove/update operations, and derived total
- `cart-drawer`: Slide-in smoked glass drawer (right side) with item list, quantity controls, total, empty state, and PROCEED TO TRANSACTION button
- `component-header`: Fixed global header with brand name, category nav with hover dropdowns, person icon, cart icon with badge
- `component-footer`: Global footer with INVENTORY, TRANSACTIONS, LEGAL, MANIFESTO links and copyright
- `mock-data`: Typed static data (Product, Category, CartItem types + sample objects) for all pages and components
- `frontend-infra`: Dockerfile, Vite config, SvelteKit config, npm scripts, README.md

### Modified Capabilities

(none — this is the first implementation change)

## Impact

- **New directories**: `frontend/` (full SvelteKit project), `acceptance_test/` (Playwright Python project)
- **Dependencies**: SvelteKit, Tailwind CSS, Vitest, @testing-library/svelte, Playwright, pytest
- **No API dependency**: All data is static/mock. No backend connection required.
- **No existing code affected**: This is a greenfield addition.
