#!/usr/bin/sh
# https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/#download-the-files

cd "$(git rev-parse --show-toplevel)/static" && \
  wget https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js -O swagger-ui-bundle.js && \
  wget https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css -O swagger-ui.css && \
  wget https://cdn.jsdelivr.net/npm/redoc@2/bundles/redoc.standalone.js -O redoc.standalone.js
