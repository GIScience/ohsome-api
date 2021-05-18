Response Parameters
===================

Description of the response parameters.

General Parameters
------------------

Description of parameters that are present in every response.

* ``attribution`` - copyrights and attribution
* ``apiVersion`` - version of the ohsome API

OSM Tags
--------

When requested, the result will contain OSM elements' tags as individual GeoJSON properties.

.. note:: Any OSM tag with a key starting with the special `@` character (e.g.: `@osmId`), will be modified through the addition of another `@` at the start (the example would be changed to `@@osmId`).

Aggregation Parameters
----------------------

* ``value`` - indicates the result of the chosen computation (``count``, ``area``, ``lenght``, ``perimeter``)

**specific for /contributions**

* ``fromTimestamp`` - temporal starting point
* ``toTimestamp`` - temporal ending point

**specific for /elements and /users**

* ``timestamp`` - indicates the period of time to which the computation refers
* ``value2`` - result of the chosen computation (``count``, ``area``, ``lenght``, ``perimeter``) applying the filter2 parameter
* ``ratio`` - indicates the result of the ratio computation

Extraction Parameters
---------------------

Description of the custom response parameters that are marked with a leading ``@``.

* ``@osmId`` - id of the OSM element, including its type (e.g. node/1)
* ``@version`` - version number of the OSM element
* ``@changesetId`` - id of the OSM changeset which last increased the version of this OSM element
* ``@osmType`` - type of the OSM element (NODE, WAY or RELATION)

**specific for /elements**

* ``@snapshotTimestamp`` - describes for which timestamp a snapshot of this feature was requested
* ``@lastEdit`` - describes the timestamp at which this feature was edited the last time

**specific for /elementsFullHistory**

* ``@validFrom`` - indicates when a creation or change of this feature with the provided attributes and geometry was made; has the same value as the fromTimestamp if the creation/change happened before the requested time interval
* ``@validTo`` - indicates until when this feature with the provided attributes and geometry stayed unchanged or undeleted; has the same value as the toTimestamp if the change/deletion happened after the requested time interval

**specific for /contributions**

* ``@timestamp`` - indicates when this contribution occurred
* ``@creation`` - contribution type; indicates if the OSM element newly fits the query's requirements: either because it is freshly created, moved into the query's area of interest, or is now matching the defined filter parameter (true); cannot occur in combination with other contribution types
* ``@geometryChange`` - contribution type; indicates if the geometry of the OSM element has changed (true); can occur in combination with @tagChange
* ``@tagChange``- contribution type; indicates if the tags of this OSM element have changed (true); can occur in combination with @geometryChange
* ``@deletion`` - contribution type; indicates if the OSM element does not match the query requirements anymore: either because it got deleted, moved outside of the query area of interest, or is not matching the defined filter anymore (true); cannot occur in combination with other contribution types

.. note:: No `contribution type` can occur with having ``false`` as a value. If any of them is present, the value is always ``true``.

Metadata
--------

* ``timeout`` - limit of the processing time in seconds
* ``spatialExtent`` - spatial boundary of the OSM data in the underlying OSHDB
* ``temporalExtent`` - timeframe of the OSM data in the underlying OSHDB
* ``fromTimestamp`` - temporal starting point
* ``toTimestamp`` - temporal ending point
* ``replicationSequenceNumber`` - precise state of the OSM data contained in the underlying OSHDB, expressed as the id of the last applied (hourly) diff file from planet.openstreetmap.org
