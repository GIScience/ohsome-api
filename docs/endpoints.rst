API Endpoints
=============


.. note:: For **POST requests** the fields are given analogous to **GET requests**. When you just have a smaller set of spatial parameters,
    a GET request fits perfectly. POST mostly makes sense when you start to use GeoJSON as input geometries.
    
    The usage of the parameters **types**, **keys** and **values** is not recommended as they are deprecated. Please use the 
    filter_ parameter for your requests.

Aggregation Endpoints
---------------------

.. http:post :: /elements/(aggregation)

   Get ``aggregation`` of OSM elements.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   
   :query <boundary>: One of these boundary parameters: bboxes_, bcircles_, bpolys_. See boundaries_
   :query time: ISO-8601 conform timestring(s); default: latest timestamp in the OSHDB, see time_
   :query filter: combines several attributive filters: OSM type, geometry (simple feature) type, as well as the OSM tag; See filter_
   :query format: 'json' or 'csv'; default: 'json'
   :query showMetadata: add additional metadata information to the response: 'true', 'false', 'yes', 'no'; default: 'false'
   :query timeout: custom timeout to limit the processing time in seconds; default: empty
   :query types: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM type(s) ‘node’ and/or ‘way’ and/or ‘relation’ OR simple feature type(s) ‘point’ and/or ‘line’ and/or 'polygon’ and/or 'other'; default: all three OSM types
   :query keys: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM key(s) given as a list and combined with the 'AND' operator; default: empty
   :query values: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM value(s) given as a list and combined with the 'AND' operator; values(n) MUST fit to keys(n); default: empty
.. _bboxes: boundaries.html#bounding-boxes
.. _bcircles: boundaries.html#circles
.. _bpolys: boundaries.html#polygons
.. _boundaries: boundaries.html#boundaries
.. _time: time.html#time
.. _filter: filter.html#filter


**Example request**:

How big is the area of farmland in the region Rhein-Neckar?

 .. tabs::

   .. code-tab:: bash curl (GET)


      curl -X GET 'https://api.ohsome.org/v1/elements/area?bboxes=8.625%2C49.3711%2C8.7334%2C49.4397&format=json&time=2014-01-01&filter=landuse%3Dfarmland%20and%20type%3Away'

   .. code-tab:: bash curl (POST)


      curl -X POST 'https://api.ohsome.org/v1/elements/area' --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' --data-urlencode 'format=json' --data-urlencode 'time=2014-01-01' --data-urlencode 'filter=landuse=farmland and type:way'

   .. code-tab:: python Python


       import requests
       URL = 'https://api.ohsome.org/v1/elements/area'
       data = {"bboxes": "8.625,49.3711,8.7334,49.4397", "format": "json", "time": "2014-01-01", "filter": "landuse=farmland and type:way"}
       response = requests.post(URL, data=data)
       print(response.json())

   .. code-tab:: r R


       library(httr)
       r <- POST("https://api.ohsome.org/v1/elements/area", encode = "form", body = list(bboxes = "8.625,49.3711,8.7334,49.4397", filter = "landuse=farmland and type:way", time = "2014-01-01"))
       r


**Example response**:

  .. tabs::

   .. code-tab:: json curl (GET)

     {
       "attribution" : {
         "url" : "https://ohsome.org/copyrights",
         "text" : "© OpenStreetMap contributors"
       },
       "apiVersion" : "1.1.0",
       "result" : [ {
         "timestamp" : "2014-01-01T00:00:00Z",
         "value" : 1.020940258E7
       } ]
     }

   .. code-tab:: json curl (POST)

     {
       "attribution" : {
         "url" : "https://ohsome.org/copyrights",
         "text" : "© OpenStreetMap contributors"
       },
       "apiVersion" : "1.1.0",
       "result" : [ {
         "timestamp" : "2014-01-01T00:00:00Z",
         "value" : 1.020940258E7
       } ]
     }


   .. code-tab:: json Python

     {
       "attribution" : {
         "url" : "https://ohsome.org/copyrights",
         "text" : "© OpenStreetMap contributors"
       },
       "apiVersion" : "1.1.0",
       "result" : [ {
         "timestamp" : "2014-01-01T00:00:00Z",
         "value" : 10209402.58
       } ]
     }

   .. code-tab:: json R

     {
       "attribution" : {
         "url" : "https://ohsome.org/copyrights",
         "text" : "© OpenStreetMap contributors"
       },
       "apiVersion" : "1.1.0",
       "result" : [ {
         "timestamp" : "2014-01-01T00:00:00Z",
         "value" : 1.020940258E7
       } ]
     }


