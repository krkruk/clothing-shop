import subprocess

import pytest


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):
    return {
        **browser_context_args,
        "viewport": {"width": 1280, "height": 720},
    }


# --- API integration test fixtures ---

@pytest.fixture(scope="session")
def api_base_url():
    return "http://localhost:8080"


@pytest.fixture(scope="session")
def admin_auth():
    return ("admin", "admin")


def _find_container(service_name: str) -> str:
    result = subprocess.run(
        ["podman", "ps", "--format", "{{.Names}}", "--filter", f"name={service_name}"],
        capture_output=True, text=True,
    )
    names = [n.strip() for n in result.stdout.strip().split("\n") if n.strip()]
    if not names:
        pytest.skip(f"No running container found for service '{service_name}'. Start the stack with: make dev")
    return names[0]


@pytest.fixture(scope="session")
def postgres_container():
    return _find_container("postgres")


@pytest.fixture(scope="session")
def minio_container():
    return _find_container("minio")


@pytest.fixture(scope="session")
def category_id(postgres_container):
    result = subprocess.run(
        ["podman", "exec", postgres_container,
         "psql", "-U", "clothingshop", "-d", "clothingshop",
         "-t", "-A", "-c", "SELECT id FROM category WHERE slug='tops' LIMIT 1"],
        capture_output=True, text=True,
    )
    cat_id = result.stdout.strip()
    if not cat_id:
        pytest.skip("No 'tops' category found in database. Ensure Liquibase migrations have run.")
    return cat_id
