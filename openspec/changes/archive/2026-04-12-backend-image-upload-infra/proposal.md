## Why

The project needs its first working backend API and containerized infrastructure. Currently only a Spring Boot skeleton (`DemoApplication.java`) exists with no endpoints, no database schema, no object storage integration, and no Docker/Podman Compose setup. This change establishes the foundational backend capabilities — product creation and image upload to MinIO — along with the full infrastructure needed to run and test the stack.

## What Changes

- **Add OpenAPI contract**: Create `openapi/spec.yaml` defining schemas and paths for the two admin endpoints. Configure Gradle openapi-generator plugin with `interfaceOnly=true` to generate Spring API interfaces and model classes. Controller implements generated interface, uses generated models directly — no hand-written DTOs
- **Add two admin API endpoints**: `POST /api/v1/admin/products` (create product) and `POST /api/v1/admin/products/{id}/image` (upload image, store in MinIO, link to product in Postgres)
- **Add Liquibase migrations**: `category`, `product`, and `product_image` tables with seed data for 3 locales (EN, PL, ES)
- **Add MinIO integration**: AWS S3 SDK v2 configured against MinIO, bucket auto-creation on startup, image upload via `putObject`
- **Add HTTP Basic auth**: `admin:admin` credentials for admin endpoints (demo only, production warning required)
- **Add RFC 9457 error handling**: Global exception handler returning `application/problem+json`
- **Add containerized infrastructure**: Full-stack `compose.yml` (postgres, minio, backend, frontend, nginx), dev-only `compose.local.yml` (postgres + minio), multi-stage Dockerfiles for backend and frontend
- **Add test pyramid**: Unit tests + `@WebMvcTest` under `./gradlew test`, component tests with TestContainers (Postgres + MinIO) under `./gradlew testComponent` using JUnit tags
- **Add Makefile targets**: `infra-local`, `build`, `dev` for infrastructure orchestration
- **Add README**: Full-stack and testing launch instructions with demo auth warning

## Capabilities

### New Capabilities
- `product-api`: Admin endpoints for creating products and uploading product images to MinIO. Contract-first via OpenAPI — generated interfaces define controller signatures, generated models replace hand-written DTOs. Validation from OpenAPI schema constraints only (PoC scope)
- `image-storage`: MinIO integration via AWS S3 SDK v2 — bucket initialization, object upload with key convention `products/{productId}/{imageId}/{variant}.ext`, image variant ENUM schema (ORIGINAL, GALLERY, THUMBNAIL) ready for future resizing
- `backend-infra`: Containerized infrastructure — Dockerfiles, compose files, nginx config, Makefile targets, environment variable management
- `backend-testing`: Test pyramid for the backend — unit tests, `@WebMvcTest`, component tests with TestContainers (Postgres 16 + MinIO), JUnit tag separation

### Modified Capabilities
<!-- No existing capabilities to modify — this is the first backend change -->

## Impact

- **`openapi/`**: New `spec.yaml` — API contract source of truth for the two admin endpoints
- **`backend/`**: New Gradle dependencies (openapi-generator plugin, AWS S3 SDK, TestContainers, Liquibase, Spring Security), new source files for controllers (implementing generated interfaces), services, repositories, entities, config, exceptions, Liquibase XML migrations, test source sets. Generated models in `src/main/generated/` (gitignored)
- **`infra/`**: New directory with `compose.yml`, `compose.local.yml`, `.env.example`, `nginx/nginx.conf`
- **`frontend/`**: New multi-stage `Dockerfile` (node:20-alpine)
- **Root**: New `Makefile`, new `README.md`
- **External dependencies**: MinIO container, PostgreSQL 16 container, nginx container, Eclipse Temurin JDK 21 base image
- **Ports**: nginx publishes `:8080` to host; `compose.local.yml` exposes Postgres `:5432` and MinIO `:9000/:9001`
