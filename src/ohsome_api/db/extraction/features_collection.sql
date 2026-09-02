-- $1: aoi
-- $2: time
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
    (status_geom_type).geom_type as geom_type,
    ST_AsBinary(c.geom) as geom,
    false as clipped,
    ST_XMin(c.geom) as xmin,
    ST_YMin(c.geom) as ymin,
    ST_XMax(c.geom) as xmax,
    ST_YMax(c.geom) as ymax
FROM contributions AS c
JOIN aoi ON ST_INTERSECTS(c.geom, aoi.geom)
WHERE
    1 = 1
    AND (%(time_clause)s)
    AND (%(filter_clause)s)
