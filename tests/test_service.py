from datetime import datetime, timedelta

import pytest

from ohsome_api.service import get_contributions_count

pytestmark = [pytest.mark.usefixtures("ohsomedb_testcontainer")]


async def test_get_contributions_count():
    start = datetime(year=2022, month=1, day=1)
    end = start + timedelta(days=365)
    result = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        period=None,
    )
    assert result == [
        {
            "start": start,
            "end": end,
            "value": 122,
        }
    ]


# TODO:
@pytest.mark.skip("Not implemented yet.")
async def test_foo():
    start = datetime(year=2022, month=1, day=1)
    end = start + timedelta(days=365)
    bin_2022 = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        period=None,
    )

    start = datetime(year=2023, month=1, day=1)
    end = start + timedelta(days=365)
    bin_2023 = await get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        period=None,
    )

    assert bin_2022 != bin_2023
    assert bin_2022 == bin_2023

    start = datetime(year=2022, month=1, day=1)
    end = start + timedelta(days=365 * 2)
    period = "P1Y"
    bins = get_contributions_count(
        "building=* and building!=no and type:way",
        start,
        end,
        period,
    )
    assert bins == [
        {
            "start": start,
            "end": datetime(year=2023, month=1, day=1),
            "value": bin_2022,
        },
        {
            "start": datetime(year=2023, month=1, day=1),
            "end": end,
            "value": bin_2023,
        },
    ]
