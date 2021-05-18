HTTP Response Status
====================

List of HTTP status codes and possible messages.

2xx success
-----------

``200 OK`` - standard response for successful GET or POST requests.

.. note:: If an error occurs during a data-extraction request, the result will be a broken GeoJSON containing the error JSON object at the end. In this case, the ohsome API will give back a 200 response status.

4xx client errors
-----------------

``400 Bad Request`` - the ohsome API cannot process the request due to a client error. This can occur because a parameter is not given in the correct format, mandatory parameters are not present, unknown or not valid parameters are present, parameters are not unique, malformed GeoJSON, unsopported content-type reader, wrong parameter values or other malformed requests.

``404 Not Found``-  the requested resource could not be found because the given boundary or time are not within the underlying osh-data.

``413 Payload Too Large`` - the request is too large in respect to the given timeout parameter.

5xx server errors
-----------------

``500 Internal Server Error``- generic error message, given when an unexpected condition in the ohsome API was encountered. This can occur in cases of bugs, when keytables are missing or the access to the database failed.

``503 Service Unavailable`` - temporary state; the ohsome API cannot handle the request because the cluster backend is currently not able to process requests.
