-- $1: start
-- $2: end
-- $3: series
-- $4: aoi
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($4, 4326))).geom as geom
)

SELECT
    %(aggregation_clause)s,
    WIDTH_BUCKET(valid_from, $3::timestamptz []) AS time_bin
FROM contributions c, aoi
WHERE
    1 = 1
    AND %(filter_clause)s
    AND valid_from >= $1::timestamptz
    AND valid_from < $2::timestamptz
    AND ST_INTERSECTS(c.geom, aoi.geom)
    AND (status_geom_type).status = 'latest'  -- noqa
GROUP BY time_bin
ORDER BY time_bin
