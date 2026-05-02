## Why

The landing page has several issues: hardcoded dollar sign for prices (should use currency from API), chessboard rows constrained to max-w-7xl (should be full-width), ACQUIRE buttons navigate to a product detail page that uses mock data (should fetch from API), and the user icon in the header is non-functional (should provide login/admin/logout). Additionally, the header needs a currency picker dropdown (PLN/EUR), the product detail page should match the PRD/mockup design (dark → white gradient), and the cart needs to handle currency switching. Finally, basic SEO metadata is missing.

## What Changes

- Fix currency display: replace hardcoded `$` with currency code/symbol from API response across all components (ChessboardRow, AcquisitionForm, CartDrawer, CartItem)
- Make chessboard grid full-width: remove `max-w-7xl` constraint from landing page product grid
- Add currency picker dropdown in header between user icon and cart icon (PLN/EUR, theme-aligned)
- Send `x-currency-code` header on all API requests based on selected currency
- Add user icon dropdown in header: login → `/admin/login` (if not authed), admin panel (if authed), logout option
- Rewrite product detail page (`/products/[id]`) to fetch from `GET /api/v1/products/{id}` instead of mock data
- Restyle product detail page to match PRD mockup: hero carousel → scroll-gradient blend (dark → white #f5f5f5) → white content area with 2/3 narrative + 1/3 sticky acquisition form → dark detail grid
- Update cart store: when currency changes, refetch prices for all cart items from API and convert cart to new currency
- Add SEO metadata: `<svelte:head>` with meta description, Open Graph tags, JSON-LD Product schema on product pages
- Generate `sitemap.xml` at build time and add static `robots.txt`

## Capabilities

### New Capabilities
- `seo-metadata`: Meta tags, JSON-LD structured data, sitemap.xml, robots.txt for SEO

### Modified Capabilities
- `component-header`: Add currency picker dropdown and user icon dropdown with login/admin/logout
- `page-landing`: Fix chessboard to full-width, use currency from API response, remove hardcoded dollar sign
- `page-product-detail`: Fetch from API instead of mock data, match PRD mockup styling (dark → white gradient)
- `cart-store`: Handle currency switching by refetching item prices
- `cart-drawer`: Display prices in the selected currency

## Impact

- **Frontend components**: Header, ChessboardRow, AcquisitionForm, CartDrawer, CartItem, ProductNarrative, DetailGrid all need currency-aware display
- **Frontend routes**: Landing page (+page.svelte), product detail page (/products/[id]/+page.svelte) need API data fetching updates
- **Frontend stores**: Cart store needs currency state and conversion logic
- **Frontend API client**: All API calls must include `x-currency-code` header
- **SEO files**: New sitemap generation script, new robots.txt, new meta tag logic
- **Build process**: Sitemap generation step added

## Non-goals

- No backend changes (those are in `multi-currency-foundation`)
- No SSR or adapter changes
- No new pages or routes
- No authorization/permissions system — admin access is unrestricted after login
- No checkout/payment flow
