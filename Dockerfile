FROM python:3.14-slim AS builder
COPY --from=ghcr.io/astral-sh/uv:0.10.9 /uv /uvx /bin/

# TODO: remove after fetching ohsomedb-schema from pypi
RUN apt update \
    && apt install -y --no-upgrade --no-install-recommends \
      git

WORKDIR /app

# Sync deps only
RUN --mount=type=cache,target=/root/.cache/uv \
    --mount=type=bind,source=uv.lock,target=uv.lock \
    --mount=type=bind,source=pyproject.toml,target=pyproject.toml \
    uv sync --locked --no-install-project --no-editable --no-dev

# Copy the project into the image
COPY README.md .
COPY pyproject.toml .
COPY uv.lock .
COPY ./src ./src

# Sync the project
RUN --mount=type=cache,target=/root/.cache/uv \
    uv sync --locked --no-editable --no-dev


FROM python:3.14-slim AS app

ENV VIRTUAL_ENV=/app/.venv \
    PATH="/app/.venv/bin:$PATH"

WORKDIR /app

COPY --from=builder /app/.venv /app/.venv
COPY static static

ENTRYPOINT ["fastapi", "run", "--entrypoint", "ohsome_api.api:app", "--port", "80", "--forwarded-allow-ips", "'*'", "--root-path", "/ohsome-api/v2"]
