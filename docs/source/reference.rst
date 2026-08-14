.. Reference guides are technical descriptions of the machinery and how to
   operate it. Reference material is information-oriented.
   https://diataxis.fr/

Reference
=========

Area of Interest (AOI)
----------------------

The ``aoi`` parameter allows to control the spatial extent of the request.

It can be defined in different formats: GeoJSON Geometry, Bounding Box (BBOX) or Well Known Text (WKT).

You should pass only a single ``Polygon`` or ``MultiPolygon`` geometry.

All coordinates must be in WGS84 EPSG:4326 (longitude, latitude) format.
For more details see: https://epsg.io/4326

Bounding Box
^^^^^^^^^^^^

An array of coordinates in this order:

* xmin, ymin, xmax, ymax
* lon_min, lat_min, lon_max, lat_max

Example:

.. code-block:: json

    "aoi": [8.68812,49.4039,8.72362,49.41582]


Well Known Text
^^^^^^^^^^^^^^^

Geometry as a standard text string.

Example:

.. code-block:: json

    "aoi": "POLYGON ((8.68812 49.4039, 8.72362 49.4039, 8.72362 49.41582, 8.68812 49.41582, 8.68812 49.4039))"



GeoJSON Geometry
^^^^^^^^^^^^^^^^

This is defined as Geometry object.
You can't pass a GeoJSON Feature or FeatureCollection.
For further details see: https://datatracker.ietf.org/doc/html/rfc7946#section-3.1

Example:

.. code-block:: json

    "aoi":  {
        "type": "Polygon",
        "coordinates": [
          [
            [
              8.68812,
              49.4039
            ],
            [
              8.72362,
              49.4039
            ],
            [
              8.72362,
              49.41582
            ],
            [
              8.68812,
              49.41582
            ],
            [
              8.68812,
              49.4039
            ]
          ]
        ]
    }



Time
----

The ``time`` parameter allows to control the temporal extent of the request.

Depending on the endpoint this temporal extent is defined differently:
as single timestamp, time series, time bins or time range.

Timestamp
^^^^^^^^^

A single point in time (snapshot) of OSM data.


.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``time``
     - Single timestamp.
       Earliest allowed: ``2007-10-08``.
       Shorthand: ``earliest`` or ``latest``.
     - ISO-8601 (UTC)

Examples:

.. code-block:: json

    "time": "2007-10-08T00:00:00Z"

.. code-block:: json

    "time": "latest"


Time Range
^^^^^^^^^^

.. code-block:: text

   Timerange: [===]
   The range contains all OSM data for its temporal duration

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - ISO-8601 (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - ISO-8601 (UTC)

Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
      }


Time Series
^^^^^^^^^^^

.. code-block:: text

   Timeseries: *----*-----*-----*
   Snapshots:  1    2     3     4
   Each * is a snapshot of OSM data at that point in the series
   Number of points are restricted to a maximum of 10000.

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - ISO-8601 (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - ISO-8601 (UTC)
   * - ``interval``
     - Temporal duration between each point in the series (snapshot).
     - ISO-8601 duration


Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
        "interval": "P1M"
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
        "interval": "P1Y"
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
        "interval": "P1M"
      }


Time Bins
^^^^^^^^^

.. code-block:: text

   Timebins: [===][===][===][===]
   Bins:       1    2    3    4
   Each bin contains all OSM data for its temporal duration
   Number of bins are restricted to a maximum of 10000.

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - ISO-8601 (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - ISO-8601 (UTC)
   * - ``binSize``
     - Temporal duration of each bin:
     - ISO-8601 duration

Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
        "binSize": "P1M"
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
        "binSize": "P1Y"
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
        "binSize": "P1M"
      }


Filter
------

The ``filter`` parameter combines the following: definition of the OSM type,
the geometry (simple feature) type, as well as the OSM tag. The filter syntax is defined in textual form. 
A filter expression can be composed out of several actual filters, which are combined with boolean operators and parentheses.


Selectors
^^^^^^^^^

