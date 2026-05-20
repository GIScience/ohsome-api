import pytest
from pydantic import ValidationError

from ohsome_api.request_models import GeoJsonFeatureCollection

# TODO: "id": 0


def test_geojson_smoke_test(aoi_geojson_heigit: dict):
    GeoJsonFeatureCollection(**aoi_geojson_heigit)


def test_geojson_invalid_feature(aoi_geojson_heigit: dict):
    with pytest.raises(ValidationError):
        GeoJsonFeatureCollection(**aoi_geojson_heigit["features"][0])


def test_geojson_none():
    with pytest.raises(ValidationError):
        GeoJsonFeatureCollection(type="FeatureCollection", features=None)  # type: ignore


def test_geojson_missing():
    with pytest.raises(ValidationError):
        GeoJsonFeatureCollection()  # type: ignore


def test_geojson_empty_features_list(aoi_geojson_heigit: dict):
    aoi_geojson_heigit["features"] = []
    with pytest.raises(ValidationError):
        GeoJsonFeatureCollection(**aoi_geojson_heigit)
