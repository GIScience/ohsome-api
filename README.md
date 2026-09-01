[![Build Status](https://jenkins.heigit.org/buildStatus/icon?job=ohsome-api/main)](https://jenkins.heigit.org/job/ohsome-api/job/main/)
[![LICENSE](https://img.shields.io/github/license/GIScience/ohsome-api)](LICENSE)
[![API docs](https://img.shields.io/badge/API-docs-blue.svg)](https://docs.ohsome.org/ohsome-api/stable)
[![status: active](https://github.com/GIScience/badges/raw/master/status/active.svg)](https://github.com/GIScience/badges#active)

# ohsome API

![](docs/source/_static/ohsome-logo.svg)

The ohsome API is a generic web API for in-depth analysis of OpenStreetMap (OSM) data with a focus on its history.
It allows to get aggregated statistics about the evolution of OSM data itself and about the contributors behind the data.
Furthermore, data extraction methods are provided to access the historic development of individual OSM features.

## Migration from ohsome API v1

> [!IMPORTANT]
> This repository is for ohsome API version 2, a complete rewrite of the ohsome API, including a new database backend.
>
> See our [migration guide](https://docs.ohsome.org/ohsome-api/v2-rc/migration_guide.html) for the differences between the versions and how to upgrade to the new version.

## Using the ohsome API

To make your life easier, we already have a running ohsome API instance on our servers, 
where you can send your requests to analyze the history of the OpenStreetMap data.
This instance is publicly accessible under the following URL:

- https://api.heigit.org/ohsome-api/v2-rc (release candidate for v2)

If you need further information, visit these sites:
- [Documentation](https://docs.ohsome.org/ohsome-api/v2-rc)
- [Swagger UI](https://api.heigit.org/ohsome-api/v2-rc/docs)

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

#### Debugging

To log the SQL query, arguments and the execution plan turn on debug logging (see above) and set the environment variable `OHSOME_API_OHSOMEDB_DEBUG` to `True`:

```sh
export OHSOME_API_LOG_LEVEL=DEBUG
export OHSOME_API_OHSOMEDB_DEBUG=True
```


### Run Development Server

```sh
uv run fastapi dev
```

Be aware that this method does not use the production ASGI server and behaves differently in some cases.
