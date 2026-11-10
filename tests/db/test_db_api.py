import pytest
from shapely.geometry import MultiPolygon, Polygon

from ohsome_api.db.aoi import (
    _merge_overlapping,
    _parts_of,
    cluster,
    wkt_to_geometry,
)


def test_wkt_to_geometry_polygon():
    geom = wkt_to_geometry("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))")
    assert isinstance(geom, Polygon)


def test_wkt_to_geometry_multipolygon():
    geom = wkt_to_geometry("""
        MULTIPOLYGON (
            ((40 40, 20 45, 45 30, 40 40)),
            ((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),
             (30 20, 20 15, 20 25, 30 20))
        )""")
    assert isinstance(geom, MultiPolygon)


def test_wkt_to_geometry_other():
    with pytest.raises(TypeError):
        wkt_to_geometry("POINT (30 10)")


def rect_wkt(x_min: float, y_min: float, width: float, height: float) -> str:
    return f"""((
        {x_min} {y_min},
        {x_min + width} {y_min},
        {x_min + width} {y_min + height},
        {x_min} {y_min + height},
        {x_min} {y_min}
    ))"""


def test_cluster():
    aoi: MultiPolygon = wkt_to_geometry(f"""MULTIPOLYGON(
        {rect_wkt(0, 0, 10, 10)},
        {rect_wkt(11, 0, 1, 1)},
        {rect_wkt(13, 0, 1, 1)},
        {rect_wkt(20, 20, 1, 1)},
        {rect_wkt(22, 20, 1, 1)}
    )""")
    clustered = cluster(aoi, 2)
    assert len(clustered) == 2
    clustered.sort(key=lambda cluster: cluster.bounds[0])
    cluster1_bb = clustered[0].bounds
    cluster2_bb = clustered[1].bounds
    assert cluster1_bb == (0.0, 0.0, 14.0, 10.0)
    assert cluster2_bb == (20.0, 20.0, 23.0, 21.0)


def test_merge_overlapping():
    aoi = wkt_to_geometry(f"""MULTIPOLYGON(
        {rect_wkt(0, 0, 10, 10)},
        {rect_wkt(5, 5, 10, 10)},
        {rect_wkt(25, 25, 10, 10)}
    )""")
    parts = _parts_of(aoi)
    unioned = _merge_overlapping(parts)
    assert len(unioned) == 2
