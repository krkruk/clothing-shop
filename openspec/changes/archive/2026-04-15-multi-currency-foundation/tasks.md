## 1. Database Schema

- [x] 1.1 Consolidate all Liquibase migrations into a single `V001__initial_schema.xml`: create `category`, `product` (without price/currency columns), `product_price` (id UUID PK, product_id FK, currency VARCHAR(3), price DECIMAL(10,2), UNIQUE on product_id+currency), and `product_image` tables. Remove all previous migration files.
- [x] 1.2 Update seed data to include both PLN and EUR prices for every seeded product in `product_price`.

## 2. Backend Entity & Repository

- [x] 2.1 Create `ProductPrice` JPA entity in `entity/` with fields: `id` (UUID), `product` (ManyToOne), `currency` (String, 3 chars), `price` (BigDecimal). Add UNIQUE constraint on (product, currency).
- [x] 2.2 Create `ProductPriceRepository` Spring Data JPA interface with methods: `findAllByProductId(UUID productId)`, `findByProductIdAndCurrency(UUID productId, String currency)`, `deleteAllByProductId(UUID productId)`.
- [x] 2.3 Remove `price` and `currency` fields from `Product` entity.

## 3. OpenAPI Spec Update

- [x] 3.1 Add `ProductPriceDto` schema to `openapi/spec.yaml`: `{currency: string(3), price: string(decimal)}`.
- [x] 3.2 Update `CreateProductRequest`: remove `price` and `currency` fields, add `prices` field (array of `ProductPriceDto`, min 2 items, max 2 items).
- [x] 3.3 Update `UpdateProductRequest`: remove `price` and `currency` fields, add `prices` field (array of `ProductPriceDto`, nullable).
- [x] 3.4 Update `ProductSummary`: add `price` (string decimal) and `currency` (string) fields (populated per resolved currency).
- [x] 3.5 Update `ProductDetailResponse`: remove top-level `price` and `currency`, add `price` and `currency` fields (populated per resolved currency), add `prices` array (all currencies for admin reference).
- [x] 3.6 Update `ProductResponse` (admin): remove top-level `price` and `currency`, add `prices` array of `ProductPriceDto`.
- [x] 3.7 Add `x-currency-code` header parameter to `GET /products` and `GET /products/{id}` operations.
- [x] 3.8 Run `make generate` to regenerate backend interfaces/DTOs and frontend API client.

## 4. Backend Service & Mapper Updates

- [x] 4.1 Add currency resolution logic: extract `x-currency-code` from request header, validate against supported currencies (PLN, EUR), default to PLN if absent/invalid. Create a utility method or request-scoped bean.
- [x] 4.2 Update `ProductMapper`: when mapping to `ProductSummary` or `ProductDetailResponse`, resolve price from `ProductPrice` table based on the requested currency. Populate `price` and `currency` fields accordingly.
- [x] 4.3 Update `ProductService.createProduct()`: accept `prices` array, create `ProductPrice` entities for each currency entry (must be exactly PLN + EUR).
- [x] 4.4 Update `ProductService.updateProduct()`: if `prices` is provided in request, delete existing prices and replace with new ones.
- [x] 4.5 Update `PublicProductController`: read `x-currency-code` header from request, pass currency to service/mapper layer.

## 5. Backend Validation

- [x] 5.1 Add validation: `prices` array in create request must contain exactly 2 entries (PLN and EUR). Reject with 422 if missing a currency.
- [x] 5.2 Add validation: each price must be positive. Reject with 422 if negative or zero.
- [x] 5.3 Add validation: currency codes must be exactly "PLN" or "EUR". Reject with 422 for any other value.

## 6. Frontend Admin — Add Product Form

- [x] 6.1 Replace the single price field + currency dropdown with two required price fields: "VALUATION (PLN)" and "VALUATION (EUR)" in the add-product form at `/admin/add-product`.
- [x] 6.2 Update form submission to send `prices` array: `[{currency: "PLN", price: "..."}, {currency: "EUR", price: "..."}]` instead of the old `price` + `currency` fields.
- [x] 6.3 Add client-side validation: both PLN and EUR price fields are required before submission.

## 7. Frontend Admin — Inventory Edit Modal

- [x] 7.1 Update the inventory edit modal to display both PLN and EUR price fields, pre-populated from the product's `prices` array.
- [x] 7.2 Update edit submission to send the `prices` array in the update request.

## 8. Verification

- [x] 8.1 Run `make clean && make infra-local` to verify fresh database starts with consolidated migration.
- [x] 8.2 Run `make seed` to verify seed data loads with dual-currency prices.
- [x] 8.3 Run `make test` to verify backend unit tests pass with new schema.
- [x] 8.4 Run `make test-component` to verify component/integration tests pass.
- [x] 8.5 Manually verify admin add-product form creates products with both PLN and EUR prices.
- [x] 8.6 Manually verify admin edit modal displays and updates both prices.
- [x] 8.7 Manually verify `GET /api/v1/products` with `x-currency-code: EUR` returns EUR prices.
