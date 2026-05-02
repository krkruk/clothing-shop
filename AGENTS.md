# AGENTS.md — Clothingshop

## Project Overview

Clothingshop is a lightweight e-commerce application for an alternative clothing brand. Users browse a product catalog, personalize items with measurements, manage a cart, and place orders. Admins manage products, categories, and view orders.

**Scope:** MVP / Proof-of-Concept — minimal operational overhead, no payment gateway, no user accounts in v1.

---

## Architecture

**Monorepo, containerized, contract-first.** Everything runs via Podman Compose behind an nginx reverse proxy. A single `Makefile` is the entry point for all operations.

```
Browser → nginx (:8080) → /        → Frontend (Svelte SPA)
                         → /api/*  → Backend  (Spring Boot)
Backend → Postgres 16 (schema via Liquibase)
Backend → MinIO (S3-compatible product images)
```

- Only nginx publishes a host port (8080). All other services communicate on an internal network.
- No CORS — nginx provides same-origin routing.
- Generated code (from OpenAPI) is gitignored and produced at build time.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Svelte + TypeScript, Vite |
| Backend | Java 21, Spring Boot 3.x |
| Database | PostgreSQL 16 (Liquibase migrations) |
| Object Storage | MinIO (S3-compatible, pre-signed URL uploads) |
| Reverse Proxy | nginx:alpine |
| Container Runtime | Podman (rootless), Compose v3 spec |
| API Contract | OpenAPI 3.1 (`openapi/spec.yaml`) |
| Code Generation | openapi-generator-cli (typescript-fetch + spring) |
| Acceptance Tests | Playwright + Python (uv) |
| Task Runner | GNU Make |

---

## Repository Structure

```
simple_shop/
├── compose.yml                  # All services
├── Makefile                     # Single entry point
├── .env                         # Gitignored local env vars
├── openapi/
│   └── spec.yaml                # API contract — source of truth
├── backend/
│   ├── Dockerfile               # Multi-stage: dev + prod
│   ├── build.gradle
│   └── src/main/
│       ├── generated/           # Gitignored — Java DTOs & interfaces from OpenAPI
│       ├── java/                # Hand-written application code
│       └── resources/db/changelog/  # Liquibase migrations
├── frontend/
│   ├── Dockerfile               # Multi-stage: dev + prod
│   ├── package.json
│   └── src/
│       └── api/generated/       # Gitignored — TS client from OpenAPI
├── infra/
│   ├── .env.example
│   ├── nginx/nginx.conf
│   ├── postgres/seed/dev-data.sql
│   └── minio/seed/images/   # Committed product photos (key-prefixed .jpg)
└── acceptance_test/
    ├── pyproject.toml           # uv: pytest, pytest-playwright
    ├── conftest.py
    └── tests/
```

---

## Development Workflow

All operations go through the Makefile:

| Command | Purpose |
|---------|---------|
| `make setup` | First-time setup after clone (generate + build) |
| `make generate` | Run OpenAPI code generation for both frontend and backend |
| `make dev` | Start all services in dev mode (hot-reload, volume mounts) |
| `make build` | Build all container images (prod targets) |
| `make seed` | Load sample data into Postgres and MinIO |
| `make test-acceptance` | Run Playwright e2e tests (requires dev + seed) |
| `make clean` | Stop containers, remove volumes, delete generated code |

**Prerequisites:** Podman, Make. Acceptance tests also require Python 3.12+, uv, Playwright browsers.

---

## API Design

### Contract-First OpenAPI

1. Define/edit `openapi/spec.yaml`
2. Run `make generate`
3. Generated Java interfaces/DTOs land in `backend/src/main/generated/`
4. Generated TypeScript client lands in `frontend/src/api/generated/`
5. Application code compiles against generated types

**Never edit generated code directly.** Always update the OpenAPI spec and regenerate.

### API Versioning

All endpoints prefixed with `/api/v1/`. No header or query-parameter versioning in v1.

### Error Format

All errors use **RFC 9457 Problem Details** (`application/problem+json`):

```json
{
  "type": "https://example.com/errors/validation",
  "title": "Validation Error",
  "status": 422,
  "detail": "Request body contains invalid fields",
  "fields": [
    { "field": "items[0].personalization.waistCm", "message": "must be between 40 and 200" }
  ]
}
```

### Endpoints

**Public (no auth):**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/products` | List products (cursor pagination, category filter) |
| GET | `/api/v1/products/{id}` | Product detail |
| GET | `/api/v1/categories` | List categories |
| POST | `/api/v1/orders` | Place order |

**Admin (HTTP Basic Auth):**

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/admin/products` | Create product |
| GET | `/api/v1/admin/products` | List all products incl. inactive (cursor pagination) |
| PUT | `/api/v1/admin/products/{id}` | Update product (partial — non-null fields only) |
| DELETE | `/api/v1/admin/products/{id}` | Soft-delete product |
| POST | `/api/v1/admin/products/{id}/image` | Upload image |
| DELETE | `/api/v1/admin/products/{id}/images/{imageId}` | Delete product image |
| POST | `/api/v1/admin/categories` | Create category |
| PUT | `/api/v1/admin/categories/{id}` | Update category |
| DELETE | `/api/v1/admin/categories/{id}` | Delete category (blocked if non-empty) |
| GET | `/api/v1/admin/orders` | List orders (cursor pagination) |
| GET | `/api/v1/admin/orders/{id}` | Order detail |

