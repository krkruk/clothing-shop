## ADDED Requirements

### Requirement: List categories endpoint
The system SHALL provide a `GET /api/v1/categories` endpoint (public, no auth required) that returns all active categories ordered by `display_order`.

#### Scenario: Fetch all categories
- **WHEN** a GET request is made to `/api/v1/categories`
- **THEN** the response is 200 with a JSON array of category objects, each containing `id` (UUID), `slug` (string), and `name` (string)

#### Scenario: Categories ordered by display_order
- **WHEN** categories exist with display_order values 1, 2, 3, 4
- **THEN** they are returned in ascending display_order sequence

### Requirement: Category schema in OpenAPI spec
The response schema SHALL be defined in `openapi/spec.yaml` with operationId `listCategories` under the `products` tag.

#### Scenario: Generated code includes category API
- **WHEN** `make generate` is run after adding the endpoint to spec.yaml
- **THEN** backend generates a `CategoriesApi` interface and frontend generates a corresponding TypeScript client
