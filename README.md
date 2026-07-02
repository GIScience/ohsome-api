[![Build Status](https://jenkins.heigit.org/buildStatus/icon?job=ohsome-api/main)](https://jenkins.heigit.org/job/ohsome-api/job/main/)

# ohsome API

![](docs/logo.svg)

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

#### Logging

Logging is configured in [`src/ohsome_api/log_config.yaml`](src/ohsome_api/log_config).
It is possible to provide a custom log config YAML file which follows the [logging config dictschema](https://docs.python.org/3/library/logging.config.html#logging-config-dictschema) of Python by setting the environment variable `OHSOME_API_LOG_CONFIG`.

The log level can be set independently of the log config by setting the environment variable `OHSOME_API_LOG_LEVEL` to one of the [log levels](https://docs.python.org/3/library/logging.html#levels) of Python:

```sh
export OHSOME_API_LOG_LEVEL=DEBUG
```


### Run Development Server

```sh
uv run fastapi dev
```