.. http:post :: /elements/(aggregation)/density

   Get density of ``aggregation`` of OSM elements divided by the total area in square-kilometers.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   
   :query <other>: see above_

**Example request**:

What is the density of restaurants with wheelchair access in Heidelberg?

   .. tabs::

      .. code-tab:: bash curl (GET)

         curl -X GET 'https://api.ohsome.org/v1/elements/count/density?bboxes=8.625%2C49.3711%2C8.7334%2C49.4397&format=json&filter=amenity%3Drestaurant%20and%20wheelchair%3Dyes%20and%20type%3Anode&time=2019-05-07'

      .. code-tab:: bash curl (POST)

         curl -X POST 'https://api.ohsome.org/v1/elements/count/density' --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' --data-urlencode 'format=json' --data-urlencode 'time=2019-05-07' --data-urlencode 'filter=amenity=restaurant and wheelchair=yes and type:node'

      .. code-tab:: python Python

          import requests
          URL = 'https://api.ohsome.org/v1/elements/count/density'
          data = {"bboxes": "8.625,49.3711,8.7334,49.4397", "format": "json", "time": "2019-05-07", "filter": "amenity=restaurant and wheelchair=yes and type:node"}
          response = requests.post(URL, data=data)

      .. code-tab:: r R

         library(httr)
         r <- POST("https://api.ohsome.org/v1/elements/count/density", encode = "form", body = list(bboxes = "8.625,49.3711,8.7334,49.4397", filter = "amenity=restaurant and wheelchair=yes and type:node", time = "2019-05-07"))
         r

**Example response**:

 .. tabs::

       .. code-tab:: json curl (GET)

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "result" : [ {
                 "timestamp" : "2019-05-07T00:00:00Z",
                 "value" : 0.79
               } ]
             }

       .. code-tab:: json curl (POST)

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "result" : [ {
                 "timestamp" : "2019-05-07T00:00:00Z",
                 "value" : 0.79
               } ]
             }


       .. code-tab:: json Python

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "result" : [ {
                 "timestamp" : "2019-05-07T00:00:00Z",
                 "value" : 0.79
               } ]
             }

       .. code-tab:: json R

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "result" : [ {
                 "timestamp" : "2019-05-07T00:00:00Z",
                 "value" : 0.79
               } ]
             }

.. http:post :: /elements/(aggregation)/ratio

   Get ratio of OSM elements satisfying ``filter2`` to elements satisfying ``filter``.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   
   :query <other>: see above_
   :query filter2: see filter_
   :query keys2: Deprecated! see **filter2**
   :query types2: Deprecated! use **filter2**
   :query values2: Deprecated! see **filter2**
   
**Example request**:

How many oneway streets exist within living_street streets in Heidelberg over time? And how many of them are oneway streets?

 .. tabs::

      .. code-tab:: bash curl (GET)

          curl -X GET 'https://api.ohsome.org/v1/elements/length/ratio?bboxes=8.625%2C49.3711%2C8.7334%2C49.4397&format=json&filter=highway%3Dliving_street%20and%20type%3Away&filter2=highway%3Dliving_street%20and%20oneway%3Dyes%20and%20type%3Away&time=2016-01-01%2F2018-01-01%2FP1Y'

      .. code-tab:: bash curl (POST)

          curl -X POST 'https://api.ohsome.org/v1/elements/length/ratio' --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' --data-urlencode 'format=json' --data-urlencode 'time=2016-01-01/2018-01-01/P1Y' --data-urlencode 'filter=highway=living_street and type:way' --data-urlencode 'filter2=highway=living_street and oneway=yes and type:way'

      .. code-tab:: python Python

          import requests
          URL = 'https://api.ohsome.org/v1/elements/length/ratio'
          data = {"bboxes": "8.625,49.3711,8.7334,49.4397", "format": "json", "time": "2016-01-01/2018-01-01/P1Y", "filter": "highway=living_street and type:way", "filter2": "highway=living_street and oneway=yes and type:way"}
          response = requests.post(URL, data=data)
          print(response.json())

      .. code-tab:: r R

           library(httr)
           r <- POST("https://api.ohsome.org/v1/elements/length/ratio", encode = "form", body = list(bboxes = "8.625,49.3711,8.7334,49.4397", time = "2016-01-01/2018-01-01/P1Y", filter = "highway=living_street and type:way", filter2 = "highway=living_street and oneway=yes and type:way"))
           r

