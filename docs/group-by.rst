Grouping
========
The grouping end points are available for every aggregation resource (count, length, perimeter, 
area). They are accessed through an additional /groupBy/(grouping-type) resource. Group types are ``boundary``, ``key``, ``tag`` and ``type``.

.. note:: For /elements/(aggregation)/**density** the group types ``boundary``, ``tag`` and ``type`` are available.

 For **POST requests** the fields are given analogous to **GET requests**. When you just have a smaller set of spatial parameters, a GET request fits perfectly. POST mostly makes sense when you start to use GeoJSON as input geometries.
    
|

Boundary
--------
Groups the result by the given boundaries that are defined through any of the boundary query 
parameters.

.. http:post :: /elements/(aggregation)/groupBy/boundary

  :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
  :query <params>: see query-parameters_ at /elements(aggregation) endpoint

.. _query-parameters: endpoints.html#post--elements-(aggregation)

|

Key
----
Groups the result by the given keys that are defined through the ``groupByKeys`` query parameter.

.. http:post :: /elements/(aggregation)/groupBy/key

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKeys: OSM key(s) given as a list and combined with the ‘AND’ operator; default: empty;
.. _query-parameters: endpoints.html#post--elements-(aggregation)


.. note:: The ``groupByKeys`` query parameter is only available for the /groupBy/key endpoint.

.. note:: The /groupBy/key endpoint is **not** available for /elements/(aggregation)/**density**.

|

Tag
----
Groups the result by the given tags that are defined through the ``groupByKey`` and 
``groupByValues`` query parameters.

.. http:post :: /elements/(aggregation)/groupBy/tag

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKey: OSM key(s) given as a list and combined with the ‘AND’ operator, e.g.: 'highway’, 'building’; (one groupByKey parameter must be defined)
    :query groupByValues: OSM value(s) given as a list and combined with the ‘AND’ operator, default: no value
.. _query-parameters: endpoints.html#post--elements-(aggregation)


|

Type
-----
Groups the result by the given OSM, or simple feature types that are defined through the ``types`` 
parameter.

.. http:post :: /elements/(aggregation)/groupBy/type

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
.. _query-parameters: endpoints.html#post--elements-(aggregation)

|

Tag and Boundary
----------------
Groups the result by the given boundary and the tags.

.. http:post :: /elements/(aggregation)/groupBy/boundary/groupBy/tag

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKey: OSM key(s) given as a list and combined with the ‘AND’ operator, e.g.: 'highway’, 'building’; (one groupByKey parameter must be defined)
    :query groupByValues: OSM value(s) given as a list and combined with the ‘AND’ operator, default: no value
.. _query-parameters: endpoints.html#post--elements-(aggregation)

