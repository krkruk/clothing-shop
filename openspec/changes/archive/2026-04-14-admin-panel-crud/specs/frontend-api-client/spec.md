## MODIFIED Requirements

### Requirement: OpenAPI TypeScript client
Generate TypeScript client from `openapi/spec.yaml` to `frontend/src/api/generated/` (gitignored). The generated client SHALL include all new admin endpoints: `PUT /admin/products/{id}`, `DELETE /admin/products/{id}`, `GET /admin/products`, `DELETE /admin/products/{id}/images/{imageId}`, and `GET /categories`.

#### Scenario: Generated client includes all admin CRUD methods
- **WHEN** `make generate` is run after OpenAPI spec update
- **THEN** `AdminProductsApi.ts` includes methods: `createProduct`, `updateProduct`, `deleteProduct`, `listAdminProducts`, `uploadProductImage`, `deleteProductImage`
- **AND** `ProductsApi.ts` includes method: `listCategories`

### Requirement: Typed product list fetch
Generated function for `GET /api/v1/products` with typed parameters. The generated types SHALL include the new fields: `currency`, `sku`, `isActive`.

#### Scenario: Product types include new fields
- **WHEN** frontend code imports `ProductResponse` or `ProductSummary`
- **THEN** the types include `currency: string`, `sku: string | null`, `isActive: boolean`
