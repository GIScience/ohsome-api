.. reference guides are technical descriptions of the machinery and how to
   operate it. reference material is information-oriented.
   https://diataxis.fr/

Data Model (Extraction)
-----------------------

The ohsome API provides data extracts in Parquet format.

Read more about the Parquet file format here: https://parquet.apache.org/docs/file-format/

Parquet provides a ``Geospatial Type``. This type is used to store all geometries.
You can read more about this here: https://parquet.apache.org/docs/file-format/types/geospatial/


Features / Collections Extraction Schema
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This is the schema you get from the following endpoints:

* ``/extraction/features.parquet``
* ``/extraction/collections.parquet``



.. list-table::
   :header-rows: 1
   :widths: 20 25 45

   * - Column
     - Type
     - Description
   * - ``osm_type``
     - ``BYTE_ARRAY (STRING)``
     - The OSM element type: node, way, or relation.
   * - ``osm_id``
     - ``INT64``
     - Identifier of the OSM element.
   * - ``edit_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version of the feature was uploaded.
   * - ``valid_to_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version was superseded by a newer edit. Set to a far-future placeholder (2222-01-01) if the version is still current or was deleted.
   * - ``version``
     - ``INT32``
     - Version number as reported by the OSM API; does not reflect every geometric change.
   * - ``minor_version``
     - ``INT32``
     - Minor version that increments with every geometric change; resets to 0 at each new major (OSM API) version.
   * - ``edits``
     - ``INT32``
     - Running total of all edits/minor versions made to this element.
   * - ``user_id``
     - ``INT32``
     - Identifier of the OSM contributor that made the edit.
   * - ``user_name``
     - ``BYTE_ARRAY (STRING)``
     - Username of the OSM contributor at the time of the data extract.
   * - ``changeset_id``
     - ``INT64``
     - Identifier of the OSM changeset this edit belongs to.
   * - ``tags``
     - ``group (MAP), repeated group key_value {BYTE_ARRAY (STRING) key; BYTE_ARRAY (STRING) value;}``
     - Key/value tag pairs for this OSM element; empty map if none exist.
   * - ``bbox``
     - ``group (STRUCT) {DOUBLE xmin; DOUBLE xmax; DOUBLE ymin; DOUBLE ymax;}``
     - Bounding box of the element's geometry, in decimal degrees (WGS84).
   * - ``geom_type``
     - ``BYTE_ARRAY (STRING)``
     - Geometry type: Point, LineString, Polygon, or MultiPolygon.
   * - ``geom``
     - ``BYTE_ARRAY``
     - Geometry encoded as Well-Known Binary (WKB).
   * - ``clipped``
     - ``BOOLEAN``
     - Indicates whether the geometry was clipped to the area-of-interest boundary.



Collections Members Extraction Schema
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This is the schema you get from the following endpoints:

* ``/extraction/collections_members.parquet``


The collections members schema has additional attributes, i.e. that provide information about the role of the member in the parent OSM relation.

Members that are part of more than one OSM relation will be duplicated.


.. list-table::
   :header-rows: 1
   :widths: 20 25 45

   * - Column
     - Type
     - Description
   * - ``osm_type``
     - ``BYTE_ARRAY (STRING)``
     - The OSM element type: node, way.
   * - ``osm_id``
     - ``INT64``
     - Identifier of the OSM element.
   * - ``edit_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version of the feature was uploaded.
   * - ``valid_to_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version was superseded by a newer edit. Set to a far-future placeholder (2222-01-01) if the version is still current or was deleted.
   * - ``version``
     - ``INT32``
     - Version number as reported by the OSM API; does not reflect every geometric change.
   * - ``minor_version``
     - ``INT32``
     - Minor version that increments with every geometric change; resets to 0 at each new major (OSM API) version.
   * - ``edits``
     - ``INT32``
     - Running total of all edits/minor versions made to this element.
   * - ``user_id``
     - ``INT32``
     - Identifier of the OSM contributor that made the edit.
   * - ``user_name``
     - ``BYTE_ARRAY (STRING)``
     - Username of the OSM contributor at the time of the data extract.
   * - ``changeset_id``
     - ``INT64``
     - Identifier of the OSM changeset this edit belongs to.
   * - ``tags``
     - ``group (MAP), repeated group key_value {BYTE_ARRAY (STRING) key; BYTE_ARRAY (STRING) value;}``
     - Key/value tag pairs for this OSM element; empty map if none exist.
   * - ``collection_osm_id``
     - ``INT64``
     - Identifier of the parent OSM relation that this element is a member of.
   * - ``member_role``
     - ``BYTE_ARRAY (STRING)``
     - Role describing the function this member plays within the parent relation, e.g. ``inner``/``outer`` for building multipolygons.
   * - ``member_pos``
     - ``INT32``
     - Position/index of this member within the parent relation's ordered member list.
   * - ``bbox``
     - ``group (STRUCT) {DOUBLE xmin; DOUBLE xmax; DOUBLE ymin; DOUBLE ymax;}``
     - Bounding box of the element's geometry, in decimal degrees (WGS84).
   * - ``geom_type``
     - ``BYTE_ARRAY (STRING)``
     - Geometry type: Point, LineString, Polygon, or MultiPolygon.
   * - ``geom``
     - ``BYTE_ARRAY``
     - Geometry encoded as Well-Known Binary (WKB).
   * - ``clipped``
     - ``BOOLEAN``
     - Indicates whether the geometry was clipped to the area-of-interest boundary.



