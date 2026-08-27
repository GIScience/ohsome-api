.. Explanation is a discursive treatment of a subject, that permits reflection.
   Explanation is understanding-oriented.
   https://diataxis.fr/explanation

Explanation
===========

Features & Collections
----------------------

The ohsome API categorizes OSM elements by their Simple Features Geometry types, not by OSM types.

.. list-table::
   :header-rows: 1
   :widths: 15 20 20 20 30

   * - Category
     - OSM Type
     - Geometry Type
     - ohsome Filter
     - Example
   * - Feature
     - node
     - Point
     - ``geometry:point``
     - `node/4540889804`_
   * - Feature
     - way
     - Point
     - ``geometry:point``
     - *this is a rare edge case*
   * - Feature
     - way
     - LineString
     - ``geometry:line``
     - `way/721933838`_
   * - Feature
     - way
     - Polygon
     - ``geometry:polygon``
     - `way/27426509`_
   * - Feature
     - relation [1]_
     - MultiPolygon
     - ``geometry:polygon``
     - `relation/9998694`_
   * - Collection
     - relation [2]_
     - GeometryCollection
     - ``geometry:collection``
     - `relation/3123494`_


.. _node/4540889804: https://www.openstreetmap.org/node/4540889804
.. _way/721933838: https://www.openstreetmap.org/way/721933838
.. _way/27426509: https://www.openstreetmap.org/way/27426509
.. _relation/9998694: https://www.openstreetmap.org/relation/9998694
.. _relation/3123494: https://www.openstreetmap.org/relation/3123494
.. [1] OSM relations tagged as ``type=multipolygon`` or ``type=boundary``
.. [2] OSM relations **not** tagged as ``type=multipolygon`` or ``type=boundary``


Features
--------

Features are OSM nodes and ways that have at least one tag.
Additionally, OSM relations tagged as ``type=multipolygon`` or ``type=boundary`` are included.

Features are one of the Simple Features Geometry types Points, Linestrings, Polygons or MultiPolygons.

Features have a lifespan (``[edit_timestamp, valid_to_timestamp]``).
During that lifespan they are visible on the map.
The ``edit_timestamp`` is the moment when the feature became visible on the map.
The ``valid_to_timestamp`` is the moment when the feature vanished from the map.

Features vs. OSM Elements
^^^^^^^^^^^^^^^^^^^^^^^^^

Contrasted with plain OSM elements, Features are the ohsome API's own abstraction:
they group nodes, ways and eligible relations by geometry rather than by raw OSM type.


.. _Collections:

Collections
-----------

Collections are OSM relations not tagged as ``type=multipolygon`` or ``type=boundary``.

Collections are GeometryCollections containing any of the Simple Feature Geometry types mentioned for Features (see above).

For each OSM relation a separate GeometryCollection is returned for their linear, polygonal or point members.


Collection Members
------------------

Collection Members are OSM ways and nodes that are members of OSM relations not tagged as ``type=multipolygon`` or ``type=boundary`` (Collections).

A single Collection contains only the geometries of its members as a GeometryCollection (see :ref:`Collections`), but not their attributes such as OSM tags.
To get each individual member including geometry and all other attributes such as OSM tags, Collection Members must be requested instead.


Contributions
-------------

Contributions happen at a single point in time (``edit_timestamp``).

Contributions describe changes to OSM nodes and ways that have at least one tag.
Additionally, changes to OSM relations tagged as ``type=multipolygon`` or ``type=boundary`` are included.

Contributions contain additional metadata such as changeset information and tags of the OSM element before the change.
They also contain information about deleted OSM elements (see the ``contributions_type`` attribute).

Contributions vs. Features
^^^^^^^^^^^^^^^^^^^^^^^^^^

Use Contributions if you are interested in mapping activity (i.e. all the road edits within a time range).
Use Features if you are interested in all visible OSM elements at given points in time, regardless of their ``edit_timestamp``.


Contributors
------------

Contributors are OSM users that have made an edit, change or contribution to the OSM database.

OSM Users that created an OSM account, but never made any edit are not included.

