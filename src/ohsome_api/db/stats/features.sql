-- $1: aoi
-- $2: series
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom as geom
),

series AS (
    SELECT UNNEST($2::timestamptz []) AS ts
)

SELECT
    %(aggregation_clause)s,
    series.ts
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
    AND (status_geom_type).status in ('history', 'latest')
		-- join by timestamp
		AND valid_from <= series.ts
		AND valid_to > series.ts
GROUP BY series.ts
ORDER BY series.ts
