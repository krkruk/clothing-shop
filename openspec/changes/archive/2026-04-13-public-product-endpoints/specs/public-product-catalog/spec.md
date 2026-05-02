## ADDED Requirements

### Requirement: List products with cursor pagination
The system SHALL expose `GET /api/v1/products` returning a cursor-paginated list of active products. The endpoint SHALL require no authentication. Products SHALL be ordered by `created_at` descending, then `id` descending (newest first). The system SHALL NOT execute a COUNT query. Soft-deleted products (`is_active = false`) SHALL NOT appear in results.

#### Scenario: First page with default limit
- **WHEN** a GET request is sent to `/api/v1/products` with no query parameters
- **THEN** the system SHALL return HTTP 200 with a JSON body containing `items` (array, max 20 entries), `nextCursor` (string or null), and `hasMore` (boolean). Each item SHALL contain `id` (UUID), `name` (string), `price` (decimal), `imageUrl` (string or null), `shortDescription` (string), and `category` object with `slug` and `name`.

#### Scenario: Pagination with cursor
- **WHEN** a GET request is sent to `/api/v1/products?cursor=<opaque_string>&limit=10`
- **THEN** the system SHALL decode the cursor, return the next 10 products after the cursor position, and set `hasMore` to true if more products exist beyond this page. The `nextCursor` SHALL encode the `created_at` and `id` of the last item in the current page.

#### Scenario: Last page
- **WHEN** a GET request is sent with a cursor that returns fewer items than the requested limit
- **THEN** the system SHALL return `hasMore: false` and `nextCursor: null`.

#### Scenario: Filter by category slug
- **WHEN** a GET request is sent to `/api/v1/products?category=tops`
- **THEN** the system SHALL return only products belonging to the category with slug `tops`.

#### Scenario: Non-existent category slug
- **WHEN** a GET request is sent to `/api/v1/products?category=nonexistent`
- **THEN** the system SHALL return HTTP 200 with `items: []`, `nextCursor: null`, `hasMore: false` (not an error).

#### Scenario: Custom limit
- **WHEN** a GET request is sent to `/api/v1/products?limit=5`
- **THEN** the system SHALL return at most 5 items per page.

#### Scenario: Limit exceeds maximum
- **WHEN** a GET request is sent to `/api/v1/products?limit=200`
- **THEN** the system SHALL cap the limit at 100 and return at most 100 items.

#### Scenario: Empty database
- **WHEN** a GET request is sent to `/api/v1/products` and no active products exist
- **THEN** the system SHALL return HTTP 200 with `items: []`, `nextCursor: null`, `hasMore: false`.

### Requirement: Cursor encoding and decoding
The system SHALL use opaque base64-encoded JSON cursors. The cursor SHALL encode the `createdAt` (ISO 8601) and `id` (UUID) of the last item on the current page. The cursor SHALL be treated as opaque by clients — they SHALL NOT need to decode it.

#### Scenario: Cursor encodes pagination state
- **WHEN** the system generates a cursor for a page ending with product having `createdAt=2026-04-13T10:30:00Z` and `id=abc-123`
- **THEN** the cursor SHALL be `base64({"createdAt":"2026-04-13T10:30:00Z","id":"abc-123"})`.

#### Scenario: Invalid cursor value
- **WHEN** a GET request is sent to `/api/v1/products?cursor=invalid_base64!!!`
- **THEN** the system SHALL return HTTP 400 with `application/problem+json` indicating the cursor is invalid.

### Requirement: Primary image for list view
The system SHALL include a single `imageUrl` for each product in the list response. The primary image SHALL be the `product_image` with the lowest `display_order` for that product. When no image exists, `imageUrl` SHALL be null. The image URL SHALL be a relative path prefixed with `/images/` followed by the object key stored in the database.

#### Scenario: Product with images
- **WHEN** a product has 3 images with `display_order` values 0, 1, 2
- **THEN** the `imageUrl` SHALL be constructed from the image with `display_order=0` as `/images/{object_key}`.

#### Scenario: Product without images
- **WHEN** a product has no associated images
- **THEN** the `imageUrl` SHALL be `null`.

### Requirement: Get product detail
The system SHALL expose `GET /api/v1/products/{id}` returning full product information. The endpoint SHALL require no authentication.

#### Scenario: Successful product detail
- **WHEN** a GET request is sent to `/api/v1/products/{id}` for an existing active product
- **THEN** the system SHALL return HTTP 200 with `id` (UUID), `name` (string), `description` (string), `shortDescription` (string), `price` (decimal), `category` (object with `slug` and `name`), `images` (array ordered by `display_order`), `fabrication` (object or null with `content` and `care`), and `ethics` (object or null with `origin` and `impact`).

#### Scenario: Product not found
- **WHEN** a GET request is sent to `/api/v1/products/{id}` for a non-existent product ID
- **THEN** the system SHALL return HTTP 404 with `application/problem+json`.

#### Scenario: Soft-deleted product
- **WHEN** a GET request is sent to `/api/v1/products/{id}` for a product where `is_active = false`
- **THEN** the system SHALL return HTTP 404 with `application/problem+json`.

### Requirement: Product detail images
The detail response SHALL include all images for the product, ordered by `display_order` ascending. Each image entry SHALL contain `imageId` (UUID), `imageUrl` (relative path `/images/{object_key}`), `alt` (string or null), and `displayOrder` (integer).

#### Scenario: Product with multiple images
- **WHEN** a product has 3 images with `display_order` values 0, 1, 2 and alt texts "Front view", "Back view", null
- **THEN** the `images` array SHALL contain 3 entries in display_order, each with `imageId`, `imageUrl`, `alt`, and `displayOrder` fields.

#### Scenario: Product with no images
- **WHEN** a product has no associated images
- **THEN** the `images` array SHALL be empty (`[]`).

### Requirement: Single-query list performance
The system SHALL retrieve all data for the product list endpoint using a single native SQL query with JOIN to the `category` table and LEFT JOIN LATERAL to the `product_image` table for the primary image. The system SHALL NOT execute per-product queries for category or image data.

#### Scenario: No N+1 queries on product list
- **WHEN** a GET request is sent to `/api/v1/products?limit=20`
- **THEN** the system SHALL execute at most 1 SQL query to retrieve all product summaries with their categories and primary images.