.. table::
    :widths: 24 50 24

    +------------------------------------+------------------------------------+-----------------------------------+
    |                                    | **description**                    | **example**                       |
    +====================================+====================================+===================================+
    | ``key=value``                      | matches all entities which         | ``natural=tree``                  |
    |                                    | have this exact tag                |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``key=*``                          | matches all entities which have    | ``addr:housenumber=*``            |
    |                                    | any tag with the given key         |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``key!=value``                     | matches all entities               | ``oneway!=yes``                   |
    |                                    | which do not have                  |                                   |
    |                                    | this exact tag                     |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``key!=*``                         | matches all entities which do not  | ``name!=*``                       |
    |                                    | have any tag with the given key    |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``key in (value list)``            | matches all entities which do      | ``highway in``                    |
    |                                    | have any tag with the given key    | ``(residential,                   |
    |                                    | and one of the given values        | living_street)``                  |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``type:osm-type``                  | matches all entities of the        | ``type:node``                     |
    |                                    | given osm type                     |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:osm-id``                      | matches all entities with the      | ``id:1234``                       |
    |                                    | given osm id [1]_                  |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:osm-type/osm-id``             | matches the entity with the given  | ``id:node/1234``                  |
    |                                    | osm type and id                    |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:(osm-id list)``               | matches all entities with the      | ``id:(1, 42, 1234)``              |
    |                                    | given osm ids [1]_                 |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:(osm-type/osm-id list)``      | matches all entities with the      | ``id:(node/1, way/3)``            |
    |                                    | given osm types and ids            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:(id range)``                  | matches all entities with an id    | ``id:(1 .. 9999)``                |
    |                                    | matching the given id range [2]_   |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry:geom-type``             | matches anything which has a       | ``geometry:polygon``              |
    |                                    | geometry of the given type         |                                   |
    |                                    | (point, line, polygon, or          |                                   |
    |                                    | collection)                        |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``area:(from..to)``                | matches features with a geometry   | ``area:(1.0 .. 1E6)``             |
    |                                    | having an area (measured in m²)    |                                   |
    |                                    | in the given range [2]_            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``length:(from..to)``              | matches features with a geometry   | ``length:( .. 100)``              |
    |                                    | having a length (measured in m)    |                                   |
    |                                    | in the given range [2]_            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``changeset:id``                   | matches contributions [3]_         | ``changeset:42``                  |
    |                                    | performed in the specified         |                                   |
    |                                    | changeset                          |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``changeset:(id list)``            | matches contributions [3]_         | ``changeset:(10, 42)``            |
    |                                    | performed in one of the            |                                   |
    |                                    | specified changesets               |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``changeset:(from..to)``           | matches contributions [3]_         | ``changeset:(10..42)``            |
    |                                    | performed in a range of            |                                   |
    |                                    | changesets                         |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
.. [1] Keep in mind that osm ids are not unique between osm types. In order to include only a specific object the id needs to be used together with an osm type filter. Alternatively, one can also use the combined type+id filter (e.g. `id:node/1234`).
.. [2] The lower or upper bound of a range may be omitted to indicate that the values are only to be limited to be "up to" or "starting from" the given value, respectively. For example: `id:(10..)` will accept all entities with an id of 10 or higher.
.. [3] The `changeset` filters can only be used in `contribution` based API endpoints.

|

Operators
^^^^^^^^^

.. table::
    :widths: 24 50 24

    +------------------------+------------------------------------+------------------------+
    |                        | **description**                    | **example**            |
    +========================+====================================+========================+
    | ``(…)``                | can be used to change              | ``highway=primary and  |
    |                        | precedence of operators            | (name=* or ref=*)``    |
    +------------------------+------------------------------------+------------------------+
    | ``not X``              | negates the following filter       | ``not type:node``      |
    |                        | expression                         |                        |
    +------------------------+------------------------------------+------------------------+
    | ``X and Y``            | returns entities which match       | ``highway=service and  |
    |                        | both filter expressions X and Y    | service=driveway``     |
    +------------------------+------------------------------------+------------------------+
    | ``X or Y``             | returns entities which match at    | ``natural=wood or      |
    |                        | least one of the filter            | landuse=forest``       |
    |                        | expressions X or Y                 |                        |
    +------------------------+------------------------------------+------------------------+

Operators follow the following order of precedence: parentheses before ``not``, before ``and``, before ``or``.

|

Special Characters & Whitespace
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

| When writing filters, tags without special characters can be supplied directly, without needing 
  to quote them. Example: ``amenity=drinking_water`` or ``name:it=*``. 
