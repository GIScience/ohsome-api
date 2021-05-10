HTTP Response Status
====================

List of HTTP status codes and respective descriptions.

2xx success
-----------
``200 OK`` - standard response for successful GET or POST requests.

4xx client errors
-----------------

``400 Bad Request`` - the ohsome API cannot or will not process the request due to a client error.

List of possible messages:

* "You need to define one of the boundary parameters (bboxes, bcircles, bpolys)."
* "Wrong time parameter. You need to give exactly two ISO-8601 conform timestamps, if you want to use the full-history extraction."
* "You need to give one groupByKey parameter, if you want to use groupBy/tag."
* "You need to give one groupByKeys parameter, if you want to use groupBy/key."
* "The endpoint 'metadata' does not require parameters"
* "The geometry of each feature in the GeoJSON has to be of type 'Polygon' or 'MultiPolygon'."
* "Apart from the custom ids, the bboxeses array must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat."
* "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs."
* "Each bcircle must consist of a lon/lat coordinate pair plus a buffer in meters."
* "Error in reading the provided GeoJSON. The given GeoJSON has to be of the type 'FeatureCollection'. Please take a look at the documentation page for the bpolys parameter to see an example of a fitting GeoJSON input file."
* "The provided custom id(s) could not be parsed."
* "The provided GeoJSON cannot be converted. Please take a look at the documentation page for the bpolys parameter to see an example of a fitting GeoJSON input file."
* "Error in processing the boundary parameter. Please remember to follow the format, where you separate every coordinate with a comma, each boundary object with a pipe-sign and add optional custom ids to every first coordinate with a colon."
* "The given custom ids cannot contain semicolons, if you want to use csv as output format."
* "The interval (period) of the provided time parameter is not ISO-8601 conform."
* "The provided time parameter is not ISO-8601 conform."
* "Invalid filter syntax. Please look at the additional info and examples about the filter parameter at https://docs.ohsome.org/ohsome-api. Detailed error message: [...]"
* "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. You can either set custom ids for allyour boundary objects, or for none."
* "Unsupported content-type header found. Please make sure to use either 'multipart/form-data' or 'application/x-www-form-urlencoded'."
* "The filter2 parameter has to be defined when using a /ratio endpoint."
* "The given 'format' parameter is invalid. Please choose between 'geojson'(only available for /groupBy/boundary and data extraction requests), 'json', or 'csv'."
* "Error in processing the boundary parameter. Please remember to follow the format, where you separate every coordinate with a comma, each boundary object with a pipe-sign and add optional custom ids to every first coordinate with a colon."
* "Unknown parameter '[parameter]' for this resource."
* "Unknown parameter '[parameter]' for this resource. Did you mean '[parameter]'?"
* "Unknown parameter '[parameter]' for this resource. Did you mean '[parameter]' or '[parameter]'?"
* "Every parameter has to be unique. You can't give more than one '[parameter type]' parameter."
* "There cannot be more input values in the values|values2 than in the keys|keys2 parameter, as values_n must fit to keys_n."
* "Parameter 'types' (and 'types2') cannot have more than 4 entries."
* "The given timeout does not fit to its format. Please give one value in seconds and use a point as the decimal delimiter, if needed."
* "The given timeout is too long. It has to be shorter than [timeout] seconds"
* "You need to give at least two timestamps or a time interval for this resource."
* "The given parameter [parameter] can only contain the values 'true', 'yes', 'false', or 'no'."
* "Your provided boundary parameter (bboxes, bcircles, or bpolys) does not fit its format, or you defined more than one boundary parameter."
* "The geometry of your given boundary input could not be parsed for the creation of the response GeoJSON."
* "The keys, values and types parameters must be empty, when you set the filter parameter."
* "The properties parameter of this resource can only contain the values 'tags' and/or 'metadata' and/or 'contributionTypes' and/or 'unclipped'."
* "The properties parameter of this resource can only contain the values 'tags' and/or 'metadata' and/or 'unclipped'."

``404 Not Found``-  the requested resource could not be found.

List of possible messages

* "The provided boundary parameter does not lie completely within the underlying data-extract polygon."
* "The given time parameter is not completely within the timeframe ("[...] to [...]") of the underlying osh-data."

``413 Payload Too Large`` - the request is larger than the ohsome API is willing or able to process.

List of possible messages

* "The given query is too large in respect to the given timeout. Please use a smaller region and/or coarser time period."

5xx server errors
-----------------

``500 Internal Server Error``- generic error message, given when an unexpected condition in the ohsome API was encountered.

List of possible messages:

* "Keytables not found or access to database failed."
* "Missing keytables."

``503 Service Unavailable`` - the ohsome API cannot handle the request; temporary state.

List of possible messages:

* "The cluster backend is currently not able to process your request. Please try again later."