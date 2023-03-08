Grouping
========
The grouping end points are available for every aggregation resource (count, length, perimeter, 
area). They are accessed through an additional /groupBy/(grouping-type) resource. Group types are ``boundary``, ``key``, ``tag`` and ``type``.

.. note:: For /elements/(aggregation)/**density** the group types ``boundary``, ``tag`` and ``type`` are available.

 For **POST requests** the fields are given analogous to **GET requests**. When you just have a smaller set of spatial parameters, a GET request fits perfectly. POST mostly makes sense when you start to use GeoJSON as input geometries.

Boundary
--------
Groups the result by the given boundaries that are defined through any of the boundary query 
parameters.

.. http:post :: /elements/(aggregation)/groupBy/boundary

  :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
  :query <params>: see query-parameters_ at /elements(aggregation) endpoint

Key
----
Groups the result by the given keys that are defined through the ``groupByKeys`` query parameter.

.. http:post :: /elements/(aggregation)/groupBy/key

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKeys: OSM key(s) given as a comma separated list (e.g. "key1,key2"), mandatory, no default value

The result of this grouping will contain separate results for each key specified in ``groupByKeys`` and - if applicable - a ``remainder`` for all objects that fulfilled the filter but had none of the 'groupByKeys'. Note that if more than one key is defined in ``groupByKeys``, the sum of the result values can be higher than the total result of a simple (not-grouped) aggregation: this is because an element can match none, one, or multiple of the specified ``groupByKeys``.

Here you can find a groupByKey_ example.

.. note:: The ``groupByKeys`` query parameter is only available for the /groupBy/key endpoint.

.. note:: The /groupBy/key endpoint is **not** available for /elements/(aggregation)/**density**.

Tag
----
Groups the result by the given tags that are defined through the ``groupByKey`` and 
``groupByValues`` query parameters.

.. http:post :: /elements/(aggregation)/groupBy/tag

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKey: OSM key e.g.: 'highway’; mandatory, no default value, only one groupByKey can be defined
    :query groupByValues: OSM value(s) for the specified key given as a comma separated list (e.g. "value1,value2"), default: no value

The result of this grouping will contain separate results for each specified tag having the key defined in ``groupByKey`` and a value as given in ``groupByValue``. A ``remainder`` object contains results for all elements which do not match any of these tags, if there are any. If the ``groupByValues`` parameter is left empty, all occuring tag values which are found in the filtered OSM data will be present in the output.

Here you can find a groupByTag_ example. 

.. note:: The ``groupByKey`` and the ``groupByValues`` query parameters are only available for the /groupBy/tag endpoint.

Type
-----
Groups the result by the given OSM, or simple feature types that are defined through the ``types`` 
parameter.

.. http:post :: /elements/(aggregation)/groupBy/type

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint

Boundary and Tag
----------------
Groups the result by the given boundary and the tags.

.. http:post :: /elements/(aggregation)/groupBy/boundary/groupBy/tag

    :param aggregation: aggregation type, one of ``area``, ``count``, ``length``, ``perimeter``
    :query <params>: see query-parameters_ at /elements(aggregation) endpoint
    :query groupByKey: OSM key e.g.: 'highway’; mandatory, no default value, only one groupByKey can be defined
    :query groupByValues: OSM value(s) for the specified key given as a comma separated list (e.g. "value1,value2"), default: no value

The result of this grouping will contain separate results for each conbination of boundary and OSM tag. The semantics are otherwise the same as for the individual groupings described above.

Here you can find a groupByBoundaryGroupByTag_ example.

.. _groupByTag: endpoints.html#post--elements-(aggregation)-groupBy-boundary-groupBy-tag
.. _groupByBoundaryGroupByTag: endpoints.html#post--elements-(aggregation)-groupBy-boundary-groupBy-tag
.. _query-parameters: endpoints.html#post--elements-(aggregation)
.. _groupByKey: endpoints.html#post--elements-(aggregation)-groupBy-(groupType)
