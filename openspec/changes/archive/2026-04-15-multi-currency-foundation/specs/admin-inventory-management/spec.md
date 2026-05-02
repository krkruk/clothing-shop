## MODIFIED Requirements

### Requirement: Update action opens modal
Clicking the Update button SHALL open a modal overlay with the same form structure as the Product Registration form, pre-populated with the selected product's current data including existing images. The modal SHALL display both PLN and EUR price fields (matching the dual-price registration form), both pre-filled with the product's current prices.

#### Scenario: Update modal opens with product data
- **WHEN** admin clicks Update on a product row
- **THEN** a modal opens showing the product form pre-filled with current name, description, PLN price, EUR price, category, fabrication, ethics, images, and active status

#### Scenario: Update saves changes
- **WHEN** admin modifies fields (including one or both prices) and clicks save
- **THEN** a `PUT /api/v1/admin/products/{id}` request is sent with updated fields including the `prices` array
- **AND** on success, the modal closes and the table refreshes

#### Scenario: Update with new images
- **WHEN** admin adds new images in the update modal and saves
- **THEN** new images are uploaded via `POST /api/v1/admin/products/{id}/image`
- **AND** existing images are preserved
