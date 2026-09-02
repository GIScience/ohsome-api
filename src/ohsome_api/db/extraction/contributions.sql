
-- $1: aoi
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
    contrib_type,
    CASE WHEN c.contrib_type = 'DELETION' THEN c.tags_before ELSE c.tags END
        AS tags,
    tags_before,
    (status_geom_type).geom_type as geom_type,
    ST_AsBinary(c.geom) as geom,
    ST_XMin(c.geom) as xmin,
    ST_YMin(c.geom) as ymin,
    ST_XMax(c.geom) as xmax,
    ST_YMax(c.geom) as ymax
FROM contributions c
JOIN aoi ON (ST_INTERSECTS(aoi.geom, c.geom))
WHERE
    1 = 1
    AND status_geom_type = ANY(array[
        ('latest', 'Point')::status_geom_type_type,
        ('latest','LineString')::status_geom_type_type,
        ('latest','Polygon')::status_geom_type_type,
        ('latest','MultiPolygon')::status_geom_type_type,
        ('history','Point')::status_geom_type_type,
        ('history','LineString')::status_geom_type_type,
        ('history','Polygon')::status_geom_type_type,
        ('history','MultiPolygon')::status_geom_type_type,
        ('deleted','Point')::status_geom_type_type,
        ('deleted','LineString')::status_geom_type_type,
        ('deleted','Polygon')::status_geom_type_type,
        ('deleted','MultiPolygon')::status_geom_type_type
    ])
    AND (%(time_clause)s)
    AND (
        (%(filter_clause)s) OR
        -- HACK: ohsome-filter-to-sql does not know about tags before.
        -- Apply same tag filter to tags_before.
        -- In this case all other filter parts are duplicated
        -- and hopefully ignored by query planner.
        (%(filter_clause_tags_before)s)
    )
