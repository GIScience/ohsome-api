Migrate from v1
===============

Version 2 of the ohsome API is a complete rewrite of the ohsome API
functionality, implemented in Python, and further also using a new
database backed.

The core idea remained the same, which is to allow you to inspect the
history of OpenStreetMap data in a flexible manner by generating
statistics of the data or by downloading data extracts. The new database
backend of v2 allows us to also ship data updates more frequently,
meaning that the results are more closely reflecting the current state
of the OpenStreetMap data.

For v2, we tried to consolidate the large amount of endpoints and
parameters of the previous version of the ohsome API to a condenset set
that is more comprehensible and easy to understand. At the same time we
want to continue to support existing functionality that was previously
frequently requested. For this we changed the API structure in a couple
of way, as explained below. Some new functionality is also intruced in
this version, some notable examples are also included in the tables
below.

If you are missing a particular feature in v2 that you used in the
previous version of the ohsome API, or a completely new feature, please
feel free to `reach out <mailto:ohsome@heigit.org>`__ with your use case
or open an
`issue <https://github.com/GIScience/ohsome-api/issues/new/choose>`__ on
github.

API Key
-------

One major change compared to the initial prototypes of the ohsome API is
as of v2, it is required to supply an API key with every request. You
can get an API key for free by `signing up
here <https://account.heigit.org/signup>`__ to access the ohsome API. If
you should need a larger quota than the free API tier allows, please
`contact us <mailto:ohsome@heigit.org>`__ with a description of your use
case.

The URLs for the ohsome API v2 have changed:

API endpoints root URL
    | **v1**: ``https://api.ohsome.org/v1/``
    | **v2**: ``https://api.heigit.org/ohsome-api/v2-rc/`` [1]_

API documentation
    | **v1**: ``https://api.ohsome.org/v1/swagger-ui.html``
    | **v2**: ``https://api.heigit.org/ohsome-api/v2-rc/docs`` [1]_

General reference documentation, how-to guides and explanations
    | **v1**: ``https://docs.ohsome.org/ohsome-api/v1/``
    | **v2**: ``https://docs.ohsome.org/ohsome-api/v2-rc/`` [1]_


.. important::

   The **v1** API endpoints will be shut down on **November 30, 2026**.



.. _statistics--aggregation-endpoints:

Statistics / Aggregation Endpoints
----------------------------------

Most endpoints now include the response file format as a suffix similar
to a “filename extension”. For example, a statistics endpoint ending in
``.json`` will return JSON data.


Paths
^^^^^

You find these now consistently under the ``/stats`` directory:

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - **v1**
     - **v2**
   * - ``/v1/elements/count``
     - ``/v2-rc/stats/features/count``\  [2]_
   * - ``/v1/elements/length``
     - ``/v2-rc/stats/features/length``
   * - ``/v1/elements/area``
     - ``/v2-rc/stats/features/area``
   * - ``/v1/elements/perimeter``
     - not available in **v2**
   * - ``/v1/contributions/count``
     - ``/v2-rc/stats/contributions/count``
   * - ``/v1/contributions/latest/count``
     - ``/v2-rc/stats/currentness/count``
   * - ``/v1/users/count``
     - ``/v2-rc/stats/contributors/count``
   * - ``/v1/…/density``
     - not available, can be calculated on client side
   * - ``/v1/…/ratio``
     - not available, can be acchieved by performing two requests and calculating the ratio on client side
   * - ``/v1/…/groupBy/tag``
     - see request parameter ``groupBy`` below
   * - ``/v1/users/count/groupBy/tag``
     - not available
   * - ``/v1/…/groupBy/key``
     - not available, instead perform one query for each key
   * - ``/v1/…/groupBy/boundary``
     - not available, instead perform one query for each area of interest
   * - ``/v1/…/groupBy/type``
     - not available, instead perform one query for each type


Request Parameters
^^^^^^^^^^^^^^^^^^

All of the endpoints are now only available as ``POST`` requests, with a
JSON body payload instead of the previous ``x-www-form-urlencoded``
parameters.


.. list-table::
   :header-rows: 1
   :widths: 25 25 50

   * - **v1**
     - **v2**
     - example
   * - ``bboxes``
     - ``aoi``
     - ``"aoi": [ 8.68812, 49.4039, 8.72362, 49.41582 ]``
   * - ``bpolys``
     - ``aoi``\  [3]_
     - ``"aoi": { "type": "Polygon", "coordinates": […] }``
   * - ``bcircles``
     - N/A
     -
   * - ``filter``
     - ``filter``
     - ``"filter": "natural=tree and geometry:point"``
   * - ``format``
     - N/A, this is part of the path (see below)
     -
   * - ``showMetadata``
     - N/A
     -
   * - ``time``
     - ``time``\  [4]_
     - ``"time": { "start": "2014-01-01", "end": "2026-01-01", "interval": "P1Y" }``
   * - ``timeout``
     - N/A
     -
   * - N/A
     - ``groupBy``
     - ``"groupBy": { "type": "byTag", "key": "species" }``
   * - N/A
     - ``clip``
     - ``"clip": true``




