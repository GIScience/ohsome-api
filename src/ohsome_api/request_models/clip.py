from pydantic import Field

from ohsome_api.request_models.config import RequestConfigModel


class ClipRequestModel(RequestConfigModel):
    clip: bool = Field(
        default=False,
        description=(
            "If true, length and area calculations use the clipped feature geometries. "
            "Clipping can be computationally expensive for large AOIs, "
            "depending on your ohsome filter, and is usually unnecessary."
        ),
    )
