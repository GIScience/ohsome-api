[![Build Status](https://jenkins.heigit.org/buildStatus/icon?job=ohsome-api/main)](https://jenkins.heigit.org/job/ohsome-api/job/main/)

# ohsome api

## Development Setup

Requirements:
* [`uv`](https://docs.astral.sh/uv/getting-started/installation/)

We use: 
* `pytest` for unit tests
* `ruff` for static code analysis
* `ty` for static type checks
* `prek` for pre-commit hooks

### Initial Setup

```sh
uv sync
uv run prek install
uv run pytest
```

### Configuration

Copy `.env.sample` to `.env` and change content to provide credentials to an ohsomedb.

```sh
set -a; source .env; set +a
```

### Run Development Server

```sh
export LOG_LEVEL=INFO
uv run fastapi dev
```

## Production Setup

```sh
docker build -t ohsome-api .
docker run ohsome-api -p 8080:80
```
