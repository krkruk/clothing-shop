## ADDED Requirements

### Requirement: Currency picker dropdown
The header SHALL include a currency dropdown between the user icon and cart icon. The dropdown SHALL display the currently selected currency (PLN or EUR) as a compact selector. Options are hardcoded: PLN and EUR. The dropdown styling SHALL follow the design system: underline-only, Space Grotesk `text-[10px]` uppercase, dark background, no rounded corners.

#### Scenario: Currency picker defaults to PLN
- **WHEN** the header renders for the first time
- **THEN** the currency picker shows "PLN" selected

#### Scenario: User switches to EUR
- **WHEN** user selects EUR from the currency dropdown
- **THEN** all subsequent API requests include `x-currency-code: EUR` header
- **AND** prices across the page update to EUR values

#### Scenario: Currency selection persisted
- **WHEN** user selects EUR and refreshes the page
- **THEN** the currency picker still shows EUR selected (persisted in localStorage)

### Requirement: User icon dropdown menu
The header user icon SHALL display a dropdown menu on click. The dropdown SHALL follow the design system styling: `surface_container_high` background, `primary_container` 2px top border, fade-in 300ms, no rounded corners.

#### Scenario: Not authenticated — dropdown shows login
- **WHEN** the user is not logged in and clicks the user icon
- **THEN** a dropdown appears with a "LOGIN" option that navigates to `/admin/login`

#### Scenario: Authenticated — dropdown shows admin and logout
- **WHEN** the user is logged in and clicks the user icon
- **THEN** a dropdown appears with "ADMIN PANEL" (navigates to `/admin/add-product`) and "LOG OFF" (calls `auth.logout()` and navigates to `/`)

#### Scenario: Dropdown closes on outside click
- **WHEN** the dropdown is open and the user clicks outside it
- **THEN** the dropdown closes

### Requirement: Currency store
The system SHALL provide a currency store persisted in localStorage (`clothingshop-currency`) with values "PLN" or "EUR". Default is "PLN". The store SHALL be importable from `$lib/stores/currency`.

#### Scenario: Currency store initializes with PLN
- **WHEN** the app loads with no localStorage value
- **THEN** the currency store value is "PLN"

#### Scenario: Currency store reads from localStorage
- **WHEN** the app loads with localStorage value "EUR"
- **THEN** the currency store value is "EUR"

### Requirement: API client currency header injection
The frontend API client SHALL automatically inject the `x-currency-code` header with the current value from the currency store on every API request.

#### Scenario: API request includes currency header
- **WHEN** the currency store value is "EUR" and an API request is made
- **THEN** the request includes header `x-currency-code: EUR`

#### Scenario: API request defaults to PLN header
- **WHEN** no currency is selected and an API request is made
- **THEN** the request includes header `x-currency-code: PLN`