**Example response**:

   .. tabs::

       .. code-tab:: json curl (GET)

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "ratioResult" : [ {
                 "timestamp" : "2016-01-01T00:00:00Z",
                 "value" : 28660.519999999997,
                 "value2" : 7079.26,
                 "ratio" : 0.247004
               }, {
                 "timestamp" : "2017-01-01T00:00:00Z",
                 "value" : 29410.69,
                 "value2" : 7025.94,
                 "ratio" : 0.238891
               }, {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 30191.93,
                 "value2" : 6729.34,
                 "ratio" : 0.222885
               } ]
             }


       .. code-tab:: json curl (POST)

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "ratioResult" : [ {
                 "timestamp" : "2016-01-01T00:00:00Z",
                 "value" : 28660.519999999997,
                 "value2" : 7079.26,
                 "ratio" : 0.247004
               }, {
                 "timestamp" : "2017-01-01T00:00:00Z",
                 "value" : 29410.69,
                 "value2" : 7025.94,
                 "ratio" : 0.238891
               }, {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 30191.93,
                 "value2" : 6729.34,
                 "ratio" : 0.222885
               } ]
             }



       .. code-tab:: json Python

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "ratioResult" : [ {
                 "timestamp" : "2016-01-01T00:00:00Z",
                 "value" : 28660.519999999997,
                 "value2" : 7079.26,
                 "ratio" : 0.247004
               }, {
                 "timestamp" : "2017-01-01T00:00:00Z",
                 "value" : 29410.69,
                 "value2" : 7025.94,
                 "ratio" : 0.238891
               }, {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 30191.93,
                 "value2" : 6729.34,
                 "ratio" : 0.222885
               } ]
             }

       .. code-tab:: json R

             {
               "attribution" : {
                 "url" : "https://ohsome.org/copyrights",
                 "text" : "© OpenStreetMap contributors"
               },
               "apiVersion" : "1.1.0",
               "ratioResult" : [ {
                 "timestamp" : "2016-01-01T00:00:00Z",
                 "value" : 28660.519999999997,
                 "value2" : 7079.26,
                 "ratio" : 0.247004
               }, {
                 "timestamp" : "2017-01-01T00:00:00Z",
                 "value" : 29410.69,
                 "value2" : 7025.94,
                 "ratio" : 0.238891
               }, {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 30191.93,
                 "value2" : 6729.34,
                 "ratio" : 0.222885
               } ]
             }

.. http:post :: /elements/(aggregation)/groupBy/(groupType)

   Get ``aggregation`` of OSM elements grouped by ``groupType``.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   * grouping type: one of boundary_, key_, tag_, type_.
   
   :query <other>: see above_
   :query groupByKeys: see groupBy_
   :query groupByKey: see groupBy_
   :query groupByValues: see groupBy_
.. _boundary: group-by.html#boundary
.. _key: group-by.html#key
.. _tag: group-by.html#tag
.. _type: group-by.html#type
.. _groupBy: group-by.html

.. note:: For **groupBy/key** and **groupBy/tag**, 
          there are additional resource-specific parameters, which you can find at groupBy_.


**Example request**:

How often information about the roof of buildings is present?

.. tabs::

     .. code-tab:: bash curl (GET)

        curl -X GET 'https://api.ohsome.org/v1/elements/count/groupBy/key?bboxes=Heidelberg:8.625%2C49.3711%2C8.7334%2C49.4397&format=json&time=2018-01-01&filter=building%3D*%20and%20type%3Away&groupByKeys=building%3Aroof%2Cbuilding%3Aroof%3Acolour'

     .. code-tab:: bash curl (POST)

        curl -X POST 'https://api.ohsome.org/v1/elements/count/groupBy/key' --data-urlencode 'bboxes=Heidelberg:8.625,49.3711,8.7334,49.4397' --data-urlencode 'format=json' --data-urlencode 'time=2018-01-01' --data-urlencode 'groupByKeys=building:roof,building:roof:colour' --data-urlencode 'filter=building=* and type:way'

     .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/elements/count/groupBy/key'
        data = {"bboxes": "8.625,49.3711,8.7334,49.4397", "format": "json", "time": "2018-01-01", "filter": "building=* and type:way", "groupByKeys": "building:roof,building:roof:colour"}
        response = requests.post(URL, data=data)
        print(response.json())

     .. code-tab:: r R

        library(httr)
          r <- POST("https://api.ohsome.org/v1/elements/count/groupBy/key", encode = "form", body = list(bboxes = "8.625,49.3711,8.7334,49.4397", filter = "building=* and type:way", time = "2018-01-01", groupByKeys = "building:roof,building:roof:colour"))
          r

