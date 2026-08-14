import json
from typing import Annotated, Any

from geojson_pydantic import MultiPolygon, Polygon
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    computed_field,
    field_validator,
)
from pydantic.alias_generators import to_camel
from shapely.geometry import mapping
from shapely.wkt import loads as load_wkt


class BoundingBoxValidationError(ValueError):
    pass


class RequestConfigModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )


BBox = Annotated[
    tuple[float, float, float, float],
    Field(
        title="Bounding Box (BBOX)",
        description="xmin, ymin, xmax, ymax",
        json_schema_extra={
            "example": (8.68812, 49.40390, 8.72362, 49.41582),
        },
    ),
]


BBoxQuery = Annotated[
    str,
    Field(
        title="Bounding Box (BBOX)",
        description="xmin, ymin, xmax, ymax",
        json_schema_extra={"example": "8.68812,49.40390,8.72362,49.41582"},
    ),
]

WKT = Annotated[
    str,
    Field(
        title="Well Known Text (WKT)",
        description="As geometry type only POLYGON or MULTIPOLYGON is allowed.",
        json_schema_extra={
            "example": "POLYGON ((8.72362 49.41582,8.68812 49.41582,8.68812 49.4039,8.72362 49.4039,8.72362 49.41582))"  # noqa: E501
        },
    ),
]

GeoJSONGeometry = Annotated[
    Polygon | MultiPolygon,
    Field(
        title="GeoJSON Geometry",
        description="As geometry type only Polygon or MultiPolygon is allowed.",
        json_schema_extra={
            "example": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [8.72362, 49.41582],
                        [8.68812, 49.41582],
                        [8.68812, 49.40390],
                        [8.72362, 49.40390],
                        [8.72362, 49.41582],
                    ]
                ],
            },
        },
    ),
]


class AoiRequestModel(RequestConfigModel):
    aoi: BBox | GeoJSONGeometry | WKT = Field(
        description=(
            "Area of interest as a GeoJSON Geometry, Bounding Box or Well Known Text "
            "(WGS84, EPSG:4326)."
        ),
    )

    @computed_field()
    @property
    def aoi_wkt(self) -> str:
        return self.aoi.wkt  # type: ignore

    @classmethod
    def bbox(cls, value: BBox) -> Polygon | MultiPolygon:  # noqa: C901
        xmin, ymin, xmax, ymax = value
        if xmin >= xmax or ymin >= ymax:
            raise BoundingBoxValidationError(
                "min coordinate need to be smaller than max coordinate."
            )
        if ymin < -90 or ymax > 90:
            raise BoundingBoxValidationError(
                "y coordinate need to be between -90 and 90."
            )
        if xmin <= -360 or xmax >= 360:
            raise BoundingBoxValidationError(
                "x coordinate need to be between -360 and 360."
            )
        if (xmax - xmin) >= 360:
            xmin, xmax = -180.0, 180.0
        if xmin >= -180 and xmax <= 180:
            return Polygon.from_bounds(xmin, ymin, xmax, ymax)

        polys = []
        if xmin < -180:
            polys.append(Polygon.from_bounds(xmin + 360, ymin, 180, ymax).coordinates)
            polys.append(Polygon.from_bounds(-180, ymin, xmax, ymax).coordinates)
        if xmax > 180:
            polys.append(Polygon.from_bounds(xmin, ymin, 180, ymax).coordinates)
            polys.append(Polygon.from_bounds(-180, ymin, xmax - 360, ymax).coordinates)

        return MultiPolygon(coordinates=polys, type="MultiPolygon")

    @field_validator("aoi", mode="before")
    @classmethod
    def parse_string_input(cls, value: Any) -> Any:  # noqa: ANN401
        if not isinstance(value, str):
            return value

        value = value.strip()
        # 1. Handle GeoJSON String (from GET parameters)
        if (value.startswith("{") and value.endswith("}")) or (
            value.startswith("[") and value.endswith("]")
        ):
            try:
                return json.loads(value)
            except Exception as e:
                raise ValueError(f"Invalid JSON string: {e}") from e

        return cls.try_transform_wkt_str_to_geojson(value)

    @classmethod
    def try_transform_wkt_str_to_geojson(cls, value: str) -> dict:
        try:
            return mapping(load_wkt(value))
        except Exception as e:
            raise ValueError(f"Input string is invalid WKT: {e}") from e

    @field_validator("aoi")
    @classmethod
    def transform_bbox_to_geojson(
        cls,
        value: Polygon | MultiPolygon | BBox,
    ) -> Polygon | MultiPolygon:
        if isinstance(value, Polygon | MultiPolygon):
            return value
        return cls.bbox(value)


class AoiQueryModel(RequestConfigModel):
    aoi: BBoxQuery

    @field_validator("aoi", mode="before")
    @classmethod
    def validate_aoi(cls, value: str) -> str:
        try:
            bbox = [float(v) for v in value.split(",")]
        except ValueError as error:
            raise BoundingBoxValidationError("Invalid bounding box.") from error
        AoiRequestModel.bbox(bbox)  # ty:ignore[invalid-argument-type]
        return value

    @computed_field
    @property
    def aoi_wkt(self) -> str:
        bbox = self.aoi.split(",")
        return AoiRequestModel(aoi=bbox).aoi_wkt
