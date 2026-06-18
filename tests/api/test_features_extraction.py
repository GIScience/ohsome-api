import io
import json
from datetime import datetime

import pyarrow as pa
import pyarrow.parquet as pq
from fastapi.testclient import TestClient
from pyarrow import parquet
from shapely import from_wkb, to_wkt
from starlette.status import (
    HTTP_200_OK,
)


def test_features_extraction_post(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/features/extraction.parquet",
        json={"filter": "id:node/1702635807", "aoi": aoi_geojson_heigit},
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    assert (
        response.headers["content-disposition"]
        == 'attachment; filename="extractions.parquet"'
    )

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)
    assert table.num_rows == 1

    # https://www.openstreetmap.org/api/0.6/way/274497164
    assert table["osm_id"][0].as_py() == 1702635807
    assert table["osm_type"][0].as_py() == "node"
    assert table["osm_version"][0].as_py() == 6

    assert table["osm_user_name"][0].as_py() == "ezelo"
    assert table["osm_changeset_id"][0].as_py() == 74974721
    assert table["osm_tags"][0].as_py() == [
        ("name", "Dürer trifft Einstein auf Reisen"),
        ("tourism", "artwork"),
        ("material", "bronze"),
        ("start_date", "2011"),
        ("wheelchair", "yes"),
        ("artist_name", "Sabrina Hohmann"),
        ("artwork_type", "sculpture"),
    ]

    # resets to 0 if major version has been bumped up
    # assert table["minor_version"][0].as_py() == 6
    assert table["clipped"][0].as_py() is True

    last_edit_expected = datetime.fromisoformat("2019-09-26 17:18:15.000000Z")
    assert table["last_edit"][0].as_py() == last_edit_expected

    geom = table["geom"][0].as_py()
    geom = from_wkb(geom)
    assert to_wkt(geom) == "POINT (8.672892 49.416696)"

    metadata = parquet.read_metadata(response_file).metadata
    metadata_geo = metadata[b"geo"].decode("utf-8")
    metadata_geo = json.loads(metadata_geo)

    assert metadata_geo["columns"]["geom"]["bbox"] == [
        8.6728921,
        49.4166963,
        8.6728921,
        49.4166963,
    ]
    assert "crs" in metadata_geo["columns"]["geom"]

    # metadata_api = metadata[b"api"].decode("utf-8")
    # metadata_api = json.loads(metadata_api)
    # assert metadata_api["version"] == VERSION
    # assert metadata_api["attribution"]["url"] == "https://ohsome.org/copyrights"
    # assert metadata_api["attribution"]["text"] == "© OpenStreetMap contributors"


def test_features_extraction_deleted_features(
    client: TestClient, aoi_geojson_heigit: dict
):
    response = client.post(
        "/features/extraction.parquet",
        json={"filter": "id:way/394983845", "aoi": aoi_geojson_heigit},
    )
    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    table = parquet.read_table(io.BytesIO(response.content))
    assert table.num_rows == 0


def test_contributions_extract_stream(client: TestClient, aoi_geojson_heigit: dict):
    with client.stream(
        "POST",
        "/features/extraction.parquet",
        json={"filter": "id:way/274497164", "aoi": aoi_geojson_heigit},
    ) as response:
        assert response.status_code == HTTP_200_OK
        assert response.headers["content-type"] == "application/vnd.apache.parquet"
        chunks = [chunk for chunk in response.iter_bytes()]

    table = pq.read_table(io.BytesIO(b"".join(chunks)))
    assert table.num_rows == 1
    assert table.schema.field("tags").type == pa.map_(pa.string(), pa.string())
    assert table["osm_type"][0].as_py() == "way"
    assert table["osm_id"][0].as_py() == 274497164
