-- $1: series
-- $2: aoi
-- $3: group_by_tag
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($2, 4326))).geom as geom
),

series AS (
    SELECT UNNEST($1::timestamptz []) AS ts
)

SELECT
    %(aggregation_clause)s,
    series.ts,
    tags ->> $3 as tag_value
FROM contributions c, aoi, series
WHERE
    1 = 1
    AND (%(filter_clause)s)
    -- Global time filter has been part of this query from the beginning on,
    -- but is now disabled because we believe its not necessary.
    -- AND valid_from <= $end::timestamptz
    -- AND valid_to > $start::timestamptz
    AND ST_INTERSECTS(c.geom, aoi.geom)
    -- exclude deleted and invalid states
    AND (status_geom_type)
.status in ('history', 'latest')
-- join by timestamp
AND valid_from <= series.ts
AND valid_to > series.ts
GROUP BY series.ts, tag_value
ORDER BY series.ts, tag_value
LIMIT %(limit)s
