import pytest
from pydantic import ValidationError

from ohsome_api.request_models.aoi import (
    AoiQueryModel,
    AoiRequestModel,
)


@pytest.fixture
def aoi_invalid_topology_wkt() -> str:
    return "POLYGON((8.674585 49.418922,8.676354 49.417888,8.674585 49.417888,8.676354 49.418922,8.674585 49.418922))"  # noqa


@pytest.fixture
def aoi_invalid_topology_geojson() -> dict:
    return {
        "type": "Polygon",
        "coordinates": [
            [
                [8.674585, 49.418922],
                [8.676354, 49.417888],
                [8.674585, 49.417888],
                [8.676354, 49.418922],
                [8.674585, 49.418922],
            ]
        ],
    }


def test_heigit(aoi_heigit: dict, aoi_wkt_heigit: str):
    assert AoiRequestModel(aoi=aoi_heigit).aoi_wkt == aoi_wkt_heigit


def test_audimax(aoi_audimax: dict, aoi_wkt_audimax: str):
    assert AoiRequestModel(aoi=aoi_audimax).aoi_wkt == aoi_wkt_audimax


@pytest.mark.parametrize("aoi", ("", [], {}, None))
def test_invalid_empty(aoi: str | list | dict | None):
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=aoi)


def test_bbox_invalid():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=(20, 10, 5, 20))


def test_bbox_out_of_bounds_positive_x():
    with pytest.raises(
        ValidationError,
        match="x coordinate need to be between -360 and 360",
    ):
        AoiRequestModel(aoi=(-180, 10, 360, 20))


def test_bbox_out_of_bounds_negative_x():
    with pytest.raises(
        ValidationError,
        match="x coordinate need to be between -360 and 360",
    ):
        AoiRequestModel(aoi=(-360, 10, 5, 20))


def test_bbox_out_of_bounds_positive_y():
    with pytest.raises(
        ValidationError,
        match="y coordinate need to be between -90 and 90",
    ):
        AoiRequestModel(aoi=(-10, -10, 10, 91))


def test_bbox_out_of_bounds_negative_y():
    with pytest.raises(
        ValidationError,
        match="y coordinate need to be between -90 and 90",
    ):
        AoiRequestModel(aoi=(-10, -91, 10, 10))


def test_bbox_crossing_antimeridian_positive():
    wkt = AoiRequestModel(aoi=(170, 10, 190, 20)).aoi_wkt
    assert (
        wkt
        == "MULTIPOLYGON (((170.0 10.0, 180.0 10.0, 180.0 20.0, 170.0 20.0, 170.0 10.0)), ((-180.0 10.0, -170.0 10.0, -170.0 20.0, -180.0 20.0, -180.0 10.0)))"  # noqa: E501
    )


def test_bbox_crossing_antimeridian_negative():
    wkt = AoiRequestModel(aoi=(-190, 10, -170, 20)).aoi_wkt
    assert (
        wkt
        == "MULTIPOLYGON (((170.0 10.0, 180.0 10.0, 180.0 20.0, 170.0 20.0, 170.0 10.0)), ((-180.0 10.0, -170.0 10.0, -170.0 20.0, -180.0 20.0, -180.0 10.0)))"  # noqa: E501
    )


def test_bbox_coursing_antimeridian_fullworld():
    wkt = AoiRequestModel(aoi=(-359, 10, 359, 20)).aoi_wkt
    assert (
        wkt
        == "POLYGON ((-180.0 10.0, 180.0 10.0, 180.0 20.0, -180.0 20.0, -180.0 10.0))"
    )


def test_bbox_str_invalid():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi="[20, 10, 5, 20]")


def test_wkt_invalid():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi="LINE (20 10, 5 20)")


def test_wkt_valid_invalid_type():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi="LINESTRING (20 10, 5 20)")


@pytest.mark.parametrize(
    "aoi", [aoi_invalid_topology_wkt, aoi_invalid_topology_geojson]
)
def test_invalid_topology(aoi: str | dict):
    with pytest.raises(ValueError):
        AoiRequestModel(aoi=aoi)


def test_query(aoi_wkt_audimax: str):
    aoi = AoiQueryModel(aoi="8.670919,49.416393,8.673839,49.417686")
    assert aoi.aoi_wkt == aoi_wkt_audimax


@pytest.mark.parametrize(
    "aoi",
    (
        "8.670919,49.416393,8.673839,49.417686,8.570917",  # too many
        "8.670919,49.416393,8.673839",  # too few
        "8.670919,49.416393,8.673839,foo",  # no float
    ),
)
def test_query_invalid(aoi: str):
    with pytest.raises(ValueError):
        AoiQueryModel(aoi=aoi)
