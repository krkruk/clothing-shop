## MODIFIED Requirements

### Requirement: Currency conversion on currency switch
When the user switches currency, the cart store SHALL fetch updated prices for all cart items from `GET /api/v1/products/{id}` (with the new `x-currency-code` header) and update each item's price to the new currency. During conversion, a loading state SHALL be tracked.

#### Scenario: Cart converts from PLN to EUR
- **WHEN** the user switches currency from PLN to EUR with 2 items in the cart
- **THEN** the cart fetches EUR prices for both items from the API
- **AND** updates each item's price and currency to EUR values
- **AND** the cart total reflects the new EUR prices

#### Scenario: Cart converts with empty cart
- **WHEN** the user switches currency with no items in the cart
- **THEN** no API calls are made and the cart remains empty

#### Scenario: Price fetch failure during conversion
- **WHEN** a price fetch fails during currency conversion
- **THEN** the cart retains the old currency prices for that item
- **AND** an error state is tracked

### Requirement: Currency field on cart items
Each cart item SHALL include a `currency` field (3-char string) tracking which currency the item's price is in. All items in the cart SHALL always be in the same currency.

#### Scenario: New item uses current currency
- **WHEN** user acquires an artifact while EUR is selected
- **THEN** the cart item's currency is "EUR" and price is the EUR price

#### Scenario: All items share same currency
- **WHEN** user adds an item in PLN, then adds another item (still PLN)
- **THEN** both items have currency "PLN"
