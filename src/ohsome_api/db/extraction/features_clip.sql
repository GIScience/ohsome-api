--$1: aoi
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom as geom
)

SELECT
    osm_type,
    osm_id,
    valid_from,
    valid_to,
    osm_version,
    osm_minor_version,
    osm_edits,
    user_id,
    user_name,
    changeset_id,
    tags,
    ST_XMIN(proc.geom) AS xmin,
    ST_YMIN(proc.geom) AS ymin,
    ST_XMAX(proc.geom) AS xmax,
    ST_YMAX(proc.geom) AS ymax,
    (status_geom_type).geom_type AS geom_type,
    ST_AsBinary(proc.geom) AS geom,
    proc.clipped
FROM contributions c
JOIN aoi ON ST_INTERSECTS(c.geom, aoi.geom)
CROSS JOIN
    LATERAL(
        SELECT ST_COVERS(aoi.geom, c.geom) AS is_covered
    ) cov
CROSS JOIN
    LATERAL(
        SELECT
            NOT cov.is_covered AS clipped,
            CASE
                WHEN cov.is_covered THEN c.geom
                ELSE ST_Intersection(c.geom, aoi.geom)
            END AS geom
    ) proc
WHERE
    1 = 1
    AND (%(time_clause)s)
    AND (%(filter_clause)s)