Result Formats
^^^^^^^^^^^^^^

The result formats ``.json`` and ``.csv`` are supported as suffixes in
the path, e.g. ``/stats/features/length.json`` or
``/stats/contributors/count.csv``.

**JSON** responses now use a columnar format:

**v1**:

.. code:: json

   {
     "apiVersion": "1.10.4",
     "attribution": {
       "url": "https://ohsome.org/copyrights",
       "text": "© OpenStreetMap contributors"
     },
     "result": [
       {
         "timestamp": "2025-01-01T00:00:00Z",
         "value": 26856
       },
       {
         "timestamp": "2026-01-01T00:00:00Z",
         "value": 33275
       }
     ]
   }

**v2**:

.. code:: json

   {
     "apiVersion": "2.0.0",
     "attribution": {
       "url": "https://ohsome.org/copyrights",
       "text": "© OpenStreetMap contributors"
     },
     "result": {
       "timestamp": [
         "2025-01-01T00:00:00Z",
         "2026-01-01T00:00:00Z"
       ],
       "value": [
         26856,
         33275
       ]
     }
   }

The **CSV** results have not changed much:

**v1**:

.. code:: text

   # Copyright URL: https://ohsome.org/copyrights
   # Copyright Text: © OpenStreetMap contributors
   # API Version: 1.10.4
   timestamp;value
   "2025-01-01T00:00:00Z";"26856"
   "2026-01-01T00:00:00Z";"33275"

**v2**:

.. code:: text

   # apiVersion: 2.0.0
   # attribution.url: https://ohsome.org/copyrights
   # attribution.text: © OpenStreetMap contributors
   timestamp;value
   2025-01-01T00:00:00Z;26856
   2026-01-01T00:00:00Z;33275


Extraction Endpoints
--------------------

You find these now consistently under the ``/extraction`` directory:

.. _paths-1:

Paths
^^^^^

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - **v1**
     - **v2**
   * - ``/v1/elements/geometry``
     - ``/v2-rc/extraction/features``
   * - ``/v1/elementsFullHistory/geometry``
     - ``/v2-rc/extraction/features``
   * - ``/v1/contributions/geometry``
     - ``/v2-rc/extraction/contributions``\  [6]_
   * - ``/v1/…/bbox``
     - N/A (bbox is always included in result alongside full geometry)
   * - ``/v1/…/centroid``
     - not yet implemented, can be calculated on client side in post-processing
   * - N/A
     - ``/v2-rc/extraction/collections``\  [7]_
   * - N/A
     - ``/v2-rc/extraction/collections_members``\  [7]_


.. _request-parameters-1:

Request Parameters
^^^^^^^^^^^^^^^^^^

.. list-table::
   :header-rows: 1
   :widths: 25 25 50

   * - **v1**
     - **v2**
     - example
   * - ``bboxes``
     - ``aoi``
     - ``"aoi": [ 8.68812, 49.4039, 8.72362, 49.41582 ]``
   * - ``bpolys``
     - ``aoi``\  [3]_
     - ``"aoi": { "type": "Polygon", "coordinates": […] }``
   * - ``bcircles``
     - N/A
     -
   * - ``filter``
     - ``filter``
     - ``"filter": "natural=tree and geometry:point"``
   * - ``properties``
     - N/A (all properties are always returned)
     -
   * - ``showMetadata``
     - N/A
     -
   * - ``time``
     - ``time``\  [4]_
     - ``"time": { "start": "2014-01-01", "end": "2026-01-01" }``
   * - ``timeout``
     - N/A
     -
   * - ``clipGeometry``
     - ``clip``
     - ``"clip": true``


In addition to ``POST`` request with a JSON body for the request
parameters, ``GET`` requests are also supported where all parameters are
query paramters. In that case, the ``aoi`` can only be a bounding box.

Result Format
^^^^^^^^^^^^^

The extraction endpoints now return geodata in
`GeoParquet <https://geoparquet.org/>`__ format, which is compact binary
format to store and distibute geo data. This can be used directly with
many tools, or converted to other formats like GeoJSON for further
processing.

See the `API
documentation <https://docs.ohsome.org/ohsome-api/v2-rc/reference/data_model.html>`__
for details about the new extraction data format.

Metadata Endpoints
~~~~~~~~~~~~~~~~~~


.. list-table::
   :header-rows: 1
   :widths: 25 25 50

   * - **v1**
     - **v2**
     - comment
   * - ``/v1/metadata``
     - ``/v2-rc/metadata``
     -
   * - N/A
     - ``/v2-rc/filter/validate``
     - checks whether the given filter is valid or returns a 422 Validation Error if not
   * - N/A
     - ``/v2-rc/health``
     - returns wheter the API is up and running


Filters
-------

The ohsome filter language remained largely the same between **v1** and
**v2**. Some minor differences are:


