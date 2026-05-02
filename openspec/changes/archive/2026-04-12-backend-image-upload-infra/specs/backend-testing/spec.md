## ADDED Requirements

### Requirement: Unit tests under ./gradlew test
The system SHALL provide unit tests that run via `./gradlew test` without requiring any external containers. Tests SHALL use H2 in-memory database and Mockito mocks for the S3 client.

#### Scenario: Unit tests run without containers
- **WHEN** `./gradlew test` is executed
- **THEN** all unit tests SHALL pass without requiring PostgreSQL or MinIO containers

### Requirement: WebMvcTest for admin product controller
The system SHALL provide `@WebMvcTest` tests for `AdminProductController` that validate request validation, authentication, and response format using mocked service layer.

#### Scenario: Validation error returns 422
- **WHEN** a POST request is sent to the product creation endpoint with invalid fields
- **THEN** the test SHALL verify the response is HTTP 422 with `application/problem+json` and a `fields` array

#### Scenario: Missing auth returns 401
- **WHEN** a POST request is sent without Basic auth credentials
- **THEN** the test SHALL verify the response is HTTP 401

#### Scenario: Successful product creation returns 201
- **WHEN** a valid POST request with correct auth is sent
- **THEN** the test SHALL verify the response is HTTP 201 with the expected JSON body

### Requirement: Service unit tests
The system SHALL provide unit tests for `ProductService` that test business logic with mocked repositories and S3 client.

#### Scenario: Create product with valid data
- **WHEN** the service is called with valid product creation data
- **THEN** it SHALL save the product to the repository and return the created product with a generated UUID

#### Scenario: Create product with non-existent category
- **WHEN** the service is called with a categoryId that does not exist
- **THEN** it SHALL throw an exception indicating the category was not found

#### Scenario: Upload image for non-existent product
- **WHEN** the service is called to upload an image for a product ID that does not exist
- **THEN** it SHALL throw an exception indicating the product was not found

#### Scenario: Upload image uploads to S3 and saves record
- **WHEN** the service is called with a valid product, file, and content type
- **THEN** it SHALL call `s3Client.putObject()`, save a `product_image` record with `variant=ORIGINAL`, and return the image metadata

### Requirement: Component tests under ./gradlew testComponent
The system SHALL provide component tests that run via `./gradlew testComponent` as a separate Gradle task. Tests SHALL use TestContainers to spin up real PostgreSQL 16 and MinIO containers. Tests SHALL use JUnit 5 `@Tag("component")` annotation.

#### Scenario: Component tests use real containers
- **WHEN** `./gradlew testComponent` is executed
- **THEN** TestContainers SHALL start PostgreSQL 16 and MinIO containers, tests SHALL run against real database and object storage, and containers SHALL be stopped after tests complete

### Requirement: Component test for product creation
The system SHALL provide a component test that creates a product via the API and verifies the record exists in the database.

#### Scenario: Create product end-to-end
- **WHEN** a POST request is sent to create a product with valid data
- **THEN** the response SHALL be HTTP 201, and querying the database SHALL confirm the product record exists with the correct fields

### Requirement: Component test for image upload
The system SHALL provide a component test that creates a product, uploads an image, and verifies the file exists in MinIO and the record exists in the database.

#### Scenario: Upload image end-to-end
- **WHEN** a POST request is sent to upload an image for an existing product
- **THEN** the response SHALL be HTTP 201, the image SHALL be retrievable from MinIO, and the `product_image` record SHALL exist in the database with the correct object key and variant

#### Scenario: Upload image for non-existent product returns 404
- **WHEN** a POST request is sent to upload an image for a product ID that does not exist
- **THEN** the response SHALL be HTTP 404

### Requirement: Component test runs with ./gradlew build
The `testComponent` task SHALL be configured as a dependency of the `build` task, so that `./gradlew build` runs both unit tests and component tests.

#### Scenario: Build runs all tests
- **WHEN** `./gradlew build` is executed
- **THEN** both `test` (unit tests) and `testComponent` (component tests with TestContainers) SHALL execute

### Requirement: Component tests run separately from unit tests
The `test` task SHALL NOT include component tests. Running `./gradlew test` SHALL only execute unit and WebMvc tests without spinning up containers.

#### Scenario: Unit tests run without containers
- **WHEN** `./gradlew test` is executed
- **THEN** no TestContainers SHALL start, and only unit-level tests SHALL execute

### Requirement: API integration tests via pytest
The system SHALL provide API integration tests under `acceptance_test/tests/` that run via `uv run pytest` against the full containerized stack. Tests SHALL use the Python `requests` library for HTTP calls and verify data through postgres queries and MinIO downloads.

#### Scenario: Create product integration test
- **WHEN** a POST request is sent to `/api/v1/admin/products` with valid Basic auth
- **THEN** the test SHALL verify HTTP 201, response JSON contains `id`, `name`, and `category.slug`

#### Scenario: Full product with image end-to-end test
- **WHEN** a product is created, an image is uploaded, and the image is downloaded via the nginx `/images/` proxy
- **THEN** the test SHALL verify: product data in postgres matches the request, `product_image` record has the correct `object_key`, and SHA256 of the uploaded file matches the downloaded file

### Requirement: Curl test scripts for manual API verification
The directory `acceptance_test/curl_test_scripts/` SHALL provide composable bash scripts for manual API testing against the full stack. Scripts SHALL use `curl` for HTTP calls and `podman exec` for postgres queries.

#### Scenario: Create product via curl script
- **WHEN** `01_create_product.sh` is executed
- **THEN** it SHALL query postgres for a category UUID, POST to create a product, and output the JSON response

#### Scenario: Upload image via curl script
- **WHEN** `02_upload_image.sh` is executed with a product ID
- **THEN** it SHALL POST a multipart image upload and output the JSON response with `imageId` and `imageUrl`

#### Scenario: Verify image SHA256 via curl script
- **WHEN** `03_verify_image.sh` is executed with an object key
- **THEN** it SHALL download the image via `http://localhost:8080/images/{objectKey}`, compute SHA256, and compare against the local test image file

#### Scenario: Full curl test orchestrator
- **WHEN** `run_all.sh` is executed
- **THEN** it SHALL run all three steps sequentially and output a host-accessible download URL with ready-to-use `curl` and `wget` commands
