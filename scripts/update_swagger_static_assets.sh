#!/usr/bin/bash
# https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/#download-the-files

set -e

root=$(git rev-parse --show-toplevel)
pushd "$root/static"

wget https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js -O swagger-ui-bundle.js
wget https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css -O swagger-ui.css

popd