.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - **v1**
     - **v2**
   * - ``geometry:other``
     - ``geometry:collection``\  [8]_
   * - ``squaredness``
     - N/A
   * - ``roundness``
     - N/A
   * - ``perimeter``
     - N/A
   * - ``geometry.vertices``
     - not yet implemented
   * - N/A
     - ``key ~ prefix*`` newly available
   * - N/A
     - ``key ~ *suffix`` newly available
   * - N/A
     - ``key ~ *substring*`` newly available


Full Examples
-------------

curl
^^^^

**v1**:

.. code-block:: shell

   curl -X POST 'https://api.ohsome.org/v1/elements/count' \
     --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' \
     --data-urlencode 'format=csv' \
     --data-urlencode 'time=2014-01-01/2026-01-01/P1Y' \
     --data-urlencode 'filter=geometry:point and natural=tree'

**v2**:

.. code-block:: shell

   curl -X 'POST' 'https://api.heigit.org/ohsome-api/v2-rc/stats/features/count.csv' \
     -H 'Authorization: <your-api-key>' \
     -H 'Content-Type: application/json' \
     -d '{
       "filter": "geometry:point and natural=tree",
       "aoi": [ 8.625,49.3711,8.7334,49.4397 ],
       "time": {
           "start": "2014-01-01",
           "end": "2026-01-01",
           "interval": "P1Y"
       }
   }'

python
^^^^^^

**v1**:

.. code-block:: python

   import https
   OHSOME_API_URL = 'https://api.ohsome.org/v1'
   response = https.post(
       OHSOME_API_URL + /elements/count/groupBy/tag,
       data={
           "bboxes": "8.625,49.3711,8.7334,49.4397",
           "format": "csv",
           "time": "2014-01-01/2026-01-01/P1Y",
           "filter": "geometry:point and natural=tree",
           "groupByKey": "species"
       })
   print(response.json())

**v2**:

.. code-block:: python

   import httpx
   OHSOME_API_URL = "https://api.heigit.org/ohsome-api/v2-rc"
   OHSOME_API_KEY = # insert your api key here
   response = httpx.post(
       OHSOME_API_URL + "/stats/features/count.json",
       json={
           "aoi": [ 8.625,49.3711,8.7334,49.4397 ],
           "filter": "geometry:point and natural=tree",
           "time": {
               "start": "2014-01-01",
               "end": "2026-01-01",
               "interval": "P1Y",
           },
           "groupBy": {
               "type": "byTag",
               "key": "species",
           }
       },
       headers={"Authorization": OHSOME_API_KEY},
   )
   print(response.json())

ohsome-py
^^^^^^^^^

**v1**:

.. code-block:: python

   from ohsome import OhsomeClient
   client = OhsomeClient()
   response = client.elements.count.post(
	   endpoint="elements/area",
	   bboxes=[8.625,49.3711,8.7334,49.4397],
	   time="2020-01-01",
	   filter="landuse=farmland and geometry:polygon"
   )

**v2**:

Discontinued. Maybe a replacement will be made available at a later
point in time.

------------

Footnotes
---------

.. raw:: html

   </td>
   </tr>
   </table>


.. [1]
   At a later point in time, the URLs will be changed to omit the
   ``-rc`` part, e.g. the root URL for the API endpoints will be finally
   ``https://api.heigit.org/ohsome-api/v2/``.

.. [2]
   This endpoint does not include relations that are not representing a
   polygon (i.e. a ``type=multipolygon`` or ``type=boundary``). For
   generating statistics about other relations types, we might include a
   ``stats/collections/count`` endpoint in an upcoming update.

.. [3]
   instead of a full FeatureCollection, **v2** accepts a single GeoJSON
   Geometry object of a ``Polygon`` or ``MultiPolygon`` only.

.. [4]
   **v1** also supported a list of timestamps with potentially uneven
   intervals, which is not yet available in **v2**. For such request,
   multiple individual requests can be performed instead.

.. [5]
   in **v1** all results from ``length`` and ``area`` calculations used
   values based of the OSM features' geometry clipped to the request's
   area of interest. **v2** does by default not perform this clipping.
   For request around relatively small areas, it is recommended to
   specify ``"clip": true`` in order to get precise results.

.. [6]
   In addition to tags and OSM metadata, the contributions extraction
   now also includes the tags of the respective *changesets*, which was
   not accessible in **v1**.

.. [7]
   OSM relations that are not a ``type=multipolygon`` or
   ``type=boundary`` (e.g. OSM route relations) were previously included
   in the ``elements/geometry`` output as *GeometryCollection* features.
   In **v2** they are instead now accessible via separate endpoints
   which either return the respective relation as as a whole geometry
   collection (e.g. ``MultiPoint``, ``MultiLine`` or ``MultiPolygon``
   geometry), or in form of the set of all their individual members
   (using the members' respective geometry and including the members'
   tags).

.. [8]
   see also above for how non-polygonal OSM relations are handled
   differently in **v2** in general