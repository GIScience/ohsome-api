--
-- PostgreSQL database dump
--

\restrict XgIUnZ9H1gPOuAz9u1vXw2O69ru7J5Zafw1pq3HN4NRfmZI4lUsG6kzTSfrqxUo

-- Dumped from database version 17.8 (Debian 17.8-1.pgdg12+1)
-- Dumped by pg_dump version 18.1 (Debian 18.1-2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: next; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA next;


--
-- Name: SCHEMA next; Type: COMMENT; Schema: -; Owner: -
--


--
-- Name: contrib_type_type; Type: TYPE; Schema: next; Owner: -
--

CREATE TYPE next.contrib_type_type AS ENUM (
    'CREATION',
    'GEOMETRY',
    'TAG',
    'TAG_GEOMETRY',
    'DELETION',
    ''
);


--
-- Name: geom_type_type; Type: TYPE; Schema: next; Owner: -
--

CREATE TYPE next.geom_type_type AS ENUM (
    'Point',
    'LineString',
    'Polygon',
    'MultiPolygon',
    'GeometryCollection'
);


--
-- Name: osm_type_type; Type: TYPE; Schema: next; Owner: -
--

CREATE TYPE next.osm_type_type AS ENUM (
    'node',
    'way',
    'relation'
);


--
-- Name: status_type; Type: TYPE; Schema: next; Owner: -
--

CREATE TYPE next.status_type AS ENUM (
    'latest',
    'deleted',
    'history',
    'invalid'
);


--
-- Name: status_geom_type_type; Type: TYPE; Schema: next; Owner: -
--

CREATE TYPE next.status_geom_type_type AS (
	status next.status_type,
	geom_type next.geom_type_type
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: changeset_state; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.changeset_state (
    last_sequence bigint NOT NULL,
    last_timestamp timestamp with time zone NOT NULL
);


--
-- Name: changesets; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.changesets (
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    closed_at timestamp with time zone,
    tags jsonb NOT NULL,
    hashtags character varying[] NOT NULL,
    user_id bigint NOT NULL,
    user_name character varying NOT NULL,
    open boolean NOT NULL,
    geom public.geometry(Polygon,4326)
);


--
-- Name: contributions; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
)
PARTITION BY LIST (status_geom_type);


--
-- Name: contributions_deleted_collection; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_deleted_collection (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_deleted_linestring; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_deleted_linestring (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_deleted_null; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_deleted_null (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_deleted_point; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_deleted_point (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_deleted_polygon_multipolygon; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_deleted_polygon_multipolygon (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_history_collection; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_history_collection (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_history_linestring; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_history_linestring (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_history_point; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_history_point (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_history_polygon_multipolygon; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_history_polygon_multipolygon (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_invalid; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_invalid (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_latest_collection; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_latest_collection (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_latest_linestring; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_latest_linestring (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_latest_point; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_latest_point (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_latest_polygon_multipolygon; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_latest_polygon_multipolygon (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL
);


--
-- Name: contributions_members; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_members (
    member_osm_type next.osm_type_type NOT NULL,
    member_osm_id bigint NOT NULL,
    member_role character varying NOT NULL,
    relation_osm_id bigint NOT NULL,
    relation_osm_version_list integer[] NOT NULL,
    member_pos_list integer[] NOT NULL
);


--
-- Name: contributions_state; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.contributions_state (
    last_sequence bigint NOT NULL,
    last_timestamp timestamp with time zone NOT NULL
);


--
-- Name: import_state; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.import_state (
    filenames character varying[] NOT NULL,
    import_type character varying NOT NULL,
    start_timestamp timestamp with time zone,
    end_timestamp timestamp with time zone
);


--
-- Name: staging_contribution_updates; Type: TABLE; Schema: next; Owner: -
--

CREATE TABLE next.staging_contribution_updates (
    valid_from timestamp with time zone NOT NULL,
    valid_to timestamp with time zone NOT NULL,
    osm_id bigint NOT NULL,
    osm_type next.osm_type_type NOT NULL,
    osm_version integer NOT NULL,
    osm_minor_version integer NOT NULL,
    osm_edits integer NOT NULL,
    user_id integer NOT NULL,
    contrib_type next.contrib_type_type NOT NULL,
    changeset_id bigint NOT NULL,
    area double precision NOT NULL,
    area_delta double precision NOT NULL,
    length double precision NOT NULL,
    length_delta double precision NOT NULL,
    contribution_id character varying NOT NULL,
    user_name character varying NOT NULL,
    tags jsonb NOT NULL,
    tags_before jsonb NOT NULL,
    centroid public.geometry(Point,4326),
    geom public.geometry(Geometry,4326),
    countries character varying[] NOT NULL,
    status_geom_type next.status_geom_type_type NOT NULL,
    members jsonb
);


--
-- Name: contributions_deleted_collection; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_deleted_collection FOR VALUES IN ('(deleted,GeometryCollection)');


--
-- Name: contributions_deleted_linestring; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_deleted_linestring FOR VALUES IN ('(deleted,LineString)');


--
-- Name: contributions_deleted_null; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_deleted_null FOR VALUES IN ('(deleted,)');


--
-- Name: contributions_deleted_point; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_deleted_point FOR VALUES IN ('(deleted,Point)');


--
-- Name: contributions_deleted_polygon_multipolygon; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_deleted_polygon_multipolygon FOR VALUES IN ('(deleted,Polygon)', '(deleted,MultiPolygon)');


--
-- Name: contributions_history_collection; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_history_collection FOR VALUES IN ('(history,GeometryCollection)');


--
-- Name: contributions_history_linestring; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_history_linestring FOR VALUES IN ('(history,LineString)');


--
-- Name: contributions_history_point; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_history_point FOR VALUES IN ('(history,Point)');


--
-- Name: contributions_history_polygon_multipolygon; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_history_polygon_multipolygon FOR VALUES IN ('(history,Polygon)', '(history,MultiPolygon)');


--
-- Name: contributions_invalid; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_invalid FOR VALUES IN ('(invalid,)', '(invalid,Point)', '(invalid,LineString)', '(invalid,Polygon)', '(invalid,MultiPolygon)', '(invalid,GeometryCollection)');


--
-- Name: contributions_latest_collection; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_latest_collection FOR VALUES IN ('(latest,GeometryCollection)');


--
-- Name: contributions_latest_linestring; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_latest_linestring FOR VALUES IN ('(latest,LineString)');


--
-- Name: contributions_latest_point; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_latest_point FOR VALUES IN ('(latest,Point)');


--
-- Name: contributions_latest_polygon_multipolygon; Type: TABLE ATTACH; Schema: next; Owner: -
--

ALTER TABLE ONLY next.contributions ATTACH PARTITION next.contributions_latest_polygon_multipolygon FOR VALUES IN ('(latest,Polygon)', '(latest,MultiPolygon)');


--
-- Name: changesets changesets_id_key; Type: CONSTRAINT; Schema: next; Owner: -
--

ALTER TABLE ONLY next.changesets
    ADD CONSTRAINT changesets_id_key UNIQUE (id);


--
-- Name: import_state import_state_filenames_import_type_key; Type: CONSTRAINT; Schema: next; Owner: -
--

ALTER TABLE ONLY next.import_state
    ADD CONSTRAINT import_state_filenames_import_type_key UNIQUE (filenames, import_type);


--
-- Name: changesets_closed_at_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX changesets_closed_at_idx ON next.changesets USING btree (closed_at);


--
-- Name: changesets_created_at_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX changesets_created_at_idx ON next.changesets USING btree (created_at);


--
-- Name: changesets_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX changesets_geom_idx ON next.changesets USING gist (geom);


--
-- Name: changesets_hashtags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX changesets_hashtags_idx ON next.changesets USING gin (hashtags);


--
-- Name: changesets_user_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX changesets_user_id_idx ON next.changesets USING btree (user_id);


--
-- Name: contributions___status_geom_type__geom_type__idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions___status_geom_type__geom_type__idx ON ONLY next.contributions USING btree (((status_geom_type).geom_type));


--
-- Name: contributions___status_geom_type__status__idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions___status_geom_type__status__idx ON ONLY next.contributions USING btree (((status_geom_type).status));


--
-- Name: contributions_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_changeset_id_idx ON ONLY next.contributions USING btree (changeset_id);


--
-- Name: contributions_deleted_collection_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_changeset_id_idx ON next.contributions_deleted_collection USING btree (changeset_id);


--
-- Name: contributions_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_geom_idx ON ONLY next.contributions USING gist (geom);


--
-- Name: contributions_deleted_collection_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_geom_idx ON next.contributions_deleted_collection USING gist (geom);


--
-- Name: contributions_deleted_collection_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_geom_type_idx ON next.contributions_deleted_collection USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_osm_type_osm_id_idx ON ONLY next.contributions USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_collection_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_osm_type_osm_id_idx ON next.contributions_deleted_collection USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_collection_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_status_idx ON next.contributions_deleted_collection USING btree (((status_geom_type).status));


--
-- Name: contributions_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_tags_idx ON ONLY next.contributions USING gin (tags);


--
-- Name: contributions_deleted_collection_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_tags_idx ON next.contributions_deleted_collection USING gin (tags);


--
-- Name: contributions_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_valid_from_idx ON ONLY next.contributions USING btree (valid_from);


--
-- Name: contributions_deleted_collection_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_valid_from_idx ON next.contributions_deleted_collection USING btree (valid_from);


--
-- Name: contributions_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_valid_to_idx ON ONLY next.contributions USING btree (valid_to);


--
-- Name: contributions_deleted_collection_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_collection_valid_to_idx ON next.contributions_deleted_collection USING btree (valid_to);


--
-- Name: contributions_deleted_linestring_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_changeset_id_idx ON next.contributions_deleted_linestring USING btree (changeset_id);


--
-- Name: contributions_deleted_linestring_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_geom_idx ON next.contributions_deleted_linestring USING gist (geom);


--
-- Name: contributions_deleted_linestring_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_geom_type_idx ON next.contributions_deleted_linestring USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_deleted_linestring_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_osm_type_osm_id_idx ON next.contributions_deleted_linestring USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_linestring_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_status_idx ON next.contributions_deleted_linestring USING btree (((status_geom_type).status));


--
-- Name: contributions_deleted_linestring_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_tags_idx ON next.contributions_deleted_linestring USING gin (tags);


--
-- Name: contributions_deleted_linestring_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_valid_from_idx ON next.contributions_deleted_linestring USING btree (valid_from);


--
-- Name: contributions_deleted_linestring_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_linestring_valid_to_idx ON next.contributions_deleted_linestring USING btree (valid_to);


--
-- Name: contributions_deleted_null_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_changeset_id_idx ON next.contributions_deleted_null USING btree (changeset_id);


--
-- Name: contributions_deleted_null_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_geom_idx ON next.contributions_deleted_null USING gist (geom);


--
-- Name: contributions_deleted_null_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_geom_type_idx ON next.contributions_deleted_null USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_deleted_null_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_osm_type_osm_id_idx ON next.contributions_deleted_null USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_null_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_status_idx ON next.contributions_deleted_null USING btree (((status_geom_type).status));


--
-- Name: contributions_deleted_null_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_tags_idx ON next.contributions_deleted_null USING gin (tags);


--
-- Name: contributions_deleted_null_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_valid_from_idx ON next.contributions_deleted_null USING btree (valid_from);


--
-- Name: contributions_deleted_null_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_null_valid_to_idx ON next.contributions_deleted_null USING btree (valid_to);


--
-- Name: contributions_deleted_point_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_changeset_id_idx ON next.contributions_deleted_point USING btree (changeset_id);


--
-- Name: contributions_deleted_point_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_geom_idx ON next.contributions_deleted_point USING gist (geom);


--
-- Name: contributions_deleted_point_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_geom_type_idx ON next.contributions_deleted_point USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_deleted_point_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_osm_type_osm_id_idx ON next.contributions_deleted_point USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_point_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_status_idx ON next.contributions_deleted_point USING btree (((status_geom_type).status));


--
-- Name: contributions_deleted_point_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_tags_idx ON next.contributions_deleted_point USING gin (tags);


--
-- Name: contributions_deleted_point_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_valid_from_idx ON next.contributions_deleted_point USING btree (valid_from);


--
-- Name: contributions_deleted_point_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_point_valid_to_idx ON next.contributions_deleted_point USING btree (valid_to);


--
-- Name: contributions_deleted_polygon_multipolygon_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_changeset_id_idx ON next.contributions_deleted_polygon_multipolygon USING btree (changeset_id);


--
-- Name: contributions_deleted_polygon_multipolygon_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_geom_idx ON next.contributions_deleted_polygon_multipolygon USING gist (geom);


--
-- Name: contributions_deleted_polygon_multipolygon_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_geom_type_idx ON next.contributions_deleted_polygon_multipolygon USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_deleted_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_osm_type_osm_id_idx ON next.contributions_deleted_polygon_multipolygon USING btree (osm_type, osm_id);


--
-- Name: contributions_deleted_polygon_multipolygon_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_status_idx ON next.contributions_deleted_polygon_multipolygon USING btree (((status_geom_type).status));


--
-- Name: contributions_deleted_polygon_multipolygon_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_tags_idx ON next.contributions_deleted_polygon_multipolygon USING gin (tags);


--
-- Name: contributions_deleted_polygon_multipolygon_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_valid_from_idx ON next.contributions_deleted_polygon_multipolygon USING btree (valid_from);


--
-- Name: contributions_deleted_polygon_multipolygon_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_deleted_polygon_multipolygon_valid_to_idx ON next.contributions_deleted_polygon_multipolygon USING btree (valid_to);


--
-- Name: contributions_history_collection_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_changeset_id_idx ON next.contributions_history_collection USING btree (changeset_id);


--
-- Name: contributions_history_collection_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_geom_idx ON next.contributions_history_collection USING gist (geom);


--
-- Name: contributions_history_collection_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_geom_type_idx ON next.contributions_history_collection USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_history_collection_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_osm_type_osm_id_idx ON next.contributions_history_collection USING btree (osm_type, osm_id);


--
-- Name: contributions_history_collection_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_status_idx ON next.contributions_history_collection USING btree (((status_geom_type).status));


--
-- Name: contributions_history_collection_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_tags_idx ON next.contributions_history_collection USING gin (tags);


--
-- Name: contributions_history_collection_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_valid_from_idx ON next.contributions_history_collection USING btree (valid_from);


--
-- Name: contributions_history_collection_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_collection_valid_to_idx ON next.contributions_history_collection USING btree (valid_to);


--
-- Name: contributions_history_linestring_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_changeset_id_idx ON next.contributions_history_linestring USING btree (changeset_id);


--
-- Name: contributions_history_linestring_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_geom_idx ON next.contributions_history_linestring USING gist (geom);


--
-- Name: contributions_history_linestring_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_geom_type_idx ON next.contributions_history_linestring USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_history_linestring_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_osm_type_osm_id_idx ON next.contributions_history_linestring USING btree (osm_type, osm_id);


--
-- Name: contributions_history_linestring_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_status_idx ON next.contributions_history_linestring USING btree (((status_geom_type).status));


--
-- Name: contributions_history_linestring_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_tags_idx ON next.contributions_history_linestring USING gin (tags);


--
-- Name: contributions_history_linestring_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_valid_from_idx ON next.contributions_history_linestring USING btree (valid_from);


--
-- Name: contributions_history_linestring_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_linestring_valid_to_idx ON next.contributions_history_linestring USING btree (valid_to);


--
-- Name: contributions_history_point_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_changeset_id_idx ON next.contributions_history_point USING btree (changeset_id);


--
-- Name: contributions_history_point_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_geom_idx ON next.contributions_history_point USING gist (geom);


--
-- Name: contributions_history_point_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_geom_type_idx ON next.contributions_history_point USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_history_point_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_osm_type_osm_id_idx ON next.contributions_history_point USING btree (osm_type, osm_id);


--
-- Name: contributions_history_point_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_status_idx ON next.contributions_history_point USING btree (((status_geom_type).status));


--
-- Name: contributions_history_point_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_tags_idx ON next.contributions_history_point USING gin (tags);


--
-- Name: contributions_history_point_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_valid_from_idx ON next.contributions_history_point USING btree (valid_from);


--
-- Name: contributions_history_point_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_point_valid_to_idx ON next.contributions_history_point USING btree (valid_to);


--
-- Name: contributions_history_polygon_multipolygon_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_changeset_id_idx ON next.contributions_history_polygon_multipolygon USING btree (changeset_id);


--
-- Name: contributions_history_polygon_multipolygon_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_geom_idx ON next.contributions_history_polygon_multipolygon USING gist (geom);


--
-- Name: contributions_history_polygon_multipolygon_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_geom_type_idx ON next.contributions_history_polygon_multipolygon USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_history_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_osm_type_osm_id_idx ON next.contributions_history_polygon_multipolygon USING btree (osm_type, osm_id);


--
-- Name: contributions_history_polygon_multipolygon_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_status_idx ON next.contributions_history_polygon_multipolygon USING btree (((status_geom_type).status));


--
-- Name: contributions_history_polygon_multipolygon_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_tags_idx ON next.contributions_history_polygon_multipolygon USING gin (tags);


--
-- Name: contributions_history_polygon_multipolygon_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_valid_from_idx ON next.contributions_history_polygon_multipolygon USING btree (valid_from);


--
-- Name: contributions_history_polygon_multipolygon_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_history_polygon_multipolygon_valid_to_idx ON next.contributions_history_polygon_multipolygon USING btree (valid_to);


--
-- Name: contributions_invalid_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_changeset_id_idx ON next.contributions_invalid USING btree (changeset_id);


--
-- Name: contributions_invalid_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_geom_idx ON next.contributions_invalid USING gist (geom);


--
-- Name: contributions_invalid_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_geom_type_idx ON next.contributions_invalid USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_invalid_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_osm_type_osm_id_idx ON next.contributions_invalid USING btree (osm_type, osm_id);


--
-- Name: contributions_invalid_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_status_idx ON next.contributions_invalid USING btree (((status_geom_type).status));


--
-- Name: contributions_invalid_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_tags_idx ON next.contributions_invalid USING gin (tags);


--
-- Name: contributions_invalid_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_valid_from_idx ON next.contributions_invalid USING btree (valid_from);


--
-- Name: contributions_invalid_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_invalid_valid_to_idx ON next.contributions_invalid USING btree (valid_to);


--
-- Name: contributions_latest_collection_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_changeset_id_idx ON next.contributions_latest_collection USING btree (changeset_id);


--
-- Name: contributions_latest_collection_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_geom_idx ON next.contributions_latest_collection USING gist (geom);


--
-- Name: contributions_latest_collection_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_geom_type_idx ON next.contributions_latest_collection USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_latest_collection_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_osm_type_osm_id_idx ON next.contributions_latest_collection USING btree (osm_type, osm_id);


--
-- Name: contributions_latest_collection_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_status_idx ON next.contributions_latest_collection USING btree (((status_geom_type).status));


--
-- Name: contributions_latest_collection_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_tags_idx ON next.contributions_latest_collection USING gin (tags);


--
-- Name: contributions_latest_collection_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_valid_from_idx ON next.contributions_latest_collection USING btree (valid_from);


--
-- Name: contributions_latest_collection_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_collection_valid_to_idx ON next.contributions_latest_collection USING btree (valid_to);


--
-- Name: contributions_latest_linestring_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_changeset_id_idx ON next.contributions_latest_linestring USING btree (changeset_id);


--
-- Name: contributions_latest_linestring_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_geom_idx ON next.contributions_latest_linestring USING gist (geom);


--
-- Name: contributions_latest_linestring_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_geom_type_idx ON next.contributions_latest_linestring USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_latest_linestring_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_osm_type_osm_id_idx ON next.contributions_latest_linestring USING btree (osm_type, osm_id);


--
-- Name: contributions_latest_linestring_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_status_idx ON next.contributions_latest_linestring USING btree (((status_geom_type).status));


--
-- Name: contributions_latest_linestring_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_tags_idx ON next.contributions_latest_linestring USING gin (tags);


--
-- Name: contributions_latest_linestring_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_valid_from_idx ON next.contributions_latest_linestring USING btree (valid_from);


--
-- Name: contributions_latest_linestring_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_linestring_valid_to_idx ON next.contributions_latest_linestring USING btree (valid_to);


--
-- Name: contributions_latest_point_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_changeset_id_idx ON next.contributions_latest_point USING btree (changeset_id);


--
-- Name: contributions_latest_point_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_geom_idx ON next.contributions_latest_point USING gist (geom);


--
-- Name: contributions_latest_point_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_geom_type_idx ON next.contributions_latest_point USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_latest_point_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_osm_type_osm_id_idx ON next.contributions_latest_point USING btree (osm_type, osm_id);


--
-- Name: contributions_latest_point_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_status_idx ON next.contributions_latest_point USING btree (((status_geom_type).status));


--
-- Name: contributions_latest_point_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_tags_idx ON next.contributions_latest_point USING gin (tags);


--
-- Name: contributions_latest_point_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_valid_from_idx ON next.contributions_latest_point USING btree (valid_from);


--
-- Name: contributions_latest_point_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_point_valid_to_idx ON next.contributions_latest_point USING btree (valid_to);


--
-- Name: contributions_latest_polygon_multipolygon_changeset_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_changeset_id_idx ON next.contributions_latest_polygon_multipolygon USING btree (changeset_id);


--
-- Name: contributions_latest_polygon_multipolygon_geom_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_geom_idx ON next.contributions_latest_polygon_multipolygon USING gist (geom);


--
-- Name: contributions_latest_polygon_multipolygon_geom_type_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_geom_type_idx ON next.contributions_latest_polygon_multipolygon USING btree (((status_geom_type).geom_type));


--
-- Name: contributions_latest_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_osm_type_osm_id_idx ON next.contributions_latest_polygon_multipolygon USING btree (osm_type, osm_id);


--
-- Name: contributions_latest_polygon_multipolygon_status_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_status_idx ON next.contributions_latest_polygon_multipolygon USING btree (((status_geom_type).status));


--
-- Name: contributions_latest_polygon_multipolygon_tags_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_tags_idx ON next.contributions_latest_polygon_multipolygon USING gin (tags);


--
-- Name: contributions_latest_polygon_multipolygon_valid_from_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_valid_from_idx ON next.contributions_latest_polygon_multipolygon USING btree (valid_from);


--
-- Name: contributions_latest_polygon_multipolygon_valid_to_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_latest_polygon_multipolygon_valid_to_idx ON next.contributions_latest_polygon_multipolygon USING btree (valid_to);


--
-- Name: contributions_members_member_osm_type_member_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_members_member_osm_type_member_osm_id_idx ON next.contributions_members USING btree (member_osm_type, member_osm_id);


--
-- Name: contributions_members_member_role_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_members_member_role_idx ON next.contributions_members USING btree (member_role);


--
-- Name: contributions_members_relation_osm_id_idx; Type: INDEX; Schema: next; Owner: -
--

CREATE INDEX contributions_members_relation_osm_id_idx ON next.contributions_members USING btree (relation_osm_id);


--
-- Name: contributions_deleted_collection_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_deleted_collection_changeset_id_idx;


--
-- Name: contributions_deleted_collection_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_deleted_collection_geom_idx;


--
-- Name: contributions_deleted_collection_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_deleted_collection_geom_type_idx;


--
-- Name: contributions_deleted_collection_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_deleted_collection_osm_type_osm_id_idx;


--
-- Name: contributions_deleted_collection_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_deleted_collection_status_idx;


--
-- Name: contributions_deleted_collection_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_deleted_collection_tags_idx;


--
-- Name: contributions_deleted_collection_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_deleted_collection_valid_from_idx;


--
-- Name: contributions_deleted_collection_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_deleted_collection_valid_to_idx;


--
-- Name: contributions_deleted_linestring_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_deleted_linestring_changeset_id_idx;


--
-- Name: contributions_deleted_linestring_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_deleted_linestring_geom_idx;


--
-- Name: contributions_deleted_linestring_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_deleted_linestring_geom_type_idx;


--
-- Name: contributions_deleted_linestring_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_deleted_linestring_osm_type_osm_id_idx;


--
-- Name: contributions_deleted_linestring_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_deleted_linestring_status_idx;


--
-- Name: contributions_deleted_linestring_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_deleted_linestring_tags_idx;


--
-- Name: contributions_deleted_linestring_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_deleted_linestring_valid_from_idx;


--
-- Name: contributions_deleted_linestring_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_deleted_linestring_valid_to_idx;


--
-- Name: contributions_deleted_null_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_deleted_null_changeset_id_idx;


--
-- Name: contributions_deleted_null_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_deleted_null_geom_idx;


--
-- Name: contributions_deleted_null_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_deleted_null_geom_type_idx;


--
-- Name: contributions_deleted_null_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_deleted_null_osm_type_osm_id_idx;


--
-- Name: contributions_deleted_null_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_deleted_null_status_idx;


--
-- Name: contributions_deleted_null_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_deleted_null_tags_idx;


--
-- Name: contributions_deleted_null_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_deleted_null_valid_from_idx;


--
-- Name: contributions_deleted_null_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_deleted_null_valid_to_idx;


--
-- Name: contributions_deleted_point_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_deleted_point_changeset_id_idx;


--
-- Name: contributions_deleted_point_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_deleted_point_geom_idx;


--
-- Name: contributions_deleted_point_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_deleted_point_geom_type_idx;


--
-- Name: contributions_deleted_point_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_deleted_point_osm_type_osm_id_idx;


--
-- Name: contributions_deleted_point_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_deleted_point_status_idx;


--
-- Name: contributions_deleted_point_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_deleted_point_tags_idx;


--
-- Name: contributions_deleted_point_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_deleted_point_valid_from_idx;


--
-- Name: contributions_deleted_point_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_deleted_point_valid_to_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_changeset_id_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_geom_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_geom_type_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_osm_type_osm_id_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_status_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_tags_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_valid_from_idx;


--
-- Name: contributions_deleted_polygon_multipolygon_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_deleted_polygon_multipolygon_valid_to_idx;


--
-- Name: contributions_history_collection_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_history_collection_changeset_id_idx;


--
-- Name: contributions_history_collection_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_history_collection_geom_idx;


--
-- Name: contributions_history_collection_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_history_collection_geom_type_idx;


--
-- Name: contributions_history_collection_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_history_collection_osm_type_osm_id_idx;


--
-- Name: contributions_history_collection_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_history_collection_status_idx;


--
-- Name: contributions_history_collection_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_history_collection_tags_idx;


--
-- Name: contributions_history_collection_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_history_collection_valid_from_idx;


--
-- Name: contributions_history_collection_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_history_collection_valid_to_idx;


--
-- Name: contributions_history_linestring_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_history_linestring_changeset_id_idx;


--
-- Name: contributions_history_linestring_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_history_linestring_geom_idx;


--
-- Name: contributions_history_linestring_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_history_linestring_geom_type_idx;


--
-- Name: contributions_history_linestring_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_history_linestring_osm_type_osm_id_idx;


--
-- Name: contributions_history_linestring_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_history_linestring_status_idx;


--
-- Name: contributions_history_linestring_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_history_linestring_tags_idx;


--
-- Name: contributions_history_linestring_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_history_linestring_valid_from_idx;


--
-- Name: contributions_history_linestring_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_history_linestring_valid_to_idx;


--
-- Name: contributions_history_point_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_history_point_changeset_id_idx;


--
-- Name: contributions_history_point_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_history_point_geom_idx;


--
-- Name: contributions_history_point_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_history_point_geom_type_idx;


--
-- Name: contributions_history_point_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_history_point_osm_type_osm_id_idx;


--
-- Name: contributions_history_point_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_history_point_status_idx;


--
-- Name: contributions_history_point_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_history_point_tags_idx;


--
-- Name: contributions_history_point_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_history_point_valid_from_idx;


--
-- Name: contributions_history_point_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_history_point_valid_to_idx;


--
-- Name: contributions_history_polygon_multipolygon_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_changeset_id_idx;


--
-- Name: contributions_history_polygon_multipolygon_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_geom_idx;


--
-- Name: contributions_history_polygon_multipolygon_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_geom_type_idx;


--
-- Name: contributions_history_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_osm_type_osm_id_idx;


--
-- Name: contributions_history_polygon_multipolygon_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_status_idx;


--
-- Name: contributions_history_polygon_multipolygon_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_tags_idx;


--
-- Name: contributions_history_polygon_multipolygon_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_valid_from_idx;


--
-- Name: contributions_history_polygon_multipolygon_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_history_polygon_multipolygon_valid_to_idx;


--
-- Name: contributions_invalid_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_invalid_changeset_id_idx;


--
-- Name: contributions_invalid_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_invalid_geom_idx;


--
-- Name: contributions_invalid_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_invalid_geom_type_idx;


--
-- Name: contributions_invalid_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_invalid_osm_type_osm_id_idx;


--
-- Name: contributions_invalid_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_invalid_status_idx;


--
-- Name: contributions_invalid_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_invalid_tags_idx;


--
-- Name: contributions_invalid_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_invalid_valid_from_idx;


--
-- Name: contributions_invalid_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_invalid_valid_to_idx;


--
-- Name: contributions_latest_collection_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_latest_collection_changeset_id_idx;


--
-- Name: contributions_latest_collection_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_latest_collection_geom_idx;


--
-- Name: contributions_latest_collection_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_latest_collection_geom_type_idx;


--
-- Name: contributions_latest_collection_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_latest_collection_osm_type_osm_id_idx;


--
-- Name: contributions_latest_collection_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_latest_collection_status_idx;


--
-- Name: contributions_latest_collection_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_latest_collection_tags_idx;


--
-- Name: contributions_latest_collection_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_latest_collection_valid_from_idx;


--
-- Name: contributions_latest_collection_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_latest_collection_valid_to_idx;


--
-- Name: contributions_latest_linestring_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_latest_linestring_changeset_id_idx;


--
-- Name: contributions_latest_linestring_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_latest_linestring_geom_idx;


--
-- Name: contributions_latest_linestring_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_latest_linestring_geom_type_idx;


--
-- Name: contributions_latest_linestring_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_latest_linestring_osm_type_osm_id_idx;


--
-- Name: contributions_latest_linestring_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_latest_linestring_status_idx;


--
-- Name: contributions_latest_linestring_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_latest_linestring_tags_idx;


--
-- Name: contributions_latest_linestring_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_latest_linestring_valid_from_idx;


--
-- Name: contributions_latest_linestring_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_latest_linestring_valid_to_idx;


--
-- Name: contributions_latest_point_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_latest_point_changeset_id_idx;


--
-- Name: contributions_latest_point_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_latest_point_geom_idx;


--
-- Name: contributions_latest_point_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_latest_point_geom_type_idx;


--
-- Name: contributions_latest_point_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_latest_point_osm_type_osm_id_idx;


--
-- Name: contributions_latest_point_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_latest_point_status_idx;


--
-- Name: contributions_latest_point_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_latest_point_tags_idx;


--
-- Name: contributions_latest_point_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_latest_point_valid_from_idx;


--
-- Name: contributions_latest_point_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_latest_point_valid_to_idx;


--
-- Name: contributions_latest_polygon_multipolygon_changeset_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_changeset_id_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_changeset_id_idx;


--
-- Name: contributions_latest_polygon_multipolygon_geom_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_geom_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_geom_idx;


--
-- Name: contributions_latest_polygon_multipolygon_geom_type_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__geom_type__idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_geom_type_idx;


--
-- Name: contributions_latest_polygon_multipolygon_osm_type_osm_id_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_osm_type_osm_id_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_osm_type_osm_id_idx;


--
-- Name: contributions_latest_polygon_multipolygon_status_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions___status_geom_type__status__idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_status_idx;


--
-- Name: contributions_latest_polygon_multipolygon_tags_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_tags_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_tags_idx;


--
-- Name: contributions_latest_polygon_multipolygon_valid_from_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_from_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_valid_from_idx;


--
-- Name: contributions_latest_polygon_multipolygon_valid_to_idx; Type: INDEX ATTACH; Schema: next; Owner: -
--

ALTER INDEX next.contributions_valid_to_idx ATTACH PARTITION next.contributions_latest_polygon_multipolygon_valid_to_idx;


--
-- PostgreSQL database dump complete
--

\unrestrict XgIUnZ9H1gPOuAz9u1vXw2O69ru7J5Zafw1pq3HN4NRfmZI4lUsG6kzTSfrqxUo

