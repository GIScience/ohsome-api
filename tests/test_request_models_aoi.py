import pytest
from pydantic import ValidationError

from ohsome_api.request_models import AoiRequestModel


def test_geojson_smoke_test(aoi_geojson_heigit: dict):
    AoiRequestModel(aoi=aoi_geojson_heigit)  # type: ignore


def test_geojson_none():
    with pytest.raises(ValidationError):
        AoiRequestModel(aoi=None)  # type: ignore
