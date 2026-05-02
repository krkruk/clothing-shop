## ADDED Requirements

### Requirement: SvelteKit project structure
The frontend SHALL be a SvelteKit project using `adapter-static` with SPA fallback. The project SHALL live under `frontend/` directory. Generated code (`src/api/generated/`) SHALL be gitignored and have a placeholder `.gitkeep`.

#### Scenario: SvelteKit project runs
- **WHEN** `npm run dev` is executed in the `frontend/` directory
- **THEN** the dev server SHALL start and serve the application on localhost:5173

### Requirement: TypeScript configuration
The project SHALL use TypeScript with strict mode enabled. Svelte files SHALL support TypeScript in `<script lang="ts">` blocks.

#### Scenario: TypeScript compilation
- **WHEN** `svelte-check` is run
- **THEN** no type errors SHALL be reported for project source files

### Requirement: Dockerfile
A multi-stage Dockerfile SHALL exist in `frontend/`. Stage 1: Node.js build (`npm install` + `npm run build`). Stage 2: nginx:alpine serving the static output with SPA fallback. The Dockerfile SHALL produce a working container.

#### Scenario: Docker build succeeds
- **WHEN** `podman build -t clothingshop-frontend .` is run in the `frontend/` directory
- **THEN** the build SHALL complete successfully

#### Scenario: Container serves the app
- **WHEN** the container is started and a request is made to port 80
- **THEN** the landing page HTML SHALL be returned

### Requirement: npm scripts
The project SHALL define the following npm scripts: `dev` (Vite dev server), `build` (production build), `preview` (preview production build), `test` (Vitest run), `test:watch` (Vitest watch mode), `check` (svelte-check).

#### Scenario: npm scripts functional
- **WHEN** `npm run dev` / `npm run build` / `npm run test` are executed
- **THEN** each script SHALL complete without error

### Requirement: README documentation
A `README.md` SHALL exist in `frontend/` with sections: project overview, prerequisites (Node.js version), setup (install deps), development (dev server), testing (unit + component tests), building (production), Docker usage, and project structure overview.

#### Scenario: README covers essential workflows
- **WHEN** a new developer reads `frontend/README.md`
- **THEN** they SHALL find instructions for setup, dev, test, and build

### Requirement: Acceptance test infrastructure
The `acceptance_test/` directory SHALL contain a Python project with pytest + pytest-playwright configured. A `conftest.py` SHALL provide a base URL fixture (default: `http://localhost:5173`). Test files SHALL be in `tests/` subdirectory.

#### Scenario: Acceptance tests run
- **WHEN** `pytest` is executed in `acceptance_test/`
- **THEN** tests SHALL connect to the dev server and execute browser interactions
