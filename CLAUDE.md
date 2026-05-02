# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

Clothingshop — a lightweight e-commerce app for an alternative clothing brand. Obsidian vault acting as planning workspace + monorepo with working application code. Uses OpenSpec, a spec-driven development workflow with integrated AI skills.

## Commands

```bash
make infra-local      # Start PostgreSQL + MinIO only (for backend IDE dev)
make dev              # Full stack: build & start all services with hot-reload
make build            # Build all container images (prod targets)
make seed             # Load sample data into Postgres and MinIO
make generate         # Run OpenAPI code generation for frontend + backend
make clean            # Stop containers, remove volumes

make test             # Backend unit tests (cd backend && ./gradlew test)
make test-component   # Backend component/integration tests (TestContainers)
make test-acceptance  # Playwright e2e tests (requires dev + seed first)

# Run a single backend test
cd backend && ./gradlew test --tests "com.clothingshop.SomeTest.testMethod"
cd backend && ./gradlew testComponent --tests "com.clothingshop.component.SomeTest"

# Frontend tests
cd frontend && npx vitest run
cd frontend && npx vitest run src/lib/components/Cart.test.ts  # single file
```

App available at `http://localhost:8080` after `make dev`.

## OpenSpec Workflow

1. `/opsx:explore` — Think through ideas (read-only)
2. `/opsx:propose` — Create change proposal with proposal.md, design.md, tasks.md
3. `/opsx:apply` — Implement tasks from a change sequentially
4. `/opsx:archive` — Finalize and archive a completed change

OpenSpec state lives in `openspec/` (config.yaml, specs/, changes/).

## Architecture

Monorepo, containerized, contract-first. Podman Compose behind nginx reverse proxy.

```
Browser → nginx (:8080) → /        → Frontend (SvelteKit SPA)
                         → /api/*  → Backend  (Spring Boot)
                         → /images/* → MinIO
Backend → PostgreSQL 16 (Liquibase migrations)
Backend → MinIO (S3-compatible product images)
```

Only nginx publishes a host port. No CORS — nginx provides same-origin routing.

### Backend (Java 21, Spring Boot 3.x)

Base package: `com.clothingshop`

| Layer | Path | Notes |
|-------|------|-------|
| Controllers | `controller/` | Implement generated OpenAPI interfaces |
| Services | `service/` | Business logic |
| Repositories | `repository/` | Spring Data JPA |
| Entities | `entity/` | JPA entities (Product, Category, ProductImage, ImageVariant) |
| Config | `config/` | Security, S3/MinIO, bucket init |
| Exceptions | `exception/` | Global handler, RFC 9457 Problem Details |
| Generated | `build/generated/src/main/java/` | Gitignored — OpenAPI interfaces + DTOs |
| Migrations | `resources/db/changelog/` | Liquibase XML migrations, run on startup |

Component tests in `src/testComponent/` use TestContainers (PostgreSQL + MinIO) with container reuse.

### Frontend (SvelteKit + TypeScript, Vite, Tailwind CSS 4)

| Path | Purpose |
|------|---------|
| `src/routes/` | SvelteKit file-based routing (layout, landing page, products/[id]) |
| `src/lib/components/` | UI components (CartDrawer, Header, HeroCarousel, ChessboardRow, AcquisitionForm, etc.) |
| `src/lib/stores/` | Cart store using Svelte 5 runes |
| `src/lib/mock/` | Mock product/category data (will be replaced by generated API client) |
| `src/api/generated/` | Gitignored — TypeScript client from OpenAPI |
| `frontend/tests/` | Vitest + Testing Library |

### Infrastructure

- `infra/compose.local.yml` — PostgreSQL (:5432) + MinIO (:9000/:9001) only
- `infra/compose.yml` — Full stack with backend, frontend, nginx
- `infra/nginx/nginx.conf` — Reverse proxy routing, 5MB upload limit
- `infra/.env.example` — Environment variable template

### Acceptance Tests

`acceptance_test/` — Playwright + Python (uv). Host-based, against `http://localhost:8080`. Relies on seed data. `conftest.py` has container discovery (podman ps), DB query helpers, admin auth.

**Important:** Always run Python commands via `uv` (e.g. `uv run pytest`). Do not activate or reference `.venv` directly. `uv` manages virtualenvs automatically.

## Key Conventions

- **Contract-first API:** `openapi/spec.yaml` is source of truth. Never edit generated code — update spec and run `make generate`
- **API prefix:** All endpoints `/api/v1/`
- **Error format:** RFC 9457 Problem Details (`application/problem+json`)
- **Server-authoritative pricing:** Client never submits prices
- **Soft delete** for products; orders are fully immutable
- **Pre-signed URLs** for MinIO uploads — backend never proxies file bytes
- **Cursor pagination:** Spring `Slice<>`, no COUNT queries
- **Order lines:** No quantity field — each line is a unique personalized item
- **Admin auth:** HTTP Basic (demo: admin/admin)
- **Frontend cart:** Stored in localStorage, no backend session

### Coding
- **Feature implemention**: You SHALL follow OpenSpec pipeline. You may be instructed by a human, or you may need to apply manuall `opsx:explore` or `opsx:apply` commands with due dilligence
- **Chunks**: Implementation SHALL be done in indivitual chunks, test often i.e., with `./gradlew test --tests ...` or similar for Svelte project
- **Test implementation**: Any feature MUST be covered by at least unit test. You SHALL implement acceptance tests in `Playwright`
- **Static code analysis**: Prior to building Docker images, you SHALL validate and analyze statically freshly generated code
- **Agents**: Spawn up to 2 parallel agents to speed up implementation should the task be independent

## Design System ("The Obsidian Monolith")

- **No rounded corners** on interactive elements
- **No visible shadows** — depth via tonal contrast only
- **No 1px solid borders** for sectioning — use background tonal shifts
- **Fonts:** Space Grotesk (display), Manrope (body)
- **Colors:** Dark surface palette (#0e0e0e → #353534), accent `#ffb4a8`, CTA `#5c0000` → `#920703`
- **Brand voice:** "ACQUIRE" not "Buy", "CURRENT INVENTORY" not "Cart", "Artifacts" not "Products"

## Key Documents

| File | Purpose |
|------|---------|
| `AGENTS.md` | Single source of truth — full architecture, data model, conventions |
| `prd-backend.md` | Backend spec — API endpoints, error format, data model |
| `prd-ui-ux.md` | Full design system — colors, typography, components, page layouts |
| `prd-infrastructure.md` | Container architecture, service topology |
| `prd-shop-proposal.md` | Initial product proposal, goals/non-goals, user stories |
| `docs/frontend/` | HTML mockups with Tailwind CSS + screenshot captures |
