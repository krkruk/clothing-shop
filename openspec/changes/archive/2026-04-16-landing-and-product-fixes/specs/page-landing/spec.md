## MODIFIED Requirements

### Requirement: Chessboard product grid layout
The chessboard product grid SHALL be full-width (no `max-w-7xl` constraint). Each product row SHALL span the entire viewport width with the image column (1/3) and content column (2/3) filling the available space. The alternating layout (image left/right) SHALL be preserved.

#### Scenario: Product row spans full viewport
- **WHEN** the landing page renders on a 1920px wide screen
- **THEN** each chessboard row spans the full 1920px width with no horizontal margin

#### Scenario: Alternating layout preserved
- **WHEN** the landing page renders multiple product rows
- **THEN** odd rows show image left (1/3) + content right (2/3), even rows show content left (2/3) + image right (1/3)

### Requirement: Currency display in product grid
Product prices in the chessboard grid SHALL display using the currency code from the API response, not a hardcoded dollar sign. The format SHALL be "{currency} {price}" (e.g., "PLN 399.00" or "EUR 89.00").

#### Scenario: Price displayed in PLN
- **WHEN** the API returns price "399.00" with currency "PLN"
- **THEN** the chessboard row displays "PLN 399.00"

#### Scenario: Price displayed in EUR
- **WHEN** the API returns price "89.00" with currency "EUR"
- **THEN** the chessboard row displays "EUR 89.00"
