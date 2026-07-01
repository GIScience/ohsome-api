from typing import Annotated, Literal

from fastapi import APIRouter, Query
from ohsome_filter_to_sql import OhsomeFilter

router = APIRouter()


@router.get(
    "/filter/validation",
    summary="Validate filter syntax.",
    tags=["Metadata"],
)
async def validate_filter(
    filter_: Annotated[OhsomeFilter, Query(alias="filter")],
) -> dict[Literal["filter"], OhsomeFilter]:
    # TODO add ohsome_filter_to_sql error handling
    return {"filter": filter_}
