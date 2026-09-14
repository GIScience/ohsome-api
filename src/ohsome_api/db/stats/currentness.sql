-- $1: aoi
-- $2: start
-- $3: end
-- $4: series
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom as geom
)

SELECT
    %(aggregation_clause)s,
    WIDTH_BUCKET(valid_from, $4::timestamptz []) AS time_bin
FROM contributions c
JOIN aoi on (ST_INTERSECTS(c.geom, aoi.geom))
WHERE
    valid_from >= $2::timestamptz AND valid_from < $3::timestamptz
    AND (status_geom_type).status = 'latest'
    AND %(filter_clause)s
GROUP BY time_bin
ORDER BY time_bin
