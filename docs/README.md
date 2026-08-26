# Documentation

```sh
# Install dependencies
uv sync --group docs

# Run tests for the documentation Python examples
export OHSOME_API_KEY=foo
export OHSOME_API_URL="http://api.heigit.org/ohsome-api/v2-rc"
uv run pytest source/examples/
duckdb -f source/examples/extraction_duckdb.sql

# Build documentation (with live update)
make auto-html

# Build documentation (like prod)
make html
```
