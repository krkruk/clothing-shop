## 1. OpenAPI Contract

- [x] 1.1 Add `products` tag to `openapi/spec.yaml` for public product endpoints
- [x] 1.2 Define `GET /products` path with query parameters `cursor` (string, optional), `limit` (integer, optional, default 20, max 100), `category` (string, optional). Responses: 200 `ProductListResponse`, 400 `ProblemDetail`
- [x] 1.3 Define `GET /products/{id}` path with UUID path parameter. Responses: 200 `ProductDetailResponse`, 404 `ProblemDetail`
- [x] 1.4 Add `ProductListResponse` schema: `{ items: ProductSummary[], nextCursor: string|null, hasMore: boolean }`
- [x] 1.5 Add `ProductSummary` schema: `{ id: UUID, name: string, price: number, imageUrl: string|null, shortDescription: string, category: CategoryDto }`
- [x] 1.6 Add `ProductDetailResponse` schema: extends ProductSummary with `description`, `images: ProductImageDto[]`, `fabrication: FabricationDto|null`, `ethics: EthicsDto|null`
- [x] 1.7 Add `ProductImageDto` schema: `{ imageId: UUID, imageUrl: string(uri), alt: string|null, displayOrder: integer }`
- [x] 1.8 Run `make generate` and verify generated Java interfaces and model classes compile

## 2. Repository Layer

- [x] 2.1 Create `ProductSummaryProjection` interface in `repository/projection/` with fields matching the native query output: `id`, `name`, `price`, `shortDescription`, `categorySlug`, `categoryName`, `imageObjectKey`
- [x] 2.2 Add `findProductSummaries` native query method to `ProductRepository` using the keyset pagination query with LEFT JOIN LATERAL for primary image
- [x] 2.3 Add `findByIdAndIsActiveTrue` method to `ProductRepository` for detail endpoint (respects soft delete)
- [x] 2.4 Add `findByProductIdOrderByDisplayOrderAsc` method to `ProductImageRepository` for loading all images in detail endpoint

## 3. Service Layer

- [x] 3.1 Add `listProducts(String cursor, Integer limit, String categorySlug)` method to `ProductService` — decodes cursor, validates limit (cap at 100, default 20), calls repository projection, encodes nextCursor, builds `ProductListResponse`
- [x] 3.2 Add `getProductDetail(UUID id)` method to `ProductService` — fetches product with category, fetches images, builds `ProductDetailResponse`
- [x] 3.3 Add cursor encoding/decoding utility methods (base64 JSON with `createdAt` and `id` fields, handle invalid cursor by throwing exception mapped to 400)
- [x] 3.4 Add image URL construction: prepend `/images/` to stored `objectKey` for public URLs
- [x] 3.5 Update `uploadProductImage` to return `/images/{objectKey}` instead of `/{objectKey}` in `imageUrl`

## 4. Controller Layer

- [x] 4.1 Create `PublicProductController` implementing the generated `ProductsApi` interface with `@RequestMapping("/api/v1")`, no auth required
- [x] 4.2 Implement `listProducts` endpoint handler delegating to service
- [x] 4.3 Implement `getProductDetail` endpoint handler delegating to service, returning 404 for missing/inactive products

## 5. Seed Data

- [x] 5.1 Create `infra/postgres/seed/dev-data.sql` with ~25 realistic products spread across the 5 existing categories (outerwear, tops, bottoms, accessories, footwear), with fabrication and ethics data for some products
- [x] 5.2 Create placeholder images in `infra/minio/seed/images/` (small JPG files, one per product)
- [x] 5.3 Verify `make seed` loads products and images correctly

## 6. Component Tests

- [x] 6.1 Create component test class for `ProductRepository` projection query — verify pagination, category filtering, primary image selection, empty results
- [x] 6.2 Create component test for `ProductService.listProducts` — verify cursor encoding/decoding, limit capping, hasMore detection
- [x] 6.3 Create component test for `ProductService.getProductDetail` — verify full detail with images, 404 for missing, 404 for soft-deleted

## 7. Acceptance Tests — Curl Scripts

- [x] 7.1 Create `acceptance_test/curl_test_scripts/04_list_products.sh` — smoke test: first page, pagination with cursor, category filter, empty result for non-existent category
- [x] 7.2 Create `acceptance_test/curl_test_scripts/05_product_detail.sh` — smoke test: valid product detail, 404 for non-existent product

## 8. Acceptance Tests — Python

- [x] 8.1 Create `acceptance_test/tests/test_product_api.py` with tests: list first page, paginate through all products, filter by category, product detail with images, product detail 404, soft-deleted product 404, invalid cursor returns 400, empty database returns empty list
- [x] 8.2 Verify all acceptance tests pass: `cd acceptance_test && uv run pytest -v`
