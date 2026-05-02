## ADDED Requirements

### Requirement: Backend multi-stage Dockerfile
The backend SHALL have a multi-stage `Dockerfile` at `backend/Dockerfile` using Eclipse Temurin JDK 21 as the base image, with `build` stage (compiles with Gradle) and `prod` stage (runs with JRE 21 slim).

#### Scenario: Production image build
- **WHEN** `podman build` or `docker build` is run against the backend Dockerfile with the `prod` target
- **THEN** the resulting image SHALL contain only the JRE 21 runtime and the compiled JAR, with no build tools or source code

#### Scenario: Build stage compiles and runs tests
- **WHEN** the `build` stage executes
- **THEN** it SHALL run `./gradlew build` which compiles the code and runs unit tests

### Requirement: Frontend multi-stage Dockerfile
The frontend SHALL have a multi-stage `Dockerfile` at `frontend/Dockerfile` using `node:20-alpine` as the base image, with `build` stage (runs `npm run build`) and `prod` stage (serves static assets via `nginx:alpine`).

#### Scenario: Production image build
- **WHEN** the frontend Dockerfile is built with the `prod` target
- **THEN** the resulting image SHALL contain nginx serving the built static assets

### Requirement: Full-stack compose file
The file `infra/compose.yml` SHALL define all services: `postgres` (Postgres 16), `minio` (MinIO), `backend` (Spring Boot), `frontend` (Svelte), and `proxy` (nginx:alpine). Only the `proxy` service SHALL publish a port (8080) to the host. All services SHALL communicate on an internal network. Services SHALL use healthchecks and `depends_on` to ensure correct startup order: `backend` depends on `postgres` and `minio`; `proxy` depends on `backend` and `frontend`.

#### Scenario: Full stack starts with correct dependency order
- **WHEN** `podman compose -f infra/compose.yml up` is run
- **THEN** postgres and minio SHALL start first, backend SHALL wait until both are healthy, frontend SHALL start independently, and proxy SHALL start last, publishing port 8080

#### Scenario: No ports exposed except nginx
- **WHEN** the full stack is running
- **THEN** only port 8080 SHALL be accessible from the host; postgres, minio, backend, and frontend SHALL NOT be directly reachable

### Requirement: Local development compose file
The file `infra/compose.local.yml` SHALL define only `postgres` (Postgres 16) and `minio` (MinIO) services, with ports exposed to the host (postgres on 5432, minio on 9000 and 9001) for local backend development without containerizing the backend itself.

#### Scenario: Local infrastructure starts
- **WHEN** `make infra-local` is run
- **THEN** postgres SHALL be accessible on `localhost:5432` and minio on `localhost:9000` (API) and `localhost:9001` (console)

### Requirement: Nginx reverse proxy configuration
The file `infra/nginx/nginx.conf` SHALL configure nginx to serve the frontend on `/`, proxy `/api/*` requests to the backend service on port 8080, and proxy `/images/*` requests to MinIO for public image serving.

#### Scenario: API requests proxied to backend
- **WHEN** a request is sent to `localhost:8080/api/v1/products`
- **THEN** nginx SHALL proxy the request to the backend service

#### Scenario: Frontend requests served
- **WHEN** a request is sent to `localhost:8080/`
- **THEN** nginx SHALL serve the frontend static assets or proxy to the frontend service

#### Scenario: Image requests proxied to MinIO
- **WHEN** a request is sent to `localhost:8080/images/products/{pid}/{iid}/original.jpg`
- **THEN** nginx SHALL proxy the request to MinIO at `http://minio:9000/clothingshop/products/{pid}/{iid}/original.jpg`

### Requirement: Makefile targets
The root `Makefile` SHALL provide targets: `infra-local` (starts local postgres + minio), `build` (builds all container images), `dev` (starts full stack in dev mode), `run` (starts full stack detached), `test-acceptance` (runs pytest acceptance tests), in addition to `test` and `test-component` for backend Gradle tests.

#### Scenario: Make infra-local
- **WHEN** `make infra-local` is executed
- **THEN** it SHALL run `podman compose -f infra/compose.local.yml up -d`

#### Scenario: Make build
- **WHEN** `make build` is executed
- **THEN** it SHALL build all container images defined in `infra/compose.yml`

#### Scenario: Make dev
- **WHEN** `make dev` is executed
- **THEN** it SHALL start the full stack via `infra/compose.yml`

#### Scenario: Make run
- **WHEN** `make run` is executed
- **THEN** it SHALL start the full stack in detached mode via `infra/compose.yml`

#### Scenario: Make test-acceptance
- **WHEN** `make test-acceptance` is executed
- **THEN** it SHALL run `cd acceptance_test && uv run pytest -v`

### Requirement: Environment variable template
The file `infra/.env.example` SHALL document all required environment variables for the full stack: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `SPRING_DATASOURCE_URL`, `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`, `MINIO_REGION`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `NGINX_PORT`.

#### Scenario: Developer copies .env.example to .env
- **WHEN** a developer copies `infra/.env.example` to `.env` and fills in values
- **THEN** the compose stack SHALL start successfully with those values

### Requirement: Liquibase XML migrations
The system SHALL use Liquibase with XML-formatted changelog files in `backend/src/main/resources/db/changelog/`. The master changelog SHALL be `db.changelog-master.xml`.

#### Scenario: Schema migration on startup
- **WHEN** the backend application starts
- **THEN** Liquibase SHALL execute all pending XML migrations, creating the `category`, `product`, and `product_image` tables

### Requirement: Category seed data with locale columns
The Liquibase migrations SHALL create the `category` table with locale-specific columns: `name_en`, `name_pl`, `name_es` (VARCHAR 100, NOT NULL), `description_en`, `description_pl`, `description_es` (TEXT). The migration SHALL insert seed data for initial categories in all three locales.

#### Scenario: Categories seeded in three languages
- **WHEN** the database migration runs
- **THEN** the `category` table SHALL contain rows with names and descriptions in English, Polish, and Spanish

### Requirement: README with launch instructions
A `README.md` at the repository root SHALL provide detailed instructions for launching the full stack and the testing environment. It SHALL include a prominent warning that the `admin:admin` credentials are for demo purposes only and that production systems MUST use a proper identity provider (e.g., Keycloak).

#### Scenario: Developer follows README to launch full stack
- **WHEN** a developer follows the README instructions
- **THEN** they SHALL be able to start the full stack and test the API endpoints

#### Scenario: Demo auth warning is visible
- **WHEN** the README is read
- **THEN** it SHALL contain a clearly visible warning that `admin:admin` is insecure and not suitable for production
