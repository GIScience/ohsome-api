-- $1: aoi
-- $2: start
-- $3: end
-- $4: series
WITH aoi AS (
    SELECT $1::geometry as geom -- this is the original AOI geometry
)

SELECT
    %(aggregation_clause)s,
    WIDTH_BUCKET(valid_from, $4::timestamptz []) AS time_bin
FROM contributions c
%(aoi_join)s -- this join is only needed if clip is used (st_intersection in aggregation clause)
WHERE 1 = 1
    AND (%(geometry_clause)s) -- (list of multipolygon parts based on clustering)
    AND (valid_from >= $2::timestamptz AND valid_from < $3::timestamptz)
    AND (status_geom_type).status = 'latest'
    AND (%(filter_clause)s)
GROUP BY time_bin
ORDER BY time_bin
