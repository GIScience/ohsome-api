-- $1: aoi
-- $2: start
-- $3: end
-- $4: series
WITH aoi AS (
    SELECT $1::geometry as geom -- this is the original AOI geometry
),

series AS (
    SELECT UNNEST($4::timestamptz []) AS ts
)

SELECT
    %(aggregation_clause)s,
    series.ts
FROM contributions c
%(aoi_join)s -- this join is only needed if clip is used (st_intersection in aggregation clause)
JOIN series ON (valid_from <= series.ts AND valid_to > series.ts)
WHERE 1 = 1
    AND (%(geometry_clause)s) -- (list of multipolygon parts based on clustering)
    AND (%(disable_time_boundary_check)s OR (valid_to > $2::timestamptz and valid_from <= $3::timestamptz))
    AND (%(filter_clause)s)
    -- exclude deleted and invalid states
    AND (status_geom_type).status in ('history', 'latest')
GROUP BY series.ts
ORDER BY series.ts
