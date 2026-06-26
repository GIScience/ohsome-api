import pytest
from pydantic import ValidationError

from ohsome_api.request_models import BaseParameters


def test_geojson_smoke_test(aoi_geojson_heigit: dict):
    BaseParameters(filter="id:node/1702635807", aoi=aoi_geojson_heigit)  # type: ignore


def test_geojson_none():
    with pytest.raises(ValidationError):
        BaseParameters(filter="id:node/1702635807", aoi=None)  # type: ignore
