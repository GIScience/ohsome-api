.. reference guides are technical descriptions of the machinery and how to
   operate it. reference material is information-oriented.
   https://diataxis.fr/

Area of Interest (AOI)
----------------------

The ``aoi`` parameter allows to control the spatial extent of the request.

It can be defined in different formats: GeoJSON Geometry, Bounding Box (BBOX) or Well Known Text (WKT).

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

A single ``POLYGON`` or ``MULTIPOLYGON`` Geometry as a standard text string.

Example:

.. code-block:: json

    "aoi": "POLYGON ((8.68812 49.4039, 8.72362 49.4039, 8.72362 49.41582, 8.68812 49.41582, 8.68812 49.4039))"



GeoJSON Geometry
^^^^^^^^^^^^^^^^

A single GeoJSON ``Polygon`` or ``MultiPolygon`` Geometry object.
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



