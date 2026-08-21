.. reference guides are technical descriptions of the machinery and how to
   operate it. reference material is information-oriented.
   https://diataxis.fr/

Data Model (Extraction)
-----------------------

The ohsome API provides data extracts in Parquet format.


Features Extraction Schema
^^^^^^^^^^^^^^^^^^^^^^^^^

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
   * - ``osm_version``
     - ``INT32``
     - Version number as reported by the OSM API; does not reflect every geometric change.
   * - ``minor_version``
     - ``INT32``
     - Minor version that increments with every geometric change; resets to 0 at each new major (OSM API) version.
   * - ``osm_edits``
     - ``INT32``
     - Running total of all edits/minor versions made to this element.
   * - ``osm_user_id``
     - ``INT32``
     - Identifier of the OSM contributor that made the edit.
   * - ``osm_user_name``
     - ``BYTE_ARRAY (STRING)``
     - Username of the OSM contributor at the time of the data extract.
   * - ``osm_changeset_id``
     - ``INT64``
     - Identifier of the OSM changeset this edit belongs to.
   * - ``osm_tags``
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


Collections
^^^^^^^^^^^




Collections Members
^^^^^^^^^^^^^^^^^^^



Contributions
^^^^^^^^^^^^^

