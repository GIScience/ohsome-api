-- $1: aoi
-- $2: start
-- $3: end
-- $4: series
WITH aoi AS (
    SELECT ST_GEOMFROMTEXT($1, 4326) as geom
),

series AS (
    SELECT UNNEST($4::timestamptz []) AS ts
)

SELECT
    %(aggregation_clause)s,
    series.ts
FROM contributions c
JOIN aoi ON (ST_INTERSECTS(c.geom, aoi.geom))
JOIN series ON (valid_from <= series.ts AND valid_to > series.ts)
WHERE
    valid_to > $2::timestamptz and valid_from <= $3::timestamptz
    AND (%(filter_clause)s)
    -- exclude deleted and invalid states
    AND (status_geom_type).status in ('history', 'latest')
GROUP BY series.ts
ORDER BY series.ts