---

## Backend Conventions

- **Spring Boot 3.x, Java 21.** Generated OpenAPI interfaces define controller contracts — implement them.
- **Validation in two layers:** Jakarta Validation (static, from OpenAPI constraints) at controller boundary; business rules in the service layer.
- **Pagination:** Spring `Slice<>` with opaque cursor. No COUNT queries.
- **Server-authoritative pricing:** Client never submits prices at checkout. Admin submits prices as string/BigDecimal to avoid floating-point precision loss.
- **Soft delete:** Products are marked inactive, not removed. Orders are fully immutable.
- **Pre-signed URLs:** Backend generates MinIO upload URLs; client uploads directly. Backend never proxies file bytes.
- **No CORS configuration** — nginx handles same-origin routing.
- **Order lines have no quantity field** — each line is a unique personalized item. Two identical shirts = two order lines.
- **Personalization:** Structured columns (silhouette enum, waist/hips/height decimals). Not JSONB.
- **Order states:** DB enum has 6 states; v1 only creates `PLACED`. No status transition endpoints.
- **Liquibase migrations** run on startup. Schema ownership belongs to the backend.
- **Actuator:** Health and info endpoints exposed for container health checks.

---

## Frontend Conventions

- **Svelte + TypeScript.** Use generated TypeScript client from OpenAPI.
- **Cart state:** Stored client-side in localStorage. No backend session.
- **Infinite scroll:** Products loaded progressively via cursor pagination.
- **Dark editorial aesthetic** — "The Obsidian Monolith" theme. See `prd-ui-ux.md` for full design system.

### Design System Summary

- **Fonts:** Space Grotesk (display/headlines), Manrope (body/labels)
- **Colors:** Dark surface palette (#0e0e0e → #353534), accent `#ffb4a8`, CTA `#5c0000` → `#920703`
- **No rounded corners** on any interactive element. No visible shadows — depth via tonal contrast.
- **No solid borders** — section boundaries defined by background tonal shifts only.
- **Brand voice:** "ACQUIRE" not "Buy", "CURRENT INVENTORY" not "Cart", "Artifacts" not "Products".

### Key Pages

1. **Landing Page:** Hero carousel (33.33vh) + chessboard product grid (alternating 1/3 image, 2/3 content) + category tonal shifts + infinite scroll
2. **Product Detail Page:** Hero carousel → scroll-gradient blend (dark→white) → content (2/3 description, 1/3 acquisition form) → detail grid (dark)
3. **Cart:** Slide-in drawer from right, smoked glass (90% opacity + backdrop blur)
4. **Checkout:** Full-page dark theme form, server-authoritative order submission

---

## Data Model

| Entity | Key Fields | Notes |
|--------|-----------|-------|
| **Product** | UUID, name, description, shortDescription, price (BigDecimal), currency, sku, categoryId, isActive, fabrication, ethics, timestamps | Soft-deleted (isActive=false) hidden from public |
| **Category** | UUID, name, slug (unique), description, displayOrder | Cannot delete if products exist |
| **ProductImage** | UUID, productId, objectKey, alt, displayOrder | Binary in MinIO, metadata in DB |
| **Order** | UUID, customer (name/email/address), status, totalPrice, createdAt | Immutable after creation |
| **OrderLine** | UUID, orderId, productId, productName snapshot, price snapshot, personalization | No quantity — each line is unique |
| **Personalization** | UUID, orderLineId, silhouette (BOXY/CURVY/OTHER), waistCm, hipsCm, heightCm | One-to-one with order line |

---

## Testing

- **Acceptance tests** (`acceptance_test/`): Playwright + Python, host-based, against `http://localhost:8080`
- Tests rely on seed data loaded via `make seed`
- PoC scope: critical user journey (browse → product detail → acquire → cart → checkout → confirmation)
- Run: `make dev && make seed && make test-acceptance`

---

## Environment Variables

Template at `infra/.env.example`. Key variables:

| Variable | Purpose |
|----------|---------|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Database connection |
| `SPRING_DATASOURCE_URL` | JDBC connection string |
| `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `MINIO_BUCKET` | Object storage |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | HTTP Basic auth for admin |
| `NGINX_PORT` | Host port (default 8080) |

---

## v1 Non-Goals

These are explicitly out of scope:

- Payment gateway integration
- User accounts / authentication beyond HTTP Basic admin
- Inventory / stock tracking
- Order status transitions beyond PLACED
- Multi-language / multi-currency
- CI/CD pipeline
- CDN, monitoring, logging infrastructure
- Multi-node orchestration (Kubernetes, Swarm)
