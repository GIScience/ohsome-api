import logging
import logging.config
import os

import yaml

from ohsome_api.config import CONFIG


class HealthEndpointFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        """Filter out log entries for health requests."""
        if record.args and record.args[2] == "/health":  # noqa: SIM103 # type: ignore
            return False
        return True


with open(CONFIG.log_config) as f:
    config = yaml.safe_load(f)

# Configures ohsome-api and uvicorn loggers.
# Hypercorn loggers are configured via its CLI.
logging.config.dictConfig(config)

log_level = os.getenv("OHSOME_API_LOG_LEVEL", None)
if log_level is not None:
    log_level = log_level.upper()
    logging.getLogger().setLevel(log_level)
    logging.getLogger("ohsome-api").setLevel(log_level)
    logging.getLogger("uvicorn").setLevel(log_level)
    logging.getLogger("uvicorn.access").setLevel(log_level)
    logging.getLogger("hypercorn.error").setLevel(log_level)
    logging.getLogger("hypercorn.access").setLevel(log_level)
