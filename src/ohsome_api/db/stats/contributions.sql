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
WHERE 1 = 1
    AND (%(geometry_clause)s) -- (list of multipolygon parts based on clustering)
    AND (valid_from >= $2::timestamptz AND valid_from < $3::timestamptz)
    AND (
        (%(filter_clause)s) OR
        -- HACK: ohsome-filter-to-sql does not know about tags before.
        -- Apply same tag filter to tags_before.
        -- In this case all other filter parts are duplicated
        -- and hopefully ignored by query planner.
        (%(filter_clause_tags_before)s)
    )
GROUP BY time_bin
ORDER BY time_bin
