## 1. OpenAPI Contract

- [x] 1.1 Create `openapi/spec.yaml` with OpenAPI 3.1: info, servers (url: `/api/v1`), security scheme (HTTP Basic `basicAuth`), tag `admin-products`. Define paths for `POST /admin/products` and `POST /admin/products/{id}/image` with operationIds `createProduct` and `uploadProductImage`. Define component schemas: `CreateProductRequest`, `ProductResponse`, `ImageUploadResponse`, `CategoryDto`, `FabricationDto`, `EthicsDto`, `ProblemDetail`, `FieldError`. All response errors use `application/problem+json` with `ProblemDetail` schema
- [x] 1.2 Add all validation constraints to schemas: `name` (required, minLength 2, maxLength 200), `description` (required), `shortDescription` (required), `price` (required, minimum 0.01 exclusiveMinimum true), `categoryId` (required, format uuid), `alt` (maxLength 200), multipart file content type enum (image/jpeg, image/png, image/webp) documented in description. No cross-field validation — PoC scope

## 2. Gradle Build Configuration

- [x] 2.1 Add `org.openapitools:openapi-generator-gradle-plugin` to plugins block
- [x] 2.2 Configure `openApiGenerate` task: generator `spring`, input spec `${rootDir}/../openapi/spec.yaml`, output dir `${buildDir}/generated`, apiPackage `com.clothingshop.api`, modelPackage `com.clothingshop.model`, configOptions: `interfaceOnly=true`, `useTags=true`, `openApiNullable=false`, `documentationProvider=springdoc`, `skipDefaultInterface=true`. Add `swagger-annotations` and `springdoc-openapi` dependencies for generated code annotations
- [x] 2.3 Wire `compileJava` to depend on `openApiGenerate` so code generation runs before compilation
- [x] 2.4 Add `src/main/generated` to main sourceSet and `.gitignore`
- [x] 2.5 Add dependencies: AWS S3 SDK v2 (`software.amazon.awssdk:s3`, `software.amazon.awssdk:auth`), Liquibase, Spring Security, `spring-boot-starter-data-jpa`, PostgreSQL driver, `spring-boot-starter-validation`
- [x] 2.6 Add TestContainers dependencies (`postgresql`, `minio` modules), JUnit 5, configure `test` task to exclude `@Tag("component")`
- [x] 2.7 Create `testComponent` Gradle task: separate `sourceSet` using `src/testComponent/java` and `src/testComponent/resources`, include only `@Tag("component")`, depend on `test` output
- [x] 2.8 Configure `build` task to depend on both `test` and `testComponent`
- [x] 2.9 Update `backend/settings.gradle.kts`: rename root project from `demo` to `backend`

## 3. Database Schema (Liquibase)

- [x] 3.1 Create `backend/src/main/resources/db/changelog/db.changelog-master.xml` — master changelog including all migration files
- [x] 3.2 Create migration `V001__create_category_table.xml`: `category` table with `id` (UUID PK), `slug` (VARCHAR 50 UNIQUE NOT NULL), `name_en/pl/es` (VARCHAR 100 NOT NULL), `description_en/pl/es` (TEXT), `display_order` (INTEGER NOT NULL), `created_at`, `updated_at` (TIMESTAMPTZ)
- [x] 3.3 Create migration `V002__seed_categories.xml`: INSERT seed data for initial categories with names and descriptions in EN, PL, ES
- [x] 3.4 Create migration `V003__create_product_table.xml`: `product` table with `id` (UUID PK), `name` (VARCHAR 200), `description` (TEXT), `short_description` (TEXT), `price` (DECIMAL 10,2), `category_id` (UUID FK → category.id), `fabrication_content` (VARCHAR 500), `fabrication_care` (VARCHAR 500), `ethics_origin` (VARCHAR 200), `ethics_impact` (VARCHAR 500), `is_active` (BOOLEAN DEFAULT true), `created_at`, `updated_at` (TIMESTAMPTZ)
- [x] 3.5 Create migration `V004__create_product_image_table.xml`: `product_image` table with `id` (UUID PK), `product_id` (UUID FK → product.id), `object_key` (VARCHAR NOT NULL), `variant` (ENUM: ORIGINAL, GALLERY, THUMBNAIL, DEFAULT ORIGINAL), `alt` (VARCHAR 200), `display_order` (INTEGER NOT NULL), `created_at` (TIMESTAMPTZ)

## 4. JPA Entities

- [x] 4.1 Create `Category.java` entity with Lombok, mapping to `category` table with all locale columns
- [x] 4.2 Create `Product.java` entity with Lombok, mapping to `product` table, `@ManyToOne` relationship to `Category`
- [x] 4.3 Create `ProductImage.java` entity with Lombok, mapping to `product_image` table, `@ManyToOne` relationship to `Product`, `variant` enum field
- [x] 4.4 Create `ImageVariant.java` enum with values `ORIGINAL`, `GALLERY`, `THUMBNAIL`

## 5. Spring Configuration

- [x] 5.1 Create `application.yml`: configure datasource, Liquibase, JPA, multipart max-file-size 5MB, MinIO connection properties (`minio.endpoint`, `minio.access-key`, `minio.secret-key`, `minio.bucket`, `minio.region`), admin credentials
- [x] 5.2 Create `S3Config.java`: `@Configuration` class that builds and provides `S3Client` bean configured with MinIO endpoint and credentials from Spring properties
- [x] 5.3 Create `MinioBucketInit.java`: `ApplicationRunner` that checks if configured bucket exists and creates it if not
- [x] 5.4 Create `SecurityConfig.java`: Spring Security config with HTTP Basic auth for `/api/v1/admin/**`, permit-all for other paths, in-memory user `admin:admin` (overridable via env vars)

