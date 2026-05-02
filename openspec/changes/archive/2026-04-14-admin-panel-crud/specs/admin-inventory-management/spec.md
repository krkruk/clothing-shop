## ADDED Requirements

### Requirement: Inventory management page layout
The system SHALL provide an inventory management page at `/admin/inventory` with a page header ("INVENTORY MANAGEMENT"), subtitle, total assets count (right-aligned), and a product table below. The layout SHALL follow Section 6.2 of prd-ui-ux.md.

#### Scenario: Page renders with product table
- **WHEN** admin navigates to `/admin/inventory`
- **THEN** the page fetches products from `GET /api/v1/admin/products` and renders them in the table

### Requirement: Inventory table columns
The table SHALL have columns: ASSET (thumbnail, 64x80px, grayscale opacity-80, hover: full color over 500ms), IDENTITY (product name + SKU), CLASSIFICATION (short description), MODIFIED (date + time), DIRECTIVES (Update + Delete buttons, right-aligned).

#### Scenario: Product row displays all columns
- **WHEN** a product is loaded in the table
- **THEN** the row shows thumbnail, product name in Space Grotesk bold, SKU in Manrope text-[10px], short description, modification date/time, and action buttons

### Requirement: Table row hover effect
On hover, rows SHALL transition to `bg-surface_container` and the thumbnail SHALL change from grayscale opacity-80 to full color over 500ms.

#### Scenario: Row hover animation
- **WHEN** admin hovers over a table row
- **THEN** the row background shifts to `surface_container` and the thumbnail animates to full color

### Requirement: Cursor-based pagination
The table SHALL use cursor-based pagination matching the public product catalog pattern. The pagination footer SHALL display "DISPLAYING X-Y OF Z UNITS" (left) and page number buttons + chevron navigation (right).

#### Scenario: Navigate to next page
- **WHEN** admin clicks the chevron_right button
- **THEN** the next page of products is fetched using the cursor from the previous response

#### Scenario: Pagination info display
- **WHEN** products are loaded
- **THEN** pagination footer shows the range and total count of products

### Requirement: Update action opens modal
Clicking the Update button SHALL open a modal overlay with the same form structure as the Product Registration form (Section 6.1), pre-populated with the selected product's current data including existing images.

#### Scenario: Update modal opens with product data
- **WHEN** admin clicks Update on a product row
- **THEN** a modal opens showing the product form pre-filled with current name, description, price, currency, category, fabrication, ethics, images, and active status

#### Scenario: Update saves changes
- **WHEN** admin modifies fields and clicks save
- **THEN** a `PUT /api/v1/admin/products/{id}` request is sent with updated fields
- **AND** on success, the modal closes and the table refreshes

#### Scenario: Update with new images
- **WHEN** admin adds new images in the update modal and saves
- **THEN** new images are uploaded via `POST /api/v1/admin/products/{id}/image`
- **AND** existing images are preserved

### Requirement: Image management in update modal
The update modal SHALL allow adding new images, removing existing images (with `DELETE /api/v1/admin/products/{id}/images/{imageId}`), and reordering images (updating `displayOrder`).

#### Scenario: Remove existing image
- **WHEN** admin clicks remove on an existing image in the update modal
- **THEN** the image is marked for deletion
- **AND** on save, `DELETE /api/v1/admin/products/{id}/images/{imageId}` is called

#### Scenario: Reorder images
- **WHEN** admin changes the order of images in the update modal
- **THEN** on save, displayOrder values are updated via API

### Requirement: Delete action with confirmation modal
Clicking the Delete button SHALL open a confirmation modal before performing soft-delete. The modal SHALL follow the dark design system aesthetic.

#### Scenario: Delete confirmation modal appears
- **WHEN** admin clicks Delete on a product row
- **THEN** a confirmation modal appears asking "CONFIRM REMOVAL OF ASSET?" with the product name

#### Scenario: Confirmed deletion soft-deletes product
- **WHEN** admin confirms deletion in the modal
- **THEN** a `DELETE /api/v1/admin/products/{id}` request is sent
- **AND** on success, the product is soft-deleted (isActive = false)
- **AND** the table refreshes

#### Scenario: Cancelled deletion
- **WHEN** admin cancels the deletion modal
- **THEN** the modal closes with no changes

### Requirement: Admin footer fragment
Below the table, the page SHALL display a 3-column grid with system metadata cards: System Status (pulsing primary dot + "SYNCHRONIZED"), Access Level ("SUPER_ADMINISTRATOR"), Data Integrity ("VALIDATED_100%").

#### Scenario: Footer cards render
- **WHEN** the inventory page loads
- **THEN** the 3-column footer cards are visible below the table