Contributions Extraction Schema
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This is the schema you get from the following endpoints:

* ``/extraction/contributions.parquet``


The contributions schema has additional attributes, i.e. that provide information about the changeset tags.
Each contribution also contains information on the OSM elements tags prior to this edit.

For contributions we never return clipped geometries.

.. list-table::
   :header-rows: 1
   :widths: 20 25 45

   * - Column
     - Type
     - Description
   * - ``osm_type``
     - ``BYTE_ARRAY (STRING)``
     - The OSM element type: node, way, or relation.
   * - ``osm_id``
     - ``INT64``
     - Identifier of the OSM element.
   * - ``edit_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version of the feature was uploaded.
   * - ``valid_to_timestamp``
     - ``INT64 (TIMESTAMP(isAdjustedToUTC=true, unit=MICROS))``
     - Timestamp (UTC) when this version was superseded by a newer edit. Set to a far-future placeholder (2222-01-01) if the version is still current or was deleted.
   * - ``version``
     - ``INT32``
     - Version number as reported by the OSM API; does not reflect every geometric change.
   * - ``minor_version``
     - ``INT32``
     - Minor version that increments with every geometric change; resets to 0 at each new major (OSM API) version.
   * - ``edits``
     - ``INT32``
     - Running total of all edits/minor versions made to this element.
   * - ``user_id``
     - ``INT32``
     - Identifier of the OSM contributor that made the edit.
   * - ``user_name``
     - ``BYTE_ARRAY (STRING)``
     - Username of the OSM contributor at the time of the data extract.
   * - ``changeset_id``
     - ``INT64``
     - Identifier of the OSM changeset this edit belongs to.
   * - ``changeset_tags``
     - ``group (MAP), repeated group key_value {BYTE_ARRAY (STRING) key; BYTE_ARRAY (STRING) value;}``
     - Key/value tag pairs on the OSM changeset this edit belongs to; empty map if none exist.
   * - ``contribution_type``
     - ``BYTE_ARRAY (STRING)``
     - Type of change represented by this contribution: ``CREATION``, ``DELETION``, ``TAG``, ``GEOMETRY``, or ``TAG_GEOMETRY``.
   * - ``tags``
     - ``group (MAP), repeated group key_value {BYTE_ARRAY (STRING) key; BYTE_ARRAY (STRING) value;}``
     - Key/value tag pairs for this OSM element; empty map if none exist.
   * - ``tags_before``
     - ``group (MAP), repeated group key_value {BYTE_ARRAY (STRING) key; BYTE_ARRAY (STRING) value;}``
     - Key/value tag pairs for this OSM element prior to this edit; empty map if none exist.
   * - ``bbox``
     - ``group (STRUCT) {DOUBLE xmin; DOUBLE xmax; DOUBLE ymin; DOUBLE ymax;}``
     - Bounding box of the element's geometry, in decimal degrees (WGS84).
   * - ``geom_type``
     - ``BYTE_ARRAY (STRING)``
     - Geometry type: Point, LineString, Polygon, or MultiPolygon.
   * - ``geom``
     - ``BYTE_ARRAY``
     - Geometry encoded as Well-Known Binary (WKB).

