Boundaries
==========

The spatial filter for the ohsome API is defined via the query parameters ``bboxes``,
``bcircles``, or ``bpolys``. The coordinate system is WGS 84 in the format lon-lat. 

.. note:: There is no default value for these query parameters. Either one of them (and only one) has to be defined. 

bboxes
------

    Bottom left and top right points to define a bounding box.
    The following two formats are allowed:
    
 * .. sourcecode:: text

    lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|…
	
    8.5992,49.3567,8.7499,49.4371|9.1638,49.113,9.2672,49.1766
    
 * .. sourcecode:: text

    id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|…
	
    Heidelberg:8.5992,49.3567,8.7499,49.4371|Heilbronn:9.1638,49.113,9.2672,49.1766


bcircles
--------

    Coordinate pair and radius in meters to define a circular polygon.
    The following two formats are allowed:
    
 * .. sourcecode:: text

    lon,lat,r|lon,lat,r|…
	
    8.6528,49.3683,1000|8.7294,49.4376,1000 
    
 * .. sourcecode:: text

    id1:lon,lat,r|id2:lon,lat,r|…
	
    Circle 1:8.6528,49.3683,1000|Circle 2:8.7294,49.4376,1000   


bpolys
------

    Coordinates given as a list of coordinate pairs (as for bboxes) or 
    GeoJSON FeatureCollection. The first point has to be the same as the last point and 
    MultiPolygons are only supported in GeoJSON. The following three formats are allowed:

 * .. sourcecode:: text

    8.65821,49.41129,8.65821,49.41825,8.70053,49.41825,8.70053,49.41129,8.65821,49.41129|8.67817,49.42147,8.67817,49.4342,8.70053,49.4342,8.70053,49.42147,8.67817,49.42147

 * .. sourcecode:: text

    Region 1:8.65821,49.41129,8.65821,49.41825,8.70053,49.41825,8.70053,49.41129,8.65821,49.41129|Region 2:8.67817,49.42147,8.67817,49.4342,8.70053,49.4342,8.70053,49.42147,8.67817,49.42147
 
 * .. sourcecode:: json

    {"type":"FeatureCollection","features":[{"type":"Feature","properties":{"id":"Region 1"},"geometry":{"type":"Polygon","coordinates":[[[8.65821,49.41129],[8.65821,49.41825],[8.70053,49.41825],[8.70053,49.41129],[8.65821,49.41129]]]}},{"type":"Feature","properties":{"id":"Region 2"},"geometry":{"type":"Polygon","coordinates":[[[8.67817,49.42147],[8.67817,49.4342],[8.70053,49.4342],[8.70053,49.42147],[8.67817,49.42147]]]}}]}
