Filter
======

The filter parameter combines the following: definition of the OSM type, 
the geometry (simple feature) type, as well as the OSM tag. The filter syntax is defined in textual form. 
A filter expression can be composed out of several actual filters, which are combined with boolean operators and parentheses.

.. note:: Please note that you **cannot combine**
          the filter parameter with the **deprecated types, keys and values** parameters.

|

Selectors
---------

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
    | ``id:(id list)``                   | matches all entities with the      | ``id:(1, 42, 1234)``              |
    |                                    | given osm ids [1]_                 |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:(id list)``                   | matches all entities with the      | ``id:(node/1, way/3)``            |
    |                                    | given osm types and ids            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``id:(id range)``                  | matches all entities with an id    | ``id:(1 .. 9999)``                |
    |                                    | matching the given id range [2]_   |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry:geom-type``             | matches anything which has a       | ``geometry:polygon``              |
    |                                    | geometry of the given type         |                                   |
    |                                    | (point, line, polygon, or other)   |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``area:(from..to)``                | matches features with a geometry   | ``area:(1.0 .. 1E6)``             |
    |                                    | having an area (measured in m²)    |                                   |
    |                                    | in the given range [2]_            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``length:(from..to)``              | matches features with a geometry   | ``length:( .. 100)``              |
    |                                    | having a length (measured in m)    |                                   |
    |                                    | in the given range [2]_            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``perimeter:(from..to)``           | matches features with a            | ``perimeter:( .. 100)``           |
    |                                    | perimeter (measured in m) in the   |                                   |
    |                                    | given range [2]_                   |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry.vertices:(from..to)``   | matches features by the number     | ``geometry.vertices:(1 .. 10)``   |
    |                                    | of points they consists of         |                                   |
    |                                    | in the given range [2]_            |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry.outers:number``         | matches features by the number     | ``geometry.outers:1``             |
    | or                                 | of outer rings they consists of    | or                                |
    | ``geometry.outers:(from..to)``     | in the given range [2]_            | ``geometry.outers:(2 .. )``       |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry.inners:number``         | matches features by the number     | ``geometry.inners:0``             |
    | or                                 | of holes (inner rings) they        | or                                |
    | ``geometry.inners:(from..to)``     | consists of in the given range     | ``geometry.inners:(1 .. )``       |
    |                                    | [2]_                               |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry.roundness:(from..to)``  | matches features which have a      | ``geometry.roundness:(0.8 .. )``  |
    |                                    | *roundness* (or *compactness*)     |                                   |
    |                                    | in the given range [2]_ [4]_       |                                   |
    +------------------------------------+------------------------------------+-----------------------------------+
    | ``geometry.squareness:(from..to)`` | matches features which have a      | ``geometry.squareness:(0.8 .. )`` |
    |                                    | *squareness*                       |                                   |
    |                                    | in the given range [2]_ [5]_       |                                   |
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
.. [4] This is using the `"Polsby–Popper test" score`_ where all values fall in the interval 0 to 1 and 1 represents a perfect circle.
.. [5] This is using the `rectilinearity measurement by Žunić and Rosin`_ where all values fall in the interval 0 to 1 and 1 represents a perfectly rectilinear geometry.
.. _"Polsby–Popper test" score: https://en.wikipedia.org/wiki/Polsby%E2%80%93Popper_test
.. _rectilinearity measurement by Žunić and Rosin: https://www.researchgate.net/publication/221304067_A_Rectilinearity_Measurement_for_Polygons)

|

Operators
---------

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
-------------------------------

| When writing filters, tags without special characters can be supplied directly, without needing 
  to quote them. Example: ``amenity=drinking_water`` or ``name:it=*``. 
| Allowed characters are: the letters ``a-z`` and ``A-Z``, digits, underscore, dashes and colons.
  When filtering by tags with any other characters in their key or value, these strings need to be supplied as
  double-quoted strings, e.g. ``name="Heidelberger Brückenaffe"`` or ``opening_hours="24/7"``. Escape sequences can be used to
  represent a literal double-quote character ``\"``, while a literal backslash is written as ``\\``.


Whitespace such as spaces, tabs or newlines can be put freely between operators or parts of selectors (``name = *`` is
equivalent to ``name=*``) to make a filter more readable.

|

Examples
--------

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
-------------------

The filter parameter is powered by a separate Java module, which can be used independently as a maven library.
You can find further information in the `Readme of the *oshdb-filter* module <https://github.com/GIScience/oshdb/tree/master/oshdb-filter>`_.
