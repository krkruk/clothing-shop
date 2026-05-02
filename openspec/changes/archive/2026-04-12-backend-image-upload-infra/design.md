## Context

The project currently has a Spring Boot 4.0.5 skeleton with Java 21 and Gradle Kotlin DSL — just `DemoApplication.java` and no endpoints, database, or object storage. The frontend is a Svelte skeleton without a Dockerfile. There is no containerized infrastructure, no Makefile, no compose files.

This change builds the first working backend API (product creation + image upload) and the full containerized infrastructure to run it. The AGENTS.md, `prd-backend.md`, and `prd-infrastructure.md` provide extensive specifications that this design follows.

## Goals / Non-Goals

**Goals:**
- Two working admin endpoints: product creation and image upload to MinIO
- Full Liquibase schema for `category`, `product`, `product_image` tables with seed data
- AWS S3 SDK v2 integrated with MinIO, all connection details via env vars
- HTTP Basic auth (`admin:admin`) on admin endpoints
- RFC 9457 error handling
- Full-stack compose.yml and local dev compose.local.yml
- Test pyramid: unit tests + component tests with TestContainers
- Makefile and README for developer onboarding

**Non-Goals:**
- Product listing, update, delete endpoints
- Category CRUD endpoints
- Image resizing (ORIGINAL only for now, schema ready for GALLERY/THUMBNAIL)
- Pre-signed URL flow (backend uploads directly via S3 SDK)
- Order management
- CORS configuration (nginx handles same-origin)
- Production-grade auth (Keycloak, JWT)

## Decisions

### D1: AWS S3 SDK v2 over MinIO Java SDK
**Decision**: Use `software.amazon.awssdk:s3` configured against MinIO endpoint.
**Why**: Standard Spring ecosystem choice. Portable to AWS S3, Ceph, or any S3-compatible backend. MinIO SDK would lock to MinIO-only.
**Alternative**: MinIO Java SDK (`io.minio:minio`) — simpler API but non-portable.

### D2: Backend-proxied upload over pre-signed URLs
**Decision**: Frontend sends multipart to backend; backend calls `s3Client.putObject()` directly.
**Why**: Simpler to implement and test in this first iteration. Pre-signed URLs add complexity (URL generation, expiry management, separate client PUT) with no benefit when the backend handles the upload.
**Alternative**: Pre-signed URL flow (as described in PRD) — deferred to a future change if direct client-to-MinIO uploads become necessary for performance.

### D3: One DB row per image variant with ENUM
**Decision**: `product_image.variant` is an ENUM column (ORIGINAL, GALLERY, THUMBNAIL). Each variant is a separate row.
**Why**: When image resizing is implemented, no migration is needed — just insert GALLERY and THUMBNAIL rows. The object key prefix `products/{productId}/{imageId}/` stays consistent across variants.
**Alternative**: Columns per variant (`original_key`, `gallery_key`, `thumbnail_key`) — simpler queries but requires ALTER TABLE for each new variant.

### D4: Column-per-locale for category i18n
**Decision**: `category` table has `name_en`, `name_pl`, `name_es`, `description_en`, `description_pl`, `description_es`.
**Why**: System not expected to grow beyond 3 locales. No JOINs, no JSONB parsing, simple queries. Adding a new locale requires a migration — acceptable tradeoff for query simplicity.
**Alternative**: Separate `category_i18n` table — normalized but overkill for 3 fixed locales.

### D5: JUnit tags for test separation
**Decision**: Component tests use `@Tag("component")`. Gradle `test` task excludes this tag; `testComponent` task includes only this tag. `build` depends on both.
**Why**: Single source set, IDE-friendly, no separate source directory complexity.
**Alternative**: Separate `src/testComponent/java/` source set — cleaner isolation but more Gradle boilerplate and harder IDE navigation.

### D6: Liquibase XML migrations
**Decision**: Use XML format for Liquibase changelogs.
**Why**: Explicit, IDE-friendly, schema-validated. Standard format widely supported.

### D7: Demo-only auth with hardcoded credentials
**Decision**: Spring Security with `admin:admin` in application config, overridable via env vars. README contains a prominent warning.
**Why**: Fastest path to secured endpoints. Not production-safe but acceptable for MVP.
**Alternative**: In-memory user store with env-var credentials — still simple but slightly more configurable. Will use this approach.

### D8: MinIO bucket auto-creation on startup
**Decision**: `ApplicationRunner` bean checks if bucket exists and creates it if not.
**Why**: Zero manual setup. Works in both containerized and local dev environments. Idempotent.

### D9: OpenAPI contract-first with interface-only generation
**Decision**: Create `openapi/spec.yaml` defining the two admin endpoints. Configure openapi-generator-gradle-plugin with `spring` generator and `interfaceOnly=true`, `useTags=true`. The generated output is API interfaces + model classes — no generated controller skeletons.
**Why**: Contract-first ensures frontend and backend share a single source of truth. Interface-only gives the contract (method signatures, parameter annotations, return types) without forcing a controller structure on us. The hand-written controller implements the generated interface and uses generated models directly. No parallel hand-written DTO hierarchy.
**Alternative**: Full controller generation (`interfaceOnly=false`) — generates a delegate pattern that requires customization. More boilerplate, less control.

### D10: Incremental spec growth
**Decision**: `openapi/spec.yaml` contains only the two endpoints this change implements (`POST /admin/products`, `POST /admin/products/{id}/image`). Future changes add their own endpoints to the same spec file.
**Why**: Each change owns its scope. The spec evolves incrementally alongside features. Defining all AGENTS.md endpoints upfront would create unimplemented dead contracts.
**Alternative**: Full upfront spec — defines all endpoints but only implements two. Creates confusion about what's actually available.

### D11: PoC validation scope — OpenAPI constraints only
**Decision**: Validation comes exclusively from OpenAPI schema constraints (`required`, `minLength`, `maxLength`, `minimum`). The spring generator produces Jakarta Validation annotations on generated models. No additional cross-field or business-rules validation in this PoC.
**Why**: PoC scope. The generated validation covers field-level constraints. Cross-field validation (e.g., "fabrication required when category is X") and business rules will be added in later changes via service-layer checks.
**Alternative**: Additional hand-written validator classes — overkill for PoC, contradicts contract-first simplicity.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `admin:admin` is insecure | README warning; production migration path documented (Keycloak) |
| Backend proxied upload doesn't scale for large files | 5MB cap limits the problem. Pre-signed URL flow can be added later |
| Column-per-locale requires migration for new languages | Accepted — system limited to 3 locales. Low risk for MVP |
| TestContainers startup time slows `./gradlew build` | Component tests are opt-in via `testComponent`. `build` includes them but `test` alone is fast |
| Spring Boot 4.0.5 is very recent | Using latest stable; dependency starters may have renamed. Will verify during implementation |

## Open Questions

None — all design decisions resolved during exploration.
