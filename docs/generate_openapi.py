import json

from ohsome_api.api import app

with open("./source/reference/openapi.json", "w") as f:
    json.dump(app.openapi(), f, indent=2)