**Example response**:

    .. tabs::

          .. code-tab:: json curl (GET)

            {
              "attribution" : {
                "url" : "https://ohsome.org/copyrights",
                "text" : "© OpenStreetMap contributors"
              },
              "apiVersion" : "1.1.0",
              "groupByResult" : [ {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 23225.0
                } ],
                "groupByObject" : "remainder"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1418.0
                } ],
                "groupByObject" : "building:roof"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1178.0
                } ],
                "groupByObject" : "building:roof:colour"
              } ]
            }


          .. code-tab:: json curl (POST)

            {
              "attribution" : {
                "url" : "https://ohsome.org/copyrights",
                "text" : "© OpenStreetMap contributors"
              },
              "apiVersion" : "1.1.0",
              "groupByResult" : [ {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 23225.0
                } ],
                "groupByObject" : "remainder"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1418.0
                } ],
                "groupByObject" : "building:roof"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1178.0
                } ],
                "groupByObject" : "building:roof:colour"
              } ]
            }

          .. code-tab:: json Python

            {
              "attribution" : {
                "url" : "https://ohsome.org/copyrights",
                "text" : "© OpenStreetMap contributors"
              },
              "apiVersion" : "1.1.0",
              "groupByResult" : [ {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 23225.0
                } ],
                "groupByObject" : "remainder"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1418.0
                } ],
                "groupByObject" : "building:roof"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1178.0
                } ],
                "groupByObject" : "building:roof:colour"
              } ]
            }

          .. code-tab:: json R

            {
              "attribution" : {
                "url" : "https://ohsome.org/copyrights",
                "text" : "© OpenStreetMap contributors"
              },
              "apiVersion" : "1.1.0",
              "groupByResult" : [ {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 23225.0
                } ],
                "groupByObject" : "remainder"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1418.0
                } ],
                "groupByObject" : "building:roof"
              }, {
                "result" : [ {
                  "timestamp" : "2018-01-01T00:00:00Z",
                  "value" : 1178.0
                } ],
                "groupByObject" : "building:roof:colour"
              } ]
            }

.. http:post :: /elements/(aggregation)/density/groupBy/(groupType)

   Get ``density`` of ``aggregation`` of OSM elements grouped by ``groupType``.
   
   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   * grouping type: see above - Same as for **aggregation** of OSM elements grouped by **groupType** but without **groupBy/key**.


.. http:post :: /elements/(aggregation)/groupBy/boundary/groupBy/tag

   Get ``aggregation`` of OSM elements grouped by ``boundary`` and ``tag``.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   
   :query <other>: see above
   :query groupByKey: see groupBy_
   :query groupByValues: see groupBy_
.. _boundary: group-by.html#boundary
.. _key: group-by.html#key
.. _tag: group-by.html#tag
.. _type: group-by.html#type
.. _groupBy: group-by.html

**Example request**:

Compare length of different types of streets for two or more regions.

   .. tabs::

        .. code-tab:: bash curl (GET)

           curl -X GET 'https://api.ohsome.org/v1/elements/length/groupBy/boundary/groupBy/tag?bboxes=Heidelberg%3A8.625%2C49.3711%2C8.7334%2C49.4397%7CPlankstadt%3A8.5799%2C49.3872%2C8.6015%2C49.4011&format=json&groupByKey=highway&time=2018-01-01&groupByValues=primary%2Csecondary%2Ctertiary&filter=type%3Away'

        .. code-tab:: bash curl (POST)

           curl -X POST 'https://api.ohsome.org/v1/elements/length/groupBy/boundary/groupBy/tag' --data-urlencode 'bboxes=Heidelberg:8.625,49.3711,8.7334,49.4397|Plankstadt:8.5799,49.3872,8.6015,49.4011' --data-urlencode 'format=json' --data-urlencode 'time=2018-01-01' --data-urlencode 'filter=type:way' --data-urlencode 'groupByKey=highway' --data-urlencode 'groupByValues=primary,secondary,tertiary'

        .. code-tab:: python Python

            import requests
            URL = 'https://api.ohsome.org/v1/elements/length/groupBy/boundary/groupBy/tag'
            data = {"bboxes": "Heidelberg:8.625,49.3711,8.7334,49.4397|Plankstadt:8.5799,49.3872,8.6015,49.4011", "format": "json", "time": "2018-01-01", "filter": "type:way", "groupByKey": "highway", "groupByValues": "primary,secondary,tertiary"}
            response = requests.post(URL, data=data)
            print(response.json())

        .. code-tab:: r R

             library(httr)
             r <- POST("https://api.ohsome.org/v1/elements/length/groupBy/boundary/groupBy/tag", encode = "form", body = list(bboxes = "Heidelberg:8.625,49.3711,8.7334,49.4397|Plankstadt:8.5799,49.3872,8.6015,49.4011", groupByKey = "highway", time = "2018-01-01", filter = "type:way", groupByValues = "primary,secondary,tertiary"))
             r

