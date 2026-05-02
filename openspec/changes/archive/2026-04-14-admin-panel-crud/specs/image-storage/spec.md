## ADDED Requirements

### Requirement: Delete product image endpoint
The system SHALL provide a `DELETE /api/v1/admin/products/{id}/images/{imageId}` endpoint (HTTP Basic auth) that removes an image record from the database and deletes the corresponding object from MinIO/S3 storage.

#### Scenario: Delete existing image
- **WHEN** admin sends DELETE for a valid product/image combination
- **THEN** the product_image row is deleted from the database
- **AND** the object is removed from MinIO bucket
- **AND** the response is 204 No Content

#### Scenario: Delete image from wrong product
- **WHEN** admin sends DELETE where the imageId does not belong to the specified productId
- **THEN** the response is 404 with ProblemDetail

#### Scenario: Delete image requires authentication
- **WHEN** unauthenticated DELETE request is made
- **THEN** the response is 401

#### Scenario: Delete image for non-existent product
- **WHEN** admin sends DELETE for a productId that doesn't exist
- **THEN** the response is 404 with ProblemDetail

### Requirement: Image display order update
The system SHALL support updating the `display_order` of product images for reordering purposes. This MAY be implemented as part of the product update endpoint or as a dedicated endpoint.

#### Scenario: Reorder images via update
- **WHEN** admin updates a product with reordered image display orders
- **THEN** the display_order values are persisted
- **AND** images appear in the new order on the public product detail page
