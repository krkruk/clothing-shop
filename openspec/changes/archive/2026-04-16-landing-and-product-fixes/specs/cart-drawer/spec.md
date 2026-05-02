## MODIFIED Requirements

### Requirement: Currency display in cart
All prices in the cart drawer SHALL display using the currency code from the cart items (not hardcoded dollar sign). The format SHALL be "{currency} {price}" matching the storewide currency display convention.

#### Scenario: Cart shows EUR prices
- **WHEN** the cart items have currency "EUR" and a price of "89.00"
- **THEN** the cart drawer displays "EUR 89.00" for that item

#### Scenario: Cart total in current currency
- **WHEN** the cart has 2 items in PLN (399.00 + 199.00)
- **THEN** the TOTAL VALUE displays "PLN 598.00"

### Requirement: Loading state during currency conversion
When the user switches currency, the cart drawer SHALL show a loading indicator while prices are being refetched. The cart SHALL remain interactive (items visible) but prices SHALL show a loading state.

#### Scenario: Loading indicator during conversion
- **WHEN** currency switch triggers price refetch
- **THEN** a loading indicator appears over the price areas
- **AND** item list remains visible with old prices until new prices arrive
