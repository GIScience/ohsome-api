import logging
import os

log_level = os.getenv("OHSOME_API_LOG_LEVEL", "INFO").upper()
logging.basicConfig(level=log_level)
