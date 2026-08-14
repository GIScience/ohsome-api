.. Explanation is a discursive treatment of a subject, that permits reflection.
   Explanation is understanding-oriented.
   https://diataxis.fr/

Explanation
===========

The ohsome API categorizes OSM elements by their Simple Features Geometry types, not by OSM types.


.. list-table::
   :header-rows: 1
   :widths: 20 20 20 20 20

   * - OSM Type
     - Category
     - Geometry Type
     - ohsome filter
     - example
   * - Node
     - Feature
     - Point
     - geometry:point
     - `node/4540889804`_
   * - Way
     - Feature
     - Point
     - geometry:poing
     - 123
   * - Way
     - Feature
     - Linestring
     - geometry:line
     - `way/721933838`_
   * - Way
     - Feature
     - Polygon
     - geometry:polygon
     - `way/27426509`_
   * - Relation
     - Feature
     - MultiPolygon
     - geometry:polygon
     - `relation/9998694`_
   * - Relation
     - Collection
     - GeometryCollection
     - geometry:collection
     - `relation/3123494`_


.. _node/4540889804: https://www.openstreetmap.org/node/4540889804
.. _way/721933838: https://www.openstreetmap.org/way/721933838
.. _way/27426509: https://www.openstreetmap.org/way/27426509
.. _relation/9998694: https://www.openstreetmap.org/relation/9998694
.. _relation/3123494: https://www.openstreetmap.org/relation/3123494

Features
--------
Contains of OSM nodes and ways that have any tag.
Additionally, OSM relations tagged as type=multipolygon or type=boundary are included.

This can be Points, Linestrings, Polygons and MultiPolygons.


Collections
-----------
OSM relations not tagged as type=multipolygon or type=boundary.

This is a GeometryCollection.

For each relation a separate row is returned for their linear, polygonal or point members.


Collections Members
-------------------

OSM ways and nodes that are members of OSM relations (not tagged as type=multipolygon or type=boundary).

For each relation all members features are returned including their metadata (e.g. role or position).





Contributions
-------------

This contains deletions and their metadata (e.g. OSM user_id or changeset_id).