## 6. Repositories

- [x] 6.1 Create `CategoryRepository.java`: Spring Data JPA repository with `findBySlug(String slug)` and `existsById(UUID)`
- [x] 6.2 Create `ProductRepository.java`: Spring Data JPA repository with `save()` and `findById(UUID)`
- [x] 6.3 Create `ProductImageRepository.java`: Spring Data JPA repository with `save()` and `countByProductId(UUID)` for auto-incrementing display order

## 7. Service Layer

- [x] 7.1 Create `ProductService.java`: `createProduct(CreateProductRequest)` — receives generated model from controller, validates category exists, converts to JPA entity, saves, converts entity back to generated `ProductResponse` model
- [x] 7.2 Create `ProductService.java`: `uploadProductImage(UUID productId, MultipartFile file, String contentType, String alt)` — validate product exists, validate content type (image/jpeg, image/png, image/webp), generate UUID image ID, build object key `products/{productId}/{imageId}/original.{ext}`, call `s3Client.putObject()`, save `ProductImage` entity, return generated `ImageUploadResponse` model
- [x] 7.3 Extract content-type-to-extension mapping (jpeg→jpg, png→png, webp→webp) as a utility method in `ProductService`

## 8. Error Handling

- [x] 8.1 Create `GlobalExceptionHandler.java`: `@RestControllerAdvice` that handles validation errors (MethodArgumentNotValidException → 422), business exceptions (404, 422), and generic errors (500), all returning RFC 9457 `application/problem+json`
- [x] 8.2 Create `ResourceNotFoundException.java`: custom exception for 404 responses
- [x] 8.3 Create `ValidationException.java`: custom exception for business validation failures (e.g., category not found)

## 9. Controller

- [x] 9.1 Create `AdminProductController.java`: `@RestController` that implements the generated `AdminProductsApi` interface. Annotate with `@RequestMapping("/api/v1/admin/products")`. Override methods use generated model types (`CreateProductRequest`, `ProductResponse`, `ImageUploadResponse`) directly as method arguments and return types — no hand-written DTOs, no mapper classes
- [x] 9.2 Implement `createProduct()` method body: delegate to `ProductService.createProduct()`, return `ResponseEntity<ProductResponse>` with HTTP 201
- [x] 9.3 Implement `uploadProductImage()` method body: delegate to `ProductService.uploadProductImage()`, return `ResponseEntity<ImageUploadResponse>` with HTTP 201

## 10. Unit Tests (./gradlew test)

- [x] 10.1 Create `AdminProductControllerTest.java`: `@WebMvcTest` tests for POST create product — validation errors (422), missing auth (401), successful creation (201), category not found (422). Construct requests using generated model types
- [x] 10.2 Create `AdminProductControllerTest.java`: `@WebMvcTest` tests for POST upload image — product not found (404), successful upload (201)
- [x] 10.3 Create `ProductServiceTest.java`: unit tests with `@Mock` CategoryRepository, ProductRepository, ProductImageRepository, S3Client — create product happy path, category not found, upload image happy path, product not found, unsupported content type. Use generated models as test inputs/outputs

## 11. Component Tests (./gradlew testComponent)

- [x] 11.1 Create component test base class: `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@Testcontainers`, TestContainers for PostgreSQL 16 and MinIO, `@Tag("component")`, configured datasource and S3 client to point at containers
- [x] 11.2 Create `ProductComponentTest.java`: test create product end-to-end — POST product, verify 201 response, verify record in database
- [x] 11.3 Create `ImageUploadComponentTest.java`: test image upload end-to-end — create product, upload image, verify 201 response, verify file exists in MinIO, verify `product_image` record in database with correct object key and variant=ORIGINAL
- [x] 11.4 Create `ImageUploadComponentTest.java`: test upload image for non-existent product returns 404
- [x] 11.5 Create `ImageUploadComponentTest.java`: test upload image exceeds 5MB returns error

## 12. Backend Dockerfile

- [x] 12.1 Create `backend/Dockerfile`: multi-stage build with `build` stage (Eclipse Temurin JDK 21, runs `./gradlew build` including OpenAPI generation) and `prod` stage (Eclipse Temurin JRE 21 slim, copies JAR, exposes 8080, healthcheck via actuator)

## 13. Frontend Dockerfile

- [x] 13.1 Create `frontend/Dockerfile`: multi-stage build with `build` stage (node:20-alpine, runs `npm run build`) and `prod` stage (nginx:alpine, copies built assets)

## 14. Infrastructure Files

- [x] 14.1 Create `infra/compose.yml`: full-stack services — `postgres` (Postgres 16), `minio` (MinIO with console), `backend` (builds from `../backend`), `frontend` (builds from `../frontend`), `proxy` (nginx:alpine, publishes 8080). Healthchecks on postgres and minio. `depends_on` with condition `service_healthy`. Internal network `clothingshop-net`
- [x] 14.2 Create `infra/compose.local.yml`: `postgres` (exposes 5432) and `minio` (exposes 9000, 9001) only. Same env vars as full stack
- [x] 14.3 Create `infra/nginx/nginx.conf`: reverse proxy — `location /` proxies to `frontend:80`, `location /api/` proxies to `backend:8080`
- [x] 14.4 Create `infra/.env.example`: all required environment variables with example values and comments

## 15. Makefile & README

- [x] 15.1 Create root `Makefile` with targets: `infra-local` (starts compose.local.yml), `build` (builds compose.yml images), `dev` (starts compose.yml), `clean` (stops and removes containers + volumes)
- [x] 15.2 Create `README.md`: prerequisites, full-stack launch instructions, local dev setup, testing instructions (`./gradlew test` and `./gradlew testComponent`), prominent `admin:admin` demo-only auth warning with production guidance (Keycloak)
