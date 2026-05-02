# Clothingshop

A lightweight e-commerce application for an alternative clothing brand. Users browse a product catalog with dark editorial aesthetics, personalize items with measurements, manage a cart, and place orders. Admins manage products, categories, and view orders through a protected panel.

Note: This project is intended as an exercise in working with OpenSpec. You're welcome to fork it, but you'd likely be better off with a commercial e-commerce platform — €20/month is nothing compared to the maintenance costs of a custom-built shop like this one.

## What It Does

- **Public storefront** — landing page with hero carousel, chessboard product grid with infinite scroll, category tonal shifts, and product detail pages with acquisition forms
- **Cart & checkout** — slide-in cart drawer with quantity controls, full-page checkout with server-authoritative order submission
- **Admin panel** — HTTP Basic-authed CRUD for products and categories, image upload via pre-signed MinIO URLs, order list
- **Multi-currency** — products priced in PLN and EUR, client-side currency switching
- **Contract-first API** — OpenAPI 3.1 spec generates TypeScript and Java types; the spec is the source of truth

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Svelte 5 + TypeScript, Vite, Tailwind CSS 4 |
| Backend | Java 21, Spring Boot 3.x |
| Database | PostgreSQL 16 (Liquibase migrations) |
| Object Storage | MinIO (S3-compatible, pre-signed URL uploads) |
| Reverse Proxy | nginx:alpine |
| Container Runtime | Podman (rootless), Compose v3 |
| API Contract | OpenAPI 3.1 (`openapi/spec.yaml`) |
| Code Generation | openapi-generator-cli (typescript-fetch + spring) |
| Acceptance Tests | Playwright + Python (uv) |

## Architecture

```
Browser → nginx (:8080) → /         → Frontend (SvelteKit SPA)
                         → /api/*   → Backend  (Spring Boot)
                         → /images/* → MinIO
Backend → PostgreSQL 16 (Liquibase migrations)
Backend → MinIO (S3-compatible product images)
```

Only nginx publishes a host port (8080). All other services communicate on an internal network. No CORS — nginx provides same-origin routing.

## Prerequisites

- [Podman](https://podman.io/) with the compose plugin
- [GNU Make](https://www.gnu.org/software/make/)
- JDK 21 (for local backend development)
- Python 3.12+ with [uv](https://docs.astral.sh/uv/) (for acceptance tests)

## Quick Start

### 1. Configure environment

```bash
cp infra/.env.example infra/.env
```

### 2. Start the full stack

```bash
make dev
```

This builds and starts all services: PostgreSQL, MinIO, backend, frontend, and nginx. The app is available at **http://localhost:8080**.

### 3. Seed sample data

```bash
make seed        # Direct DB + MinIO (5 products with images)
```

Or use the API-based seeder (requires the backend to be running):

```bash
make seed-api    # Creates products via HTTP API
```

### 4. Open the app

Browse to **http://localhost:8080**. Admin panel at **http://localhost:8080/admin/login** with `admin:admin`.

## Local Development

For running the backend from your IDE while using containerized databases:

```bash
make infra-local
```

This starts only PostgreSQL (port 5432) and MinIO (ports 9000/9001).

## Testing

### Backend

```bash
make test              # Unit tests
make test-component    # Component/integration tests (TestContainers: PostgreSQL + MinIO)
```

### Frontend

```bash
cd frontend && npm test
```

### End-to-end (acceptance tests)

Requires the full stack running with seed data:

```bash
make dev && make seed && make test-acceptance
```

## Available Commands

| Command | Purpose |
|---------|---------|
| `make dev` | Build and start all services with hot-reload |
| `make build` | Build all container images (prod targets) |
| `make generate` | Run OpenAPI code generation for frontend and backend |
| `make seed` | Load 5 sample products with images (direct DB/MinIO) |
| `make seed-api` | Load 5 sample products with images (via HTTP API) |
| `make test` | Backend unit tests |
| `make test-component` | Backend component/integration tests |
| `make test-acceptance` | Playwright e2e tests |
| `make infra-local` | Start PostgreSQL + MinIO only |
| `make clean` | Stop containers, remove volumes |

## Security Warning

> **`admin:admin` credentials are for demo purposes only.**
> Production systems MUST use a proper identity provider. Never deploy the default credentials to a publicly accessible environment.

## Project Structure

```
├── openapi/spec.yaml              # API contract — source of truth
├── backend/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/                  # Hand-written application code
│       └── resources/db/changelog/ # Liquibase migrations
├── frontend/
│   ├── package.json
│   └── src/
│       └── api/generated/         # TypeScript client from OpenAPI
├── infra/
│   ├── compose.yml                # Full stack services
│   ├── compose.local.yml          # PostgreSQL + MinIO only
│   ├── nginx/nginx.conf
│   ├── postgres/seed/dev-data.sql # Seed SQL (5 products)
│   └── minio/seed/images/         # Committed product photos
├── acceptance_test/
│   ├── pyproject.toml
│   └── tests/
└── Makefile                       # Single entry point for all operations
```
