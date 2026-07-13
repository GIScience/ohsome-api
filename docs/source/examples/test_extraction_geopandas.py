from .extraction_geopandas import features_extraction


def test_extraction_geopandas() -> None:
    aoi: dict = {
        "type": "Polygon",
        "coordinates": [
            [
                [8.72362, 49.41582],
                [8.68812, 49.41582],
                [8.68812, 49.4039],
                [8.72362, 49.4039],
                [8.72362, 49.41582],
            ]
        ],
    }
    osm_filter: str = "type:node and natural=tree"
    result = features_extraction(aoi=aoi, osm_filter=osm_filter)
    assert not result.empty
    assert not result["geom"].empty
    assert all(result.is_valid)