| Allowed characters are: the letters ``a-z`` and ``A-Z``, digits, underscore, dashes and colons.
  When filtering by tags with any other characters in their key or value, these strings need to be supplied as
  double-quoted strings, e.g. ``name="Heidelberger BrÃ¼ckenaffe"`` or ``opening_hours="24/7"``. Escape sequences can be used to
  represent a literal double-quote character ``\"``, while a literal backslash is written as ``\\``.


Whitespace such as spaces, tabs or newlines can be put freely between operators or parts of selectors (``name = *`` is
equivalent to ``name=*``) to make a filter more readable.

|

Examples
^^^^^^^^^

Here's some useful examples for querying some OSM features:

.. table::
    :widths: 24 34 34

    +------------------+--------------------------------------------------------+------------------------------+
    | **OSM Feature**  | **filter**                                             | **comment**                  |
    +==================+========================================================+==============================+
    | forests/woods    | | ``(landuse=forest or natural=wood) and``             | Using                        |
    |                  | | ``geometry:polygon``                                 | ``geometry:polygon`` will    |
    |                  |                                                        | select closed ways as        |
    |                  |                                                        | well as multipolygons        |
    |                  |                                                        | (e.g. a forest with          |
    |                  |                                                        | clearings).                  |
    +------------------+--------------------------------------------------------+------------------------------+
    | parks and        | | ``leisure=park and geometry:polygon or``             | A filter can also fetch      |
    | park benches     | | ``amenity=bench and (geometry:point or``             | features of different        |
    |                  | | ``geometry:line)``                                   | geometry types: this         |
    |                  |                                                        | returns parks                |
    |                  |                                                        | (polygons) as well as        |
    |                  |                                                        | park benches (points or      |
    |                  |                                                        | lines).                      |
    +------------------+--------------------------------------------------------+------------------------------+
    | buildings        | | ``building=* and building!=no and``                  | This filter also             |
    |                  | | ``geometry:polygon``                                 | excludes the (rare)          |
    |                  |                                                        | objects marked with          |
    |                  |                                                        | ``building=no``, which is    |
    |                  |                                                        | a tag used to indicate       |
    |                  |                                                        | that a feature might be      |
    |                  |                                                        | expected to be a             |
    |                  |                                                        | building (e.g. from an       |
    |                  |                                                        | outdated aerial imagery      |
    |                  |                                                        | source), but is in reality   |
    |                  |                                                        | not one.                     |
    +------------------+--------------------------------------------------------+------------------------------+
    | highways         | | ``type:way and (highway in (motorway,``              | The list of used tags        |
    |                  | | ``motorway_link, trunk, trunk_link,``                | depends on the exact         |
    |                  | | ``primary, primary_link, secondary,``                | definition of a              |
    |                  | | ``secondary_link, tertiary,``                        | "highway". In a              |
    |                  | | ``tertiary_link, unclassified,``                     | different context, it may    |
    |                  | | ``residential, living_street, pedestrian)``          | also include less or even    |
    |                  | | ``or (highway=service and service=alley))``          | more tags                    |
    |                  |                                                        | (``footway``, ``cycleway``,  |
    |                  |                                                        | ``track``, ``path``, all     |
    |                  |                                                        | ``highway=service``, etc.)   |
    +------------------+--------------------------------------------------------+------------------------------+
    | residential      | | ``type:way and highway=residential and``             | Note that some roads         |
    | roads missing    | | ``name!=* and noname!=yes``                          | might be actually            |
    | a name (for      |                                                        | unnamed in reality.          |
    | quality          |                                                        | Such features can be         |
    | assurance)       |                                                        | marked as unnamed            |
    |                  |                                                        | with the ``noname`` tag      |
    |                  |                                                        | in OSM.                      |
    +------------------+--------------------------------------------------------+------------------------------+
    | implausibly      | | ``geometry:polygon and building=* and``              | The currently largest        |
    | large            | | ``building!=no and area:(1E6..)``                    | building by footprint area   |
    | buildings        |                                                        | is a car factory building    |
    |                  |                                                        | measuring about 887,800 m².  |
    +------------------+--------------------------------------------------------+------------------------------+
     
|

Further Information
^^^^^^^^^^^^^^^^^^^

The filter is expressed as a ANTLR grammar and a Python based parser is used to interpret a given filer.
You can find further information in the `Readme of the *ohsome-filter-to-sql* library <https://github.com/GIScience/ohsome-filter-to-sql>`_.

