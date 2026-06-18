import io
import json
from datetime import datetime

import pyarrow as pa
import pyarrow.parquet as pq
from fastapi.testclient import TestClient
from pyarrow import parquet
from starlette.status import (
    HTTP_200_OK,
)


def test_features_extraction_post(client: TestClient, aoi_geojson_heigit: dict):
    response = client.post(
        "/features/extraction.parquet",
        json={"filter": "id:way/274497164", "aoi": aoi_geojson_heigit},
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
    assert table["osm_id"][0].as_py() == 274497164
    assert table["osm_type"][0].as_py() == "way"
    assert table["osm_version"][0].as_py() == 5

    # resets to 0 if major version has been bumped up
    # assert table["minor_version"][0].as_py() == 6

    assert table["osm_user_name"][0].as_py() == "Niiepce"
    assert table["osm_changeset_id"][0].as_py() == 153583423
    assert table["osm_tags"][0].as_py() == [
        ("foot", "yes"),
        ("oneway", "no"),
        ("bicycle", "yes"),
        ("highway", "service"),
        ("surface", "paving_stones"),
        ("motor_vehicle", "private"),
    ]

    last_edit_expected = datetime.fromisoformat("2024-07-05 11:04:04.000000Z")
    assert table["last_edit"][0].as_py() == last_edit_expected

    metadata = parquet.read_metadata(response_file).metadata[b"geo"].decode("utf-8")
    metadata = json.loads(metadata)

    assert metadata["columns"]["geom"]["bbox"] == [
        8.6702875,
        49.416011,
        8.6707624,
        49.4160772,
    ]
    # TODO: validate attribution in API metadata

    # Geometry (WGS84, EPSG 4326)
    # Maybe include information if geometry has been clipped


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