**Example response**:

   .. tabs::

         .. code-tab:: json curl (GET)

           {
             "attribution" : {
               "url" : "https://ohsome.org/copyrights",
               "text" : "© OpenStreetMap contributors"
             },
             "apiVersion" : "1.1.0",
             "groupByResult" : [ {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 1650245.08
               } ],
               "groupByObject" : [ "Heidelberg", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 48637.96
               } ],
               "groupByObject" : [ "Heidelberg", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 29114.72
               } ],
               "groupByObject" : [ "Heidelberg", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 35297.95
               } ],
               "groupByObject" : [ "Heidelberg", "highway=primary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 56493.26
               } ],
               "groupByObject" : [ "Plankstadt", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 3399.22
               } ],
               "groupByObject" : [ "Plankstadt", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 954.7
               } ],
               "groupByObject" : [ "Plankstadt", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 0.0
               } ],
               "groupByObject" : [ "Plankstadt", "highway=primary" ]
             } ]
           }


         .. code-tab:: json curl (POST)

           {
             "attribution" : {
               "url" : "https://ohsome.org/copyrights",
               "text" : "© OpenStreetMap contributors"
             },
             "apiVersion" : "1.1.0",
             "groupByResult" : [ {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 1650245.08
               } ],
               "groupByObject" : [ "Heidelberg", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 48637.96
               } ],
               "groupByObject" : [ "Heidelberg", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 29114.72
               } ],
               "groupByObject" : [ "Heidelberg", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 35297.95
               } ],
               "groupByObject" : [ "Heidelberg", "highway=primary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 56493.26
               } ],
               "groupByObject" : [ "Plankstadt", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 3399.22
               } ],
               "groupByObject" : [ "Plankstadt", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 954.7
               } ],
               "groupByObject" : [ "Plankstadt", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 0.0
               } ],
               "groupByObject" : [ "Plankstadt", "highway=primary" ]
             } ]
           }

         .. code-tab:: json Python

           {
             "attribution" : {
               "url" : "https://ohsome.org/copyrights",
               "text" : "© OpenStreetMap contributors"
             },
             "apiVersion" : "1.1.0",
             "groupByResult" : [ {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 1650245.08
               } ],
               "groupByObject" : [ "Heidelberg", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 48637.96
               } ],
               "groupByObject" : [ "Heidelberg", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 29114.72
               } ],
               "groupByObject" : [ "Heidelberg", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 35297.95
               } ],
               "groupByObject" : [ "Heidelberg", "highway=primary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 56493.26
               } ],
               "groupByObject" : [ "Plankstadt", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 3399.22
               } ],
               "groupByObject" : [ "Plankstadt", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 954.7
               } ],
               "groupByObject" : [ "Plankstadt", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 0.0
               } ],
               "groupByObject" : [ "Plankstadt", "highway=primary" ]
             } ]
           }

         .. code-tab:: json R

           {
             "attribution" : {
               "url" : "https://ohsome.org/copyrights",
               "text" : "© OpenStreetMap contributors"
             },
             "apiVersion" : "1.1.0",
             "groupByResult" : [ {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 1650245.08
               } ],
               "groupByObject" : [ "Heidelberg", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 48637.96
               } ],
               "groupByObject" : [ "Heidelberg", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 29114.72
               } ],
               "groupByObject" : [ "Heidelberg", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 35297.95
               } ],
               "groupByObject" : [ "Heidelberg", "highway=primary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 56493.26
               } ],
               "groupByObject" : [ "Plankstadt", "remainder" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 3399.22
               } ],
               "groupByObject" : [ "Plankstadt", "highway=tertiary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 954.7
               } ],
               "groupByObject" : [ "Plankstadt", "highway=secondary" ]
             }, {
               "result" : [ {
                 "timestamp" : "2018-01-01T00:00:00Z",
                 "value" : 0.0
               } ],
               "groupByObject" : [ "Plankstadt", "highway=primary" ]
             } ]
           }


