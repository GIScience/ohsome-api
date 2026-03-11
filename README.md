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

```bash
uv sync
uv run prek install
uv run pytest
```
