-- $1: aoi
-- $2: start
-- $3: end
-- $4: series
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom as geom
)

SELECT
    COUNT(DISTINCT user_id) AS value,
    WIDTH_BUCKET(valid_from, $4::timestamptz []) AS time_bin
FROM contributions c
JOIN aoi ON (ST_INTERSECTS(c.geom, aoi.geom))
WHERE
    valid_from >= $2::timestamptz AND valid_from < $3::timestamptz
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
