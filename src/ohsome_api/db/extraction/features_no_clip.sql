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
    ST_XMIN(c.geom) AS xmin,
    ST_YMIN(c.geom) AS ymin,
    ST_XMAX(c.geom) AS xmax,
    ST_YMAX(c.geom) AS ymax,
    (status_geom_type).geom_type AS geom_type,
    ST_AsBinary(c.geom) AS geom,
    false AS clipped
FROM contributions c
JOIN aoi ON ST_INTERSECTS(c.geom, aoi.geom)
-- TODO: Is it right that the cross join is not needed here?
WHERE
    1 = 1
    AND (%(time_clause)s)
    AND (%(filter_clause)s)
