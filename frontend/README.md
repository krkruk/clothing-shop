# Clothingshop Frontend

SvelteKit SPA for the Clothingshop e-commerce application. Dark editorial aesthetic — "The Obsidian Monolith."

## Prerequisites

- Node.js 22+
- npm 10+

## Setup

```bash
npm install
```

## Development

```bash
npm run dev
```

Starts the Vite dev server at `http://localhost:5173`.

## Testing

```bash
# Unit & component tests
npm test

# Watch mode
npm run test:watch
```

## Type Checking

```bash
npm run check
```

## Production Build

```bash
npm run build
npm run preview
```

## Docker

```bash
podman build -t clothingshop-frontend .
podman run -p 8080:80 clothingshop-frontend
```

## Project Structure

```
frontend/
├── src/
│   ├── app.html              # HTML shell with Google Fonts
│   ├── app.css               # Global styles & Tailwind theme
│   ├── api/generated/        # Gitignored — generated API client
│   ├── lib/
│   │   ├── components/       # Svelte components
│   │   ├── stores/           # Svelte stores (cart)
│   │   └── mock/             # Mock data & types
│   └── routes/
│       ├── +layout.svelte    # Global layout (header, footer, cart)
│       ├── +page.svelte      # Landing page
│       └── products/[id]/    # Product detail page
├── tests/                    # Vitest tests
├── svelte.config.js
├── vite.config.ts
├── Dockerfile
└── nginx.conf
```