.. http:post :: /elements/(aggregation)/density/groupBy/boundary/groupBy/tag

   Get ``density`` of ``aggregation`` of OSM elements grouped by ``boundary`` and ``tag``.
   
   * aggregation type: same as for **/elements/(aggregation)/groupBy/boundary/groupBy/tag**.


Users Aggregation Endpoints
---------------------------

.. http:post :: /users/count

    Compute data aggregation functions on users. Possbile endpoints:
    
    * /count
    * /count/groupBy/(groupType)
    * /count/density
    * /count/density/groupBy/(boundary or tag or type)

    :query <other>: see above_
    :param groupType: property to group by, one of boundary_, key_, tag_, type_.

**Example request**:

Show number of users editing buildings before, during and after Nepal earthquake 2015.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/users/count?bboxes=82.3055%2C6.7576%2C87.4663%2C28.7025&format=json&filter=building%3D*%20and%20type%3Away&time=2015-03-01%2F2015-08-01%2FP1M'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/users/count' --data-urlencode 'bboxes=82.3055,6.7576,87.4663,28.7025' --data-urlencode 'format=json' --data-urlencode 'time=2015-03-01/2015-08-01/P1M' --data-urlencode 'filter=building=* and type:way'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/users/count'
        data = {"bboxes": "82.3055,6.7576,87.4663,28.7025", "format": "json", "time": "2015-03-01/2015-08-01/P1M", "filter": "building=* and type:way"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/users/count", encode = "form", body = list(bboxes = "82.3055,6.7576,87.4663,28.7025", filter = "building=* and type:way", time = "2015-03-01/2015-08-01/P1M"))
        r


**Example response**:

   .. tabs::

    .. code-tab:: json curl (GET)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "result" : [ {
          "fromTimestamp" : "2015-03-01T00:00:00Z",
          "toTimestamp" : "2015-04-01T00:00:00Z",
          "value" : 97.0
        }, {
          "fromTimestamp" : "2015-04-01T00:00:00Z",
          "toTimestamp" : "2015-05-01T00:00:00Z",
          "value" : 3490.0
        }, {
          "fromTimestamp" : "2015-05-01T00:00:00Z",
          "toTimestamp" : "2015-06-01T00:00:00Z",
          "value" : 3102.0
        }, {
          "fromTimestamp" : "2015-06-01T00:00:00Z",
          "toTimestamp" : "2015-07-01T00:00:00Z",
          "value" : 477.0
        }, {
          "fromTimestamp" : "2015-07-01T00:00:00Z",
          "toTimestamp" : "2015-08-01T00:00:00Z",
          "value" : 185.0
        } ]
      }


    .. code-tab:: json curl (POST)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "result" : [ {
          "fromTimestamp" : "2015-03-01T00:00:00Z",
          "toTimestamp" : "2015-04-01T00:00:00Z",
          "value" : 97.0
        }, {
          "fromTimestamp" : "2015-04-01T00:00:00Z",
          "toTimestamp" : "2015-05-01T00:00:00Z",
          "value" : 3490.0
        }, {
          "fromTimestamp" : "2015-05-01T00:00:00Z",
          "toTimestamp" : "2015-06-01T00:00:00Z",
          "value" : 3102.0
        }, {
          "fromTimestamp" : "2015-06-01T00:00:00Z",
          "toTimestamp" : "2015-07-01T00:00:00Z",
          "value" : 477.0
        }, {
          "fromTimestamp" : "2015-07-01T00:00:00Z",
          "toTimestamp" : "2015-08-01T00:00:00Z",
          "value" : 185.0
        } ]
      }


    .. code-tab:: json Python

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "result" : [ {
          "fromTimestamp" : "2015-03-01T00:00:00Z",
          "toTimestamp" : "2015-04-01T00:00:00Z",
          "value" : 97.0
        }, {
          "fromTimestamp" : "2015-04-01T00:00:00Z",
          "toTimestamp" : "2015-05-01T00:00:00Z",
          "value" : 3490.0
        }, {
          "fromTimestamp" : "2015-05-01T00:00:00Z",
          "toTimestamp" : "2015-06-01T00:00:00Z",
          "value" : 3102.0
        }, {
          "fromTimestamp" : "2015-06-01T00:00:00Z",
          "toTimestamp" : "2015-07-01T00:00:00Z",
          "value" : 477.0
        }, {
          "fromTimestamp" : "2015-07-01T00:00:00Z",
          "toTimestamp" : "2015-08-01T00:00:00Z",
          "value" : 185.0
        } ]
      }

    .. code-tab:: json R

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "result" : [ {
          "fromTimestamp" : "2015-03-01T00:00:00Z",
          "toTimestamp" : "2015-04-01T00:00:00Z",
          "value" : 97.0
        }, {
          "fromTimestamp" : "2015-04-01T00:00:00Z",
          "toTimestamp" : "2015-05-01T00:00:00Z",
          "value" : 3490.0
        }, {
          "fromTimestamp" : "2015-05-01T00:00:00Z",
          "toTimestamp" : "2015-06-01T00:00:00Z",
          "value" : 3102.0
        }, {
          "fromTimestamp" : "2015-06-01T00:00:00Z",
          "toTimestamp" : "2015-07-01T00:00:00Z",
          "value" : 477.0
        }, {
          "fromTimestamp" : "2015-07-01T00:00:00Z",
          "toTimestamp" : "2015-08-01T00:00:00Z",
          "value" : 185.0
        } ]
      }


