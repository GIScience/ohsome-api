import io

import pyarrow as pa
import pyarrow.parquet as pq
from fastapi.testclient import TestClient
from starlette.status import (
    HTTP_200_OK,
)


def test_contributions_extract(client: TestClient):
    chunks = []
    with client.stream(
        "POST",
        "/features/extraction.parquet",
        json={"filter": "id:way/274497164"},
    ) as response:
        assert response.status_code == HTTP_200_OK
        assert response.headers["content-type"] == "application/vnd.apache.parquet"
        for chunk in response.iter_bytes():
            chunks.append(chunk)

    table = pq.read_table(io.BytesIO(b"".join(chunks)))
    assert table.num_rows == 1
    assert table.schema.field("tags").type == pa.map_(pa.string(), pa.string())
    assert table["osm_type"][0].as_py() == "way"
    assert table["osm_id"][0].as_py() == 274497164
