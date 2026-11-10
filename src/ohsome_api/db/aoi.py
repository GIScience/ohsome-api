import logging
import math
from dataclasses import dataclass

from shapely.geometry import MultiPolygon, Polygon
from shapely.wkt import loads as load_wkt

from ohsome_api.config import CONFIG

logger = logging.getLogger("ohsome-api")

Bounds = tuple[float, float, float, float]


def bbox_intersects(a: Bounds, b: Bounds) -> bool:
    return a[0] <= b[2] and b[0] <= a[2] and a[1] <= b[3] and b[1] <= a[3]


def bbox_area(bounds: Bounds) -> float:
    minx, miny, maxx, maxy = bounds
    return (maxx - minx) * (maxy - miny)


def bbox_union(bounds_list: list[Bounds]) -> Bounds:
    return (
        min(b[0] for b in bounds_list),
        min(b[1] for b in bounds_list),
        max(b[2] for b in bounds_list),
        max(b[3] for b in bounds_list),
    )


@dataclass
class Part:
    index: int  # 1-based position within the original MultiPolygon
    geom: Polygon
    bounds: Bounds


@dataclass
class Cluster:
    parts: list[Part]
    bounds: Bounds


class UnionFind:
    def __init__(self, n: int) -> None:
        self._parent = list(range(n))

    def find(self, x: int) -> int:
        while self._parent[x] != x:
            self._parent[x] = self._parent[self._parent[x]]
            x = self._parent[x]
        return x

    def union(self, a: int, b: int) -> None:
        ra, rb = self.find(a), self.find(b)
        if ra != rb:
            self._parent[ra] = rb


def _parts_of(aoi: MultiPolygon) -> list[Part]:
    geoms: list[Polygon] = list(aoi.geoms)
    return [
        Part(index=i, geom=g, bounds=g.bounds) for i, g in enumerate(geoms, start=1)
    ]


def _merge_overlapping(parts: list[Part]) -> list[Cluster]:
    uf = UnionFind(len(parts))
    for i in range(len(parts)):
        for j in range(i + 1, len(parts)):
            if bbox_intersects(parts[i].bounds, parts[j].bounds):
                uf.union(i, j)

    groups: dict[int, list[Part]] = {}
    for i, part in enumerate(parts):
        groups.setdefault(uf.find(i), []).append(part)

    return [
        Cluster(parts=members, bounds=bbox_union([p.bounds for p in members]))
        for members in groups.values()
    ]


def _greedy_merge(clusters: list[Cluster], k_max: int) -> list[Cluster]:
    if len(clusters) <= k_max:
        return clusters

    clusters = list(clusters)
    while len(clusters) > k_max:
        best_cost: float = math.inf
        best_pair: tuple[int, int] = (-1, -1)
        for i in range(len(clusters)):
            for j in range(i + 1, len(clusters)):
                merged_bounds = bbox_union([clusters[i].bounds, clusters[j].bounds])
                cost = (
                    bbox_area(merged_bounds)
                    - bbox_area(clusters[i].bounds)
                    - bbox_area(clusters[j].bounds)
                )
                if cost < best_cost:
                    best_cost = cost
                    best_pair = (i, j)

        i, j = best_pair
        merged_bounds = bbox_union([clusters[i].bounds, clusters[j].bounds])

        merged = Cluster(
            parts=clusters[i].parts + clusters[j].parts,
            bounds=merged_bounds,
        )
        clusters = [c for idx, c in enumerate(clusters) if idx not in (i, j)]
        clusters.append(merged)

    return clusters


def wkt_to_geometry(wkt: str) -> Polygon | MultiPolygon:
    geom = load_wkt(wkt)
    if not isinstance(geom, Polygon | MultiPolygon):
        raise TypeError("AOI geometry must be a Polygon or MultiPolygon.")
    return geom


def cluster(
    aoi: MultiPolygon,
    k_max: int = CONFIG.ohsomedb.aoi_clusters_max,
) -> list[MultiPolygon]:
    """Spatial cluster of Polygons from a MultiPolygon into `k_max` MultiPolygons."""
    parts = _parts_of(aoi)
    logger.debug(f"Clustering {len(parts)} parts of AOI geometry...")
    clusters = _merge_overlapping(parts)
    logger.debug(f"Clustering {len(clusters)} clusters of AOI geometry merged...")
    clusters = _greedy_merge(clusters, k_max)
    logger.debug(f"Clustering {len(clusters)} clusters of AOI geometry clustered...")
    return [MultiPolygon([p.geom for p in c.parts]) for c in clusters]
