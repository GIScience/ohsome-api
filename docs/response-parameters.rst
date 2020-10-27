Response Parameters
===================

List and description of response parameters.

Extraction Endpoints
--------------------

* ``@osmId`` - id of the OSM feature with its type (node/way/relation)
* ``@validFrom`` - indicates when a creation or change of this feature with these exactly attributes and geometry was made; has the same value as the fromTimestamp if the creation/change happened before the requested timestamp
* ``@validTo`` - indicates until when this feature with these exactly attributes and geometry stayed unchanged or undeleted; has the same value as the fromTimestamp if the creation/change happened before the requested timestamp

Contribution Endpoints
----------------------

* ``@osmId`` - id of the OSM feature with its type (node/way/relation)
* ``@osmType``- type (node/way/relation) of OSM feature
* ``@timestamp`` - indicates when this contribution occurred
* ``@version`` - version of the feature
* ``@changesetId``- id assigned to a changeset
* ``@creation``	- contribution type; indicates if this feature is newly created (true); cannot occur in combination with other contribution types
* ``@geometryChange`` - contribution type; indicates if the geometry of this feature has changed (true); can occur in combination with @tagChange
* ``@tagChange``- contribution type; indicates if the tag of this feature has changed (true); can occur in combination with @geometryChange
* ``@deletion`` - contribution type; indicates if the feature is deleted (true); cannot occur in combination with other contribution types
