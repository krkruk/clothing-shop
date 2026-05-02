## ADDED Requirements

### Requirement: Cart store initialization
The application SHALL initialize a cart store as a Svelte writable store. On first load, the store SHALL read any existing cart data from localStorage. If no data exists, the store SHALL initialize with an empty array.

#### Scenario: Fresh session with no stored cart
- **WHEN** the application loads and localStorage has no cart data
- **THEN** the cart store SHALL contain an empty items array

#### Scenario: Returning session with stored cart
- **WHEN** the application loads and localStorage has valid cart data
- **THEN** the cart store SHALL initialize with the stored items

### Requirement: Add item to cart
The store SHALL provide an `addItem` function that accepts a product and personalization data (silhouette, waistCm, hipsCm, heightCm). Each added item SHALL receive a client-generated unique ID. The item SHALL be stored with: product ID, product name, product price, personalization data, quantity of 1, and a thumbnail reference.

#### Scenario: Add a product with personalization
- **WHEN** `addItem` is called with a product and personalization
- **THEN** a new cart item SHALL be created with a unique ID
- **THEN** the item SHALL include the product price, name, and personalization
- **THEN** the items array SHALL contain the new item

#### Scenario: Add same product with different personalization
- **WHEN** `addItem` is called twice with the same product but different personalization
- **THEN** two separate cart items SHALL exist in the store (each is unique)

### Requirement: Remove item from cart
The store SHALL provide a `removeItem` function that accepts a cart item ID and removes that item from the array. Removing a non-existent item SHALL be a no-op.

#### Scenario: Remove existing item
- **WHEN** `removeItem` is called with a valid cart item ID
- **THEN** that item SHALL be removed from the items array

#### Scenario: Remove non-existent item
- **WHEN** `removeItem` is called with an ID not in the cart
- **THEN** the items array SHALL remain unchanged

### Requirement: Update item quantity
The store SHALL provide an `updateQuantity` function that accepts a cart item ID and new quantity. If quantity is set to 0 or below, the item SHALL be removed from the cart.

#### Scenario: Update quantity to positive value
- **WHEN** `updateQuantity` is called with quantity 3
- **THEN** the item's quantity SHALL be 3

#### Scenario: Update quantity to zero removes item
- **WHEN** `updateQuantity` is called with quantity 0
- **THEN** the item SHALL be removed from the cart

### Requirement: Derived cart total
The store SHALL expose a derived `cartTotal` that computes the sum of (item price * item quantity) for all items. The total SHALL update reactively when items change.

#### Scenario: Total reflects all items
- **WHEN** the cart contains items priced at $100 (qty 2) and $50 (qty 1)
- **THEN** `cartTotal` SHALL be 250

#### Scenario: Total updates on item removal
- **WHEN** an item is removed from the cart
- **THEN** `cartTotal` SHALL immediately reflect the updated sum

### Requirement: Derived cart count
The store SHALL expose a derived `cartCount` that computes the total number of items (sum of quantities). This SHALL drive the header cart badge.

#### Scenario: Cart count reflects item quantities
- **WHEN** the cart contains 2 items with quantities 1 and 3
- **THEN** `cartCount` SHALL be 4

### Requirement: localStorage persistence
Every mutation to the cart store (add, remove, update quantity) SHALL persist the current items array to localStorage under a configurable key. The persisted data SHALL be valid JSON that can round-trip through serialization.

#### Scenario: Add triggers localStorage write
- **WHEN** `addItem` is called
- **THEN** localStorage SHALL be updated with the current items array

#### Scenario: Data survives serialization
- **WHEN** the cart is persisted and then loaded from localStorage
- **THEN** all item fields (product ID, name, price, personalization) SHALL be intact
