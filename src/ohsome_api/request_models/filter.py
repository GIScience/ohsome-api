from ohsome_filter_to_sql import OhsomeFilter
from pydantic import (
    Field,
)

from ohsome_api.config import CONFIG
from ohsome_api.request_models.config import RequestConfigModel


class FilterRequestModel(RequestConfigModel):
    ohsome_filter: OhsomeFilter = Field(
        alias="filter",
        title="Filter",
        description=(
            "Filter for OSM data. "
            "Please refer to the [ohsome filter language documentation]"
            f"({CONFIG.external_docs_url}/reference/filter.html)."
        ),
        json_schema_extra={"example": "geometry:point and natural=tree"},
    )
