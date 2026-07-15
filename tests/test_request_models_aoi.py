import pytest
from pydantic import ValidationError

from ohsome_api.request_models import AoiRequestModel


def test_smoke_test(aoi_heigit: dict):
    AoiRequestModel(aoi=aoi_heigit)


def test_geojson_none():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=None)


def test_bbox_invalid():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=(20, 10, 5, 20))


def test_bbox_out_of_bounds_positive_x():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=(-180, 10, 360, 20))


def test_bbox_out_of_bounds_negative_x():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=(-360, 10, 5, 20))


def test_bbox_out_of_bounds_positive_y():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=(-10, -10, 10, 91))


def test_bbox_out_of_bounds_negative_y():
    with pytest.raises(ValidationError):
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
