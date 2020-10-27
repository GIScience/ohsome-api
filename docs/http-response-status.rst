HTTP response status of the ohsome API
======================================

List of HTTP status codes and respective descriptions of the ohsome API.

2xx success
-----------

* ``200 OK`` - standard response for successfully GET and POST requests

4xx client errors
-----------------

* ``400 Bad Request`` - the ohsome API cannot or will not process the request due to client error (e.g. malformed request syntax)
* ``401 Unauthorized`` - the client does not have valid authentication credentials for the target resource
* ``404 Not Found``-  the requested resource coud not be found
* ``405 Method not allowed`` - a request method is not supported (e.g. a POST request for resources which accept only GET requests)
* ``413 Payload Too Large`` - the request is larger than the ohsome API is willing or able to process.

5xx server errors
-----------------

* ``500 Internal Server Error``- generic error message, given when an unexpected condition in the ohsome API was encountered
* ``501 Not Implemented``	- the ohsome API either does not recognize the request method, or it lacks the ability to fulfil the request
* ``503 Service Unavailable`` - the ohsome API cannot handle the request (e.g. because it is overloaded or down for maintenance); temporary state
