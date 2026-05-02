## ADDED Requirements

### Requirement: Admin login page
The system SHALL provide a login page at `/admin` with a dark-themed form capturing username and password. The form SHALL follow the Obsidian Monolith design system (underline-only inputs, Space Grotesk labels, `surface_container_low` backgrounds).

#### Scenario: Successful login
- **WHEN** user submits valid credentials (admin/admin)
- **THEN** credentials are stored in a Svelte auth store persisted to localStorage
- **AND** user is redirected to `/admin/add-product`

#### Scenario: Invalid credentials
- **WHEN** user submits invalid credentials
- **THEN** the form displays an error message "AUTHENTICATION FAILED" in the design system's error color
- **AND** credentials are NOT stored

#### Scenario: Unauthenticated access to admin routes
- **WHEN** user navigates to any `/admin/*` route without stored credentials
- **THEN** user is redirected to `/admin/login`

### Requirement: Admin auth store
The system SHALL provide a Svelte store at `src/lib/stores/auth.ts` that holds `{username, password}` and persists to localStorage under key `dw_admin_auth`.

#### Scenario: Credentials persisted across sessions
- **WHEN** user logs in and closes the browser
- **THEN** reopening the browser and navigating to `/admin` restores the session from localStorage

#### Scenario: Logout clears credentials
- **WHEN** user clicks logout action
- **THEN** localStorage key is removed and user is redirected to `/admin/login`

### Requirement: Admin API client configuration
All admin API calls SHALL attach HTTP Basic auth credentials from the auth store to the generated TypeScript API client's `username`/`password` configuration.

#### Scenario: API call includes auth header
- **WHEN** frontend calls any `POST/PUT/DELETE /api/v1/admin/*` endpoint
- **THEN** the request includes an `Authorization: Basic <encoded>` header with stored credentials
