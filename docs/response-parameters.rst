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

Extraction Parameters
---------------------

Descriptions of the custom response parameters that are marked with a leading ``@``.

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

Contribution Parameters
-----------------------

Descriptions of the custom response parameters that are marked with a leading ``@``.

* ``@osmId`` - id of the OSM element, including its type (e.g. node/1)
* ``@version`` - version of the OSM element
* ``@changesetId`` - id of the OSM changeset where the contribution was performed
* ``@osmType`` - type of the OSM element (NODE, WAY or RELATION)
* ``@timestamp`` - indicates when this contribution occurred
* ``@creation``	- contribution type; indicates if a new feature gets created OR moved into your requested area of interest OR is now fitting to your defined filter parameter (true); cannot occur in combination with other contribution types
* ``@geometryChange`` - contribution type; indicates if the geometry of this feature has changed (true); can occur in combination with @tagChange
* ``@tagChange``- contribution type; indicates if the tag(s) of this feature has/have changed (true); can occur in combination with @geometryChange
* ``@deletion`` - contribution type; indicates if a feature gets deleted OR moved outside of your requested area of interest OR is not fitting anymore to your defined filter parameter (true); cannot occur in combination with other contribution types

.. note:: No `contribution type` can occur with having ``false`` as a value. If any of them is present, the value is always ``true``.

Metadata
--------

* ``timeout`` - limit of the processing time in seconds
* ``spatialExtent`` - spatial boundary of the OSM data in the underlying OSHDB
* ``temporalExtent`` - timeframe of the OSM data in the underlying OSHDB
* ``fromTimestamp`` - temporal starting point
* ``toTimestamp`` - temporal ending point
* ``replicationSequenceNumber`` - precise state of the OSM data contained in the underlying OSHDB, expressed as the id of the last applied (hourly) diff file from planet.openstreetmap.org
