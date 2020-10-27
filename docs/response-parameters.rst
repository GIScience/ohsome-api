Response Parameters
===================

Descriptions of the custom response parameters that are marked with a leading ``@`` for the 
data extraction and contributions endpoints.

General Parameters
------------------

* ``@osmId`` - id of the OSM feature with its type (node/way/relation)
* ``@version`` - version of the feature
* ``@changesetId``- id assigned to a changeset
* ``@osmType``- type (node/way/relation) of OSM feature


Extraction Parameters
---------------------

**specific for /elements**

* ``@snapshotTimestamp`` - describes for which timestamp a snapshot of this feature was requested
* ``@lastEdit`` - describes the timestamp at which this feature was edited the last time

**specific for /elementsFullHistory**

* ``@validFrom`` - indicates when a creation or change of this feature with the provided attributes and geometry was made; has the same value as the fromTimestamp if the creation/change happened before the requested time interval
* ``@validTo`` - indicates until when this feature with the provided attributes and geometry stayed unchanged or undeleted; has the same value as the toTimestamp if the change/deletion happened after the requested time interval

Contribution Parameters
-----------------------

* ``@timestamp`` - indicates when this contribution occurred
* ``@creation``	- contribution type; indicates if this feature is newly created (true); cannot occur in combination with other contribution types
* ``@geometryChange`` - contribution type; indicates if the geometry of this feature has changed (true); can occur in combination with @tagChange
* ``@tagChange``- contribution type; indicates if the tag of this feature has changed (true); can occur in combination with @geometryChange
* ``@deletion`` - contribution type; indicates if the feature is deleted (true); cannot occur in combination with other contribution types
