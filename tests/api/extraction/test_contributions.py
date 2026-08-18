import io
import json
from datetime import datetime

from fastapi.testclient import TestClient
from pyarrow import Table, parquet
from pyarrow.parquet import FileMetaData
from shapely import from_wkb, to_wkt
from starlette.status import (
    HTTP_200_OK,
)

from ohsome_api.api import VERSION


def test_post(
    client: TestClient,
    aoi_audimax: dict,
):
    json_body = {
        "filter": "id:way/1136431018",
        "time": {
            "start": "2023-01-01",
            "end": "2024-01-01",
        },
        "aoi": aoi_audimax,
    }
    response = client.post("/extraction/contributions.parquet", json=json_body)

    assert response.status_code == HTTP_200_OK
    assert response.headers["content-type"] == "application/vnd.apache.parquet"
    assert (
        response.headers["content-disposition"]
        == 'attachment; filename="contributions.parquet"'
    )

    response_file = io.BytesIO(response.content)
    table = parquet.read_table(response_file)

    validate_way_1136431018(table)

    metadata = parquet.read_metadata(response_file).metadata
    validate_ohsome_api_metadata(metadata)
    validate_geo_metadata_way_1136431018(metadata)


def validate_way_1136431018(table: Table):
    # https://www.openstreetmap.org/way/1136431018/history/2

    assert table.num_rows == 2

    assert table["osm_id"][1].as_py() == 1136431018
    assert table["osm_type"][1].as_py() == "way"
    assert table["osm_version"][1].as_py() == 2
    assert table["osm_user_name"][1].as_py() == "Gigaszi"
    assert table["osm_changeset_id"][1].as_py() == 139350886
    assert set(table["osm_tags"][1].as_py()) == set(
        [
            ("name", "AudimaX"),
            ("area", "yes"),
            ("building", "construction"),
            ("check_date", "2023-08-02"),  # New in comparison to osm_tags_before
            ("addr:city", "Heidelberg"),
            ("addr:postcode", "69120"),
            ("addr:street", "Im Neuenheimer Feld"),
        ]
    )
    assert set(table["osm_tags_before"][1].as_py()) == set(
        [
            ("name", "AudimaX"),
            ("area", "yes"),
            ("building", "construction"),
            ("addr:city", "Heidelberg"),
            ("addr:postcode", "69120"),
            ("addr:street", "Im Neuenheimer Feld"),
        ]
    )

    # Be careful: The order of contributions is not guaranteed to be by time.
    # Works because of strict ID filter.
    assert table["osm_tags_before"][1].as_py() == table["osm_tags"][0].as_py()
    assert table["osm_version"][1].as_py() - 1 == table["osm_version"][0].as_py()

    assert table["minor_version"][1].as_py() == 0

    last_edit_expected = datetime.fromisoformat("2023-08-02T14:30:27Z")
    assert table["edit_timestamp"][1].as_py() == last_edit_expected

    assert table["contribution_type"][1].as_py() == "TAG"
    assert set(table["osm_changeset_tags"][1].as_py()) == set(
        [
            ("comment", "Determine whether building construction is now completed"),
            ("created_by", "StreetComplete 53.3"),
            ("locale", "de-DE"),
            ("source", "survey"),
            ("StreetComplete:quest_type", "MarkCompletedBuildingConstruction"),
        ]
    )

    geom = table["geom"][1].as_py()
    geom = from_wkb(geom)
    assert (
        to_wkt(geom)
        == "POLYGON ((8.671494 49.416778, 8.671472 49.417418, 8.671598 49.417455, 8.672548 49.416975, 8.672459 49.416792, 8.671494 49.416778))"  # noqa: E501
    )


def validate_geo_metadata_way_1136431018(metadata: FileMetaData):
    metadata = metadata[b"geo"].decode("utf-8")
    metadata = json.loads(metadata)
    assert metadata["columns"]["geom"]["bbox"] == [
        8.6714721,
        49.4167781,
        8.6725477,
        49.417455,
    ]
    assert "crs" in metadata["columns"]["geom"]


def validate_ohsome_api_metadata(metadata: FileMetaData):
    metadata = metadata[b"ohsome API"].decode("utf-8")
    metadata = json.loads(metadata)
    assert metadata["version"] == VERSION
    assert metadata["attribution"]["url"] == "https://ohsome.org/copyrights"
    assert metadata["attribution"]["text"] == "© OpenStreetMap contributors"
