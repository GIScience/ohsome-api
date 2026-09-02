-- $1: aoi
-- $2: ids
-- $3: versions
WITH aoi AS (
    SELECT (ST_DUMP(ST_GEOMFROMTEXT($1, 4326))).geom AS geom
),

collection AS (
    SELECT UNNEST($2::int []) AS id, UNNEST($3::int []) AS version
)

SELECT
    osm_type,
    osm_id,
    valid_from,
    valid_to,
    osm_version,
    osm_minor_version,
    osm_edits,
    user_id,
    user_name,
    changeset_id,
    tags,
    ST_XMIN(proc.geom) AS xmin,
    ST_YMIN(proc.geom) AS ymin,
    ST_XMAX(proc.geom) AS xmax,
    ST_YMAX(proc.geom) AS ymax,
    (status_geom_type).geom_type as geom_type,
    ST_AsBinary(proc.geom) as geom,
    proc.clipped,
    m.relation_osm_id as part_of,
    m.member_role as part_of_role,
    m.member_pos_list[array_position(m.relation_osm_version_list , collection.version)] as part_of_pos
FROM collection
JOIN contributions_members m
    ON (
        collection.id = m.relation_osm_id
        AND collection.version = ANY(m.relation_osm_version_list)
    )
JOIN contributions AS c
    ON (
        m.member_osm_type = c.osm_type
        AND m.member_osm_id = c.osm_id
    )
%(clipped_geom_sql)s
WHERE (%(time_clause)s)
AND (%(filter_clause)s)
