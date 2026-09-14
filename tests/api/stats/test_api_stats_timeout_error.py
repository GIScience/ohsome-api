from fastapi.testclient import TestClient
from pytest import MonkeyPatch
from starlette.status import HTTP_504_GATEWAY_TIMEOUT

from ohsome_api.config import Config
from ohsome_api.db.db import db


async def test_stats_features_query_timeout(
    client: TestClient,
    aoi_heigit: dict,
    monkeypatch: MonkeyPatch,
):
    monkeypatch.setenv("OHSOME_API__OHSOMEDB__TIMEOUT_STATS", "0.01")
    monkeypatch.setattr("ohsome_api.db.db.CONFIG", Config())

    client.portal.call(db.disconnect)  # type: ignore
    client.portal.call(db.connect)  # type: ignore

    response = client.post(
        "/stats/features/area.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "earliest",
                "end": "latest",
                "interval": "P1M",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_504_GATEWAY_TIMEOUT
    assert response.headers["content-type"] == "application/json"
    detail = response.json()["detail"]
    assert detail == [
        {
            "type": "QueryTimeoutError",
            "msg": (
                "Query timeout limit has been exceeded. "
                "For statistics endpoints the timeout limit is 0.01. "
                "For extraction endpoints the timeout limit is 180."
            ),
        }
    ]
