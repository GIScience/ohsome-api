import logging
import logging.config
import os

import yaml

from ohsome_api.config import CONFIG

with open(CONFIG.log_config) as f:
    config = yaml.safe_load(f)

logging.config.dictConfig(config)
log_level = os.getenv("OHSOME_API_LOG_LEVEL", None)
if log_level is not None:
    log_level = log_level.upper()
    logging.getLogger().setLevel(log_level)
    logging.getLogger("ohsome-api").setLevel(log_level)
    logging.getLogger("uvicorn").setLevel(log_level)
