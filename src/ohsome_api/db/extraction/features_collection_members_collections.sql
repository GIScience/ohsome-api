-- $1: aoi
-- $2: ids
-- $3: version
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom AS geom
),

collection AS (
		SELECT UNNEST($2::int []) AS id, UNNEST($3::int []) AS version
),

members AS (
    SELECT
        collection.id AS relation_id,
        (status_geom_type).geom_type AS geom_type,
        %(select_geom_sql)s
    FROM contributions AS c
    JOIN contributions_members m
        ON (
            m.member_osm_type = c.osm_type
            AND m.member_osm_id = c.osm_id
        )
    JOIN collection
        ON (
            collection.id = m.relation_osm_id
            AND collection.version = ANY(m.relation_osm_version_list)
        )
    %(join_geom_sql)s
    WHERE
				1=1
				AND (%(time_clause)s)
				AND (%(filter_clause)s)
    GROUP BY collection.id, (status_geom_type).geom_type
)

SELECT
    relation_id,
    geom_type,
    ST_ASBINARY(geom) AS geom,
    clipped_count > 0 AS clipped,
    intersects_count > 0 AS intersects,
    ST_XMIN(geom) AS xmin,
    ST_YMIN(geom) AS ymin,
    ST_XMAX(geom) AS xmax,
    ST_YMAX(geom) AS ymax
FROM members