Extraction Endpoints
--------------------

.. http:post :: /elements/(geometryType)

   Get the state of OSM data at the given timestamp(s) as a GeoJSON feature collection where object geometries are returned as the given ``geometryType`` (geometry, bbox, or centroid).

   :query time: required; format same as described in time_
   :query properties: specifies what properties should be included for each feature representing an OSM element: ‘tags’ and/or 'metadata’; multiple values can be delimited by commas; default: empty
   :query clipGeometry: boolean operator to specify whether the returned geometries of the features should be clipped to the query's spatial boundary (‘true’), or not (‘false’); default: ‘true’
   :query <other>: see above_ (except **format**)
   

.. note:: The extraction endpoints always return a .geojson file.

**Example request**:

Get all the bike rental stations in Heidelberg.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/elements/geometry?bboxes=8.625%2C49.3711%2C8.7334%2C49.4397&filter=amenity%3Dbicycle_rental%20and%20type%3Anode&time=2019-09-01'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/elements/geometry' --data-urlencode 'bboxes=8.625,49.3711,8.7334,49.4397' --data-urlencode 'time=2019-09-01' --data-urlencode 'filter=amenity=bicycle_rental and type:node'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/elements/geometry'
        data = {"bboxes": "8.625,49.3711,8.7334,49.4397", "time": "2019-09-01", "filter": "amenity=bicycle_rental and type:node"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/elements/geometry", encode = "form",body = list(bboxes = "8.625,49.3711,8.7334,49.4397", filter = "amenity=bicycle_rental and type:node", time = "2019-09-01"))
        r


**Example response**:

   .. tabs::

    .. code-tab:: text curl (GET)

      file ohsome.geojson

    .. code-tab:: text curl (POST)

      file ohsome.geojson


    .. code-tab:: text Python

      file ohsome.geojson

    .. code-tab:: text R

      file ohsome.geojson
  

.. http:post :: /elementsFullHistory/(geometryType)

   Get the full history of OSM data as a GeoJSON feature collection. All changes to matching OSM features are included with corresponding ``validFrom`` and ``validTo`` timestamps.
   This endpoint supports the same ``geometryType`` options as the ``/elements`` endpoint.

   :query time: required; must consist of two ISO-8601 conform timestrings defining a time interval; no default value
   :query properties: same as for generic-extraction_
   :query clipGeometry: same as for generic-extraction_
   :query <other>: see above_ (except **format**)

.. _generic-extraction: endpoints.html#post--elements-(geometryType)
.. _above: endpoints.html#post--elements-(aggregation)
.. _time: time.html#time

**Example request**:

Extract the modifications of the blown up tower of the heidelberg castle over time

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/elementsFullHistory/geometry?bboxes=8.7137%2C49.4096%2C8.717%2C49.4119&filter=name%3DKrautturm%20and%20type%3Away&time=2008-01-01%2C2016-01-01'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/elementsFullHistory/geometry' --data-urlencode 'bboxes=8.7137,49.4096,8.717,49.4119' --data-urlencode 'time=2008-01-01,2016-01-01' --data-urlencode 'filter=name=Krautturm and type:way'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/elementsFullHistory/geometry'
        data = {"bboxes": "8.7137,49.4096,8.717,49.4119", "time": "2008-01-01,2016-01-01", "filter": "name=Krautturm and type:way"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/elementsFullHistory/geometry", encode = "form", body = list(bboxes = "8.7137,49.4096,8.717,49.4119", time = "2008-01-01,2016-01-01", filter = "name=Krautturm and type:way"))
        r

.. note:: The following example responses only show parts of the returned .geojson file.

**Example response**:

   .. tabs::

    .. code-tab:: text curl (GET)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.7160104,
                  49.4102861
                ],
                 ...
                 [
                  8.7160104,
                  49.4102861
                ]
              ]
            ]
          },
          "properties" : {
            "@osmId" : "way/24885641",
            "@validFrom" : "2008-06-15T05:25:25Z",
            "@validTo" : "2008-08-09T14:46:28Z",
            "name" : "Krautturm"
          }
        },
        ...
        ]
      }

    .. code-tab:: text curl (POST)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.7160104,
                  49.4102861
                ],
                 ...
                 [
                  8.7160104,
                  49.4102861
                ]
              ]
            ]
          },
          "properties" : {
            "@osmId" : "way/24885641",
            "@validFrom" : "2008-06-15T05:25:25Z",
            "@validTo" : "2008-08-09T14:46:28Z",
            "name" : "Krautturm"
          }
        },
        ...
        ]
      }


    .. code-tab:: text Python

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.7160104,
                  49.4102861
                ],
                 ...
                 [
                  8.7160104,
                  49.4102861
                ]
              ]
            ]
          },
          "properties" : {
            "@osmId" : "way/24885641",
            "@validFrom" : "2008-06-15T05:25:25Z",
            "@validTo" : "2008-08-09T14:46:28Z",
            "name" : "Krautturm"
          }
        },
        ...
        ]
      }

    .. code-tab:: text R

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.7160104,
                  49.4102861
                ],
                 ...
                 [
                  8.7160104,
                  49.4102861
                ]
              ]
            ]
          },
          "properties" : {
            "@osmId" : "way/24885641",
            "@validFrom" : "2008-06-15T05:25:25Z",
            "@validTo" : "2008-08-09T14:46:28Z",
            "name" : "Krautturm"
          }
        },
        ...
        ]
      }
   
      
