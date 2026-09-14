from fastapi.testclient import TestClient
from starlette.status import HTTP_400_BAD_REQUEST


async def test_stats_features_time_series_too_large(
    client: TestClient,
    aoi_heigit: dict,
):
    response = client.post(
        "/stats/features/area.json",
        json={
            "filter": "building=* and building!=no and type:way",
            "time": {
                "start": "earliest",
                "end": "latest",
                "interval": "P1D",
            },
            "aoi": aoi_heigit,
        },
    )
    assert response.status_code == HTTP_400_BAD_REQUEST
    assert response.headers["content-type"] == "application/json"
    details = response.json()["detail"]
    assert details == [
        {
            "type": "TimeSeriesTooLargeError",
            "msg": (
                "The provided values for the time parameter "
                "(time bin or time series) "
                "lead to a time series larger than 1000 points/bins."
            ),
        }
    ]
