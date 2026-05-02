## ADDED Requirements

### Requirement: Product registration page layout
The system SHALL provide a product registration form at `/admin/add-product` with a two-column grid layout (`lg:grid-cols-2`) within a `max-w-6xl` container. The page SHALL follow Section 6.1 of prd-ui-ux.md precisely: sidebar navigation, page header "PRODUCT REGISTRATION" with accent bar, and form fields organized into Identification Cluster (left) and Rich Text & Specifications Cluster (right).

#### Scenario: Page renders with all form sections
- **WHEN** admin navigates to `/admin/add-product`
- **THEN** the page displays all form sections: Product Identity, Precise Abstract, Valuation + Taxonomy, Status checkbox, The Narrative (rich text), Materiality (rich text), Preservation (rich text), Provenance, Societal Resonance, Visual Documentation drop area, and ADD PRODUCT submit button

### Requirement: Product identity fields
The form SHALL include a name field (underline-only input, Space Grotesk, uppercase, tracking-tighter) and short description field (underline-only input, Manrope). Both SHALL have labels in Space Grotesk `text-[10px]` uppercase tracking-[0.3em].

#### Scenario: Name field captures product identity
- **WHEN** admin types a product name
- **THEN** the value is stored in form state with placeholder "NAME"

### Requirement: Valuation and taxonomy dropdowns
The form SHALL include a currency dropdown (PLN, EUR, USD — default PLN) and a category dropdown (fetched from `GET /api/v1/categories`). These SHALL be in a 2-column sub-grid with underline-only select styling.

#### Scenario: Categories loaded from API
- **WHEN** the registration form loads
- **THEN** the category dropdown is populated from `GET /api/v1/categories` response

#### Scenario: Currency defaults to PLN
- **WHEN** the form is initially rendered
- **THEN** the currency dropdown shows "PLN" selected

### Requirement: Active status checkbox
The form SHALL include a custom checkbox labeled "STATUS: ACTIVE INVENTORY" with `w-5 h-5` size, `border-outline/30` default, and `bg-primary-container` when checked. Default state SHALL be checked.

#### Scenario: New product defaults to active
- **WHEN** the form is submitted with the checkbox in default state
- **THEN** `isActive: true` is sent in the create product request

#### Scenario: Admin unchecks active status
- **WHEN** admin unchecks the checkbox and submits
- **THEN** `isActive: false` is sent in the create product request

### Requirement: Rich text editor for narrative fields
The form SHALL use tiptap rich text editors for three fields: Description ("THE NARRATIVE"), Fabrication content ("MATERIALITY"), and Fabrication care ("PRESERVATION"). Each editor SHALL have a toolbar with bold, italic, and list icons (Material Symbols). Content is stored as HTML strings.

#### Scenario: Rich text produces HTML
- **WHEN** admin formats text with bold and lists in the Narrative editor
- **THEN** the form state stores the content as an HTML string for submission

### Requirement: SKU input field
The form SHALL include an SKU field (underline-only input) for user-provided stock keeping unit identifiers.

#### Scenario: SKU is submitted with product
- **WHEN** admin enters an SKU and submits
- **THEN** the SKU value is included in the create product request

### Requirement: Visual documentation drop area
The form SHALL include a drag-and-drop image area (`h-64`, dashed border, `upload_file` icon) and a 4-column thumbnail preview grid. At least one image SHALL be validated as mandatory before submission.

#### Scenario: User selects images via drag-and-drop
- **WHEN** admin drags image files onto the drop area
- **THEN** thumbnail previews appear in the preview grid with grayscale treatment

#### Scenario: User selects images via file picker
- **WHEN** admin clicks the drop area and selects files from the file picker
- **THEN** thumbnail previews appear in the preview grid

#### Scenario: No images selected on submit
- **WHEN** admin submits the form without any images selected
- **THEN** the form displays a validation error on the image area

### Requirement: Seamless image upload flow
The submit flow SHALL be: (1) submit product data via `POST /api/v1/admin/products`, (2) on success, upload each selected image via `POST /api/v1/admin/products/{id}/image`, (3) show success state.

#### Scenario: Product creation with images
- **WHEN** admin fills form with valid data and 3 images, then clicks ADD PRODUCT
- **THEN** system creates the product via API
- **AND** uploads all 3 images sequentially
- **AND** shows a success confirmation

#### Scenario: Product creation succeeds but image upload fails
- **WHEN** product is created but one image upload fails
- **THEN** system shows a partial success message indicating which images failed
- **AND** the product remains created (not rolled back)

### Requirement: Ethics fields
The form SHALL include Provenance (ethics origin) and Societal Resonance (ethics impact) fields in a full-width 2-column row, separated by a top border. Both SHALL be underline-only inputs.

#### Scenario: Ethics data submitted
- **WHEN** admin fills both ethics fields and submits
- **THEN** origin and impact values are included in the `ethics` object of the create request
