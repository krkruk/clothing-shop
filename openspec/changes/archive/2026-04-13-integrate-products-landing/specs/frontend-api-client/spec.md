## ADDED Requirements

### Requirement: OpenAPI TypeScript client generation
The frontend build SHALL include a code generation step that produces a TypeScript HTTP client from `openapi/spec.yaml`. The generated code SHALL output to `frontend/src/api/generated/` and SHALL be gitignored. The generator SHALL be invoked via `make generate` and also via `npm run generate:api` in the frontend directory.

#### Scenario: Generate TypeScript client from spec
- **WHEN** `make generate` is executed
- **THEN** a TypeScript client SHALL be generated in `frontend/src/api/generated/` with typed request/response interfaces matching the OpenAPI spec

#### Scenario: Generated types match API schema
- **WHEN** the generated client is imported in a Svelte component
- **THEN** TypeScript SHALL provide type checking for `ProductListResponse`, `ProductSummary`, `ProductDetailResponse`, and error types matching `openapi/spec.yaml`

### Requirement: Typed product list fetch
The generated client SHALL expose a function to call `GET /api/v1/products` with typed parameters (`cursor`, `limit`, `category`) and return a typed `ProductListResponse`.

#### Scenario: Fetch products with pagination parameters
- **WHEN** the client's product list function is called with `{ limit: 7 }`
- **THEN** it SHALL send `GET /api/v1/products?limit=7` and return a typed `ProductListResponse` with `items`, `nextCursor`, and `hasMore`

#### Scenario: Fetch next page with cursor
- **WHEN** the client's product list function is called with `{ cursor: "abc123", limit: 7 }`
- **THEN** it SHALL send `GET /api/v1/products?cursor=abc123&limit=7` and return the next page of results

### Requirement: Relative API base path
The generated client SHALL use `/api/v1` as the base path (relative). All API requests from the browser SHALL be routed through the nginx proxy (same-origin). No CORS configuration SHALL be required.

#### Scenario: API requests use relative URLs
- **WHEN** a product list request is made from the browser
- **THEN** the request URL SHALL be `/api/v1/products` (relative), routed by nginx to the backend
