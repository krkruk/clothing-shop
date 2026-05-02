## MODIFIED Requirements

### Requirement: Valuation and taxonomy dropdowns
The form SHALL include two required price fields in a 2-column sub-grid: "VALUATION (PLN)" and "VALUATION (EUR)". Both SHALL be underline-only inputs with labels in Space Grotesk `text-[10px]` uppercase tracking-[0.3em]. The currency dropdown SHALL be removed. The category dropdown (fetched from `GET /api/v1/categories`) SHALL remain in the same sub-grid row, making it a 3-field layout (PLN price, EUR price, category).

#### Scenario: Categories loaded from API
- **WHEN** the registration form loads
- **THEN** the category dropdown is populated from `GET /api/v1/categories` response

#### Scenario: Both price fields required
- **WHEN** admin submits the form with PLN price filled but EUR price empty
- **THEN** the form displays a validation error on the EUR price field

#### Scenario: Both prices submitted
- **WHEN** admin fills PLN price "399.00" and EUR price "89.00" and submits
- **THEN** the create request includes a `prices` array with both currency/price pairs

## REMOVED Requirements

### Requirement: Currency defaults to PLN
**Reason**: Replaced by two explicit price fields. The single currency dropdown is no longer needed.
**Migration**: The PLN and EUR price fields replace the former currency dropdown + single price field.