Contribution Endpoints
----------------------

.. http:post :: /contributions/(geometryType)

   Direct access to all contributions provided to the OSM data. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.
   It uses the same ``geometryType`` options as the ``/elements`` and ``/elementsFullHistory`` endpoints.



Metadata Endpoint
-----------------

.. http:get :: /metadata

    Get metadata of the underlying OSHDB data. Does not consume any parameters. **Only GET requests**.

**Example request**:

Get metadata of the underlying OSHDB data

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/metadata'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/metadata'
        response = requests.get(URL)
        print(response)

    .. code-tab:: r R

        library(httr)
        r <- GET("https://api.ohsome.org/v1/metadata")
        r


**Example response**:

   .. tabs::

    .. code-tab:: json curl (GET)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "extractRegion" : {
          "spatialExtent" : {
            "type" : "Polygon",
            "coordinates" : [ [ [ -180.0, -90.0 ], [ 180.0, -90.0 ], [ 180.0, 90.0 ], [ -180.0, 90.0 ], [ -180.0, -90.0 ] ] ]
          },
          "temporalExtent" : {
            "fromTimestamp" : "2007-10-08T00:00:00Z",
            "toTimestamp" : "2020-02-12T23:00:00Z"
          },
          "replicationSequenceNumber" : 65032
        }
      }

    .. code-tab:: json Python

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "extractRegion" : {
          "spatialExtent" : {
            "type" : "Polygon",
            "coordinates" : [ [ [ -180.0, -90.0 ], [ 180.0, -90.0 ], [ 180.0, 90.0 ], [ -180.0, 90.0 ], [ -180.0, -90.0 ] ] ]
          },
          "temporalExtent" : {
            "fromTimestamp" : "2007-10-08T00:00:00Z",
            "toTimestamp" : "2020-02-12T23:00:00Z"
          },
          "replicationSequenceNumber" : 65032
        }
      }

    .. code-tab:: json R

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.1.0",
        "extractRegion" : {
          "spatialExtent" : {
            "type" : "Polygon",
            "coordinates" : [ [ [ -180.0, -90.0 ], [ 180.0, -90.0 ], [ 180.0, 90.0 ], [ -180.0, 90.0 ], [ -180.0, -90.0 ] ] ]
          },
          "temporalExtent" : {
            "fromTimestamp" : "2007-10-08T00:00:00Z",
            "toTimestamp" : "2020-02-12T23:00:00Z"
          },
          "replicationSequenceNumber" : 65032
        }
      }
