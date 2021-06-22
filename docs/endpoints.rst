API Endpoints
=============

.. note:: For **POST requests** the fields are given analogous to **GET requests**. When you just have a smaller set of spatial parameters,
    a GET request fits perfectly. POST mostly makes sense when you start to use GeoJSON as input geometries.
    
    The usage of the parameters **types**, **keys** and **values** is not recommended as they are deprecated. Please use the 
    filter_ parameter for your requests.

Elements Aggregation
--------------------

.. http:post :: /elements/(aggregation)

   Get ``aggregation`` of OSM elements.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   
   :query <boundary>: One of these boundary parameters: bboxes_, bcircles_, bpolys_. See boundaries_
   :query time: ISO-8601 conform timestring(s); default: latest timestamp in the OSHDB, see time_
   :query filter: combines several attributive filters: OSM type, geometry (simple feature) type, as well as the OSM tag; See filter_
   :query format: 'json' or 'csv'; default: 'json'
   :query showMetadata: add additional metadata information to the response: 'true', 'false', 'yes', 'no'; default: 'false'
   :query timeout: custom timeout to limit the processing time in seconds; default: dependent on server settings, retrievable via the /metadata request
   :query types: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM type(s) ‘node’ and/or ‘way’ and/or ‘relation’ OR simple feature type(s) ‘point’ and/or ‘line’ and/or 'polygon’ and/or 'other'; default: all three OSM types
   :query keys: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM key(s) given as a list and combined with the 'AND' operator; default: empty
   :query values: Deprecated! Use **filter** parameter instead! Old parameter which allowed to specify OSM value(s) given as a list and combined with the 'AND' operator; values(n) MUST fit to keys(n); default: empty

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
       "apiVersion" : "1.4.2",
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
       "apiVersion" : "1.4.2",
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
       "apiVersion" : "1.4.2",
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
       "apiVersion" : "1.4.2",
       "result" : [ {
         "timestamp" : "2014-01-01T00:00:00Z",
         "value" : 1.020940258E7
       } ]
     }

.. http:post :: /elements/(aggregation)/density

   Get density of ``aggregation`` of OSM elements in the total query area per square-kilometers.

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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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

.. note:: The result of a **ratio request** may contain the value **"NaN"**, when the ratio calculation involves a division of zero by zero.

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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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
               "apiVersion" : "1.4.2",
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
   
   .. note:: ``groupByKeys``, ``groupByKey`` and ``groupByValues`` are resource-specific parameters.
   
   :query <other>: see above_
   :query groupByKeys: see key_
   :query groupByKey: see tag_
   :query groupByValues: see tag_

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
              "apiVersion" : "1.4.2",
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
              "apiVersion" : "1.4.2",
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
              "apiVersion" : "1.4.2",
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
              "apiVersion" : "1.4.2",
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
   * grouping type: one of boundary_, tag_, type_.

   :query <other>: see above_
   :query groupByKey: see tag_
   :query groupByValues: see tag_

.. http:post :: /elements/(aggregation)/groupBy/boundary/groupBy/tag

   Get ``aggregation`` of OSM elements grouped by ``boundary`` and ``tag``.

   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
   * grouping type: `boundary and tag`_.
   
   :query <other>: see above_
   :query groupByKey: see tag_ 
   :query groupByValues: see tag_

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
             "apiVersion" : "1.4.2",
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
             "apiVersion" : "1.4.2",
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
             "apiVersion" : "1.4.2",
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
             "apiVersion" : "1.4.2",
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

.. http:post :: /elements/(aggregation)/ratio/groupBy/boundary

   Get ``ratio`` of ``aggregation`` of OSM elements grouped by ``boundary``.
   
   * aggregation type: one of ``area``, ``count``, ``length``, ``perimeter``
	
   :query <other>: see above_
   :query filter2: see filter_
   :query keys2: Deprecated! see **filter2**
   :query types2: Deprecated! use **filter2**
   :query values2: Deprecated! see **filter2**

Users Aggregation
-----------------

.. http:post :: /users/count

    Get ``aggregation`` statistics about OSM users. List of endpoints:
    
    * **/count**
    * **/count/groupBy/(groupType)**
    * **/count/density**
    * **/count/density/groupBy/(boundary or tag or type)**

    * grouping type: one of boundary_, key_, tag_, type_.
    
     .. note:: ``groupByKeys``, ``groupByKey`` and ``groupByValues`` are resource-specific parameters.
    
    :query <other>: see above_
    :query groupByKeys: see key_
    :query groupByKey: see tag_
    :query groupByValues: see tag_

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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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

.. note:: For endpoint description, grouping types and query parameters of the endpoints **/count/groupBy/(groupType)**, **/count/density** and **/count/density/groupBy/(groupType)**, please refer to the corresponding `/elements/(aggregation)`_ endpoints.

Contributions Aggregation
-------------------------
      
.. http:post :: /contributions/count

   Get the count of the contributions provided to the OSM data. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters. List of endpoints:
    
    * **/count**
    * **/count/density**
    * **/latest/count**
    * **/latest/count/density**

   :query <other>: see above_
   :query contributionType: filters contributions by contribution type: 'creation', 'deletion', 'tagChange', 'geometryChange' or a combination of them; default: empty;
   
.. note:: The **/contributions/count** endpoint is a new feature that is in the experimental status, meaning it is still under internal evaluation and might be subject to changes in the upcoming minor or patch releases.
.. note:: If the ``contributionType`` parameter is let empty, the result could contain contributions that do not effect geometries or tags.
.. note:: In case of multiple time intervals using the **/contribution/latest** endpoints, a contribution is present in a time interval only if this is the time interval in which the latest contribution of the entity happend.

**Example request**:

Number of contributions to the building 'Stadthalle Heidelberg' between 2010 and 2020.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/count?bboxes=8.699053,49.411842,8.701311,49.412893&filter=id:way/140112810&time=2010-01-01,2020-01-01'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/count' --data-urlencode 'bboxes=8.699053,49.411842,8.701311,49.412893' --data-urlencode 'time=2010-01-01,2020-01-01' --data-urlencode 'filter=id:way/140112810'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/count'
        data = {"bboxes": "8.699053,49.411842,8.701311,49.412893", "time": "2010-01-01,2020-01-01", "filter": "id:way/140112810"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/count", encode = "form", body = list(bboxes = "8.699053,49.411842,8.701311,49.412893", time = "2010-01-01,2020-01-01", filter = "id:way/140112810"))
        r

**Example response**:

  .. tabs::

   .. code-tab:: json curl (GET)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2010-01-01T00:00:00Z",
	      "toTimestamp" : "2020-01-01T00:00:00Z",
	      "value" : 15.0
	    }
	  ]
	}

   .. code-tab:: json curl (POST)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2010-01-01T00:00:00Z",
	      "toTimestamp" : "2020-01-01T00:00:00Z",
	      "value" : 15.0
	    }
	  ]
	}

   .. code-tab:: json Python

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2010-01-01T00:00:00Z",
	      "toTimestamp" : "2020-01-01T00:00:00Z",
	      "value" : 15.0
	    }
	  ]
	}

   .. code-tab:: json R

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2010-01-01T00:00:00Z",
	      "toTimestamp" : "2020-01-01T00:00:00Z",
	      "value" : 15.0
	    }
	  ]
	}

.. http:post :: /contributions/count/density

   Get the density of the count of contributions in the total query area in counts per square-kilometers. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.

**Example request**:

Density of contributions to shops within the oldtown area of Heidelberg between 2012 and 2016.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/count/density?bboxes=8.69282,49.40766,8.71673,49.4133&filter=shop=*%20and%20type:node&time=2012-01-01,2016-01-01'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/count/density' --data-urlencode 'bboxes=8.69282,49.40766,8.71673,49.4133' --data-urlencode 'time=2012-01-01,2016-01-01' --data-urlencode 'filter=shop=* and type:node'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/count/density'
        data = {"bboxes": "8.69282,49.40766,8.71673,49.4133", "time": "2012-01-01,2016-01-01", "filter": "shop=* and type:node"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/count/density", encode = "form", body = list(bboxes = "8.69282,49.40766,8.71673,49.4133", time = "2012-01-01,2016-01-01", filter = "shop=* and type:node"))
        r

**Example response**:

  .. tabs::

   .. code-tab:: json curl (GET)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 417.13
	    }
	  ]
	}

   .. code-tab:: json curl (POST)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 417.13
	    }
	  ]
	}

   .. code-tab:: json Python

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 417.13
	    }
	  ]
	}

   .. code-tab:: json R

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 417.13
	    }
	  ]
	}
	
.. http:post :: /contributions/latest/count

   Get the count of the latest contributions provided to the OSM data. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.

**Example request**:

Number of the latest contributions to residential buildings with a geometry change within the oldtown area of Heidelberg in 2014.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/latest/count?bboxes=8.69282,49.40766,8.71673,49.4133&contributionType=geometryChange&filter=building=residential&time=2014-01-01/2015-01-01'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/latest/count' --data-urlencode 'bboxes=8.69282,49.40766,8.71673,49.4133' --data-urlenconde 'contributionType=geometryChange' --data-urlencode 'filter=building=residential' --data-urlencode 'time=2014-01-01,2015-01-01'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/latest/count'
        data = {"bboxes": "8.69282,49.40766,8.71673,49.4133", "contributionType": "geometryChange", "filter": "building=residential", "time": "2014-01-01,2015-01-01"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/latest/count", encode = "form", body = list(bboxes = "8.69282,49.40766,8.71673,49.4133", contributionType= "geometryChange", filter = "building=residential", time = "2014-01-01,2015-01-01"))
        r

**Example response**:

  .. tabs::

   .. code-tab:: json curl (GET)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.5.0",
	  "result" : [
	    {
	      "fromTimestamp" : "2014-01-01T00:00:00Z",
	      "toTimestamp" : "2015-01-01T00:00:00Z",
	      "value" : 5
	    }
	  ]
	}

   .. code-tab:: json curl (POST)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.5.0",
	  "result" : [
	    {
	      "fromTimestamp" : "2014-01-01T00:00:00Z",
	      "toTimestamp" : "2015-01-01T00:00:00Z",
	      "value" : 5
	    }
	  ]
	}

   .. code-tab:: json Python

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.5.0",
	  "result" : [
	    {
	      "fromTimestamp" : "2014-01-01T00:00:00Z",
	      "toTimestamp" : "2015-01-01T00:00:00Z",
	      "value" : 5
	    }
	  ]
	}

   .. code-tab:: json R

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.5.0",
	  "result" : [
	    {
	      "fromTimestamp" : "2014-01-01T00:00:00Z",
	      "toTimestamp" : "2015-01-01T00:00:00Z",
	      "value" : 5
	    }
	  ]
	}

.. http:post :: /contributions/latest/count/density

  Get the density of the count of the latest contributions in the total query area in counts per square-kilometers. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.

**Example request**:

Density of the latest contributions with a geometry change to shops within the oldtown area of Heidelberg between 2012 and 2016.

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/latest/count/density?bboxes=8.69282,49.40766,8.71673,49.4133&filter=shop=* and type:node&time=2012-01-01,2016-01-01&contributionType=geometryChange'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/latest/count/density' --data-urlencode 'bboxes=8.69282,49.40766,8.71673,49.4133' --data-urlencode 'time=2012-01-01,2016-01-01' --data-urlencode 'filter=shop=* and type:node' --data-urlencode 'contributionType=geometryChange'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/latest/count/density'
        data = {"bboxes": "8.69282,49.40766,8.71673,49.4133", "time": "2012-01-01,2016-01-01", "filter": "shop=* and type:node", "contributionType": "geometryChange"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/latest/count/density", encode = "form", body = list(bboxes = "8.69282,49.40766,8.71673,49.4133", time = "2012-01-01,2016-01-01", filter = "shop=* and type:node", contributionType = "geometryChange"))
        r

**Example response**:

  .. tabs::

   .. code-tab:: json curl (GET)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 28.48
	    }
	  ]
	}

   .. code-tab:: json curl (POST)

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 28.48
	    }
	  ]
	}

   .. code-tab:: json Python

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 28.48
	    }
	  ]
	}

   .. code-tab:: json R

	{
	  "attribution" : {
	    "url" : "https://ohsome.org/copyrights",
	    "text" : "© OpenStreetMap contributors"
	  },
	  "apiVersion" : "1.4.2",
	  "result" : [
	    {
	      "fromTimestamp" : "2012-01-01T00:00:00Z",
	      "toTimestamp" : "2016-01-01T00:00:00Z",
	      "value" : 28.48
	    }
	  ]
	}

Elements Extraction
-------------------

.. http:post :: /elements/(geometryType)

   Get the state of OSM data at the given timestamp(s) as a GeoJSON feature collection where object geometries are returned as the given geometry type (``geometry``, ``bbox``, or ``centroid``).

   :query <other>: see above_ (except **format**)
   :query time: required; format same as described in time_
   :query properties: specifies what properties should be included for each feature representing an OSM element: ‘tags’ and/or 'metadata’; multiple values can be delimited by commas; default: empty
   :query clipGeometry: boolean operator to specify whether the returned geometries of the features should be clipped to the query's spatial boundary (‘true’), or not (‘false’); default: ‘true’
   
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

Elements Full History Extraction
--------------------------------

.. http:post :: /elementsFullHistory/(geometryType)

   Get the full history of OSM data as a GeoJSON feature collection. All changes to matching OSM features are included with corresponding ``validFrom`` and ``validTo`` timestamps.
   This endpoint supports the geometry types ``bbox``, ``centroid`` and ``geometry``.

   :query <other>: see above_ (except **format**)
   :query time: required; must consist of two ISO-8601 conform timestrings defining a time interval; no default value
   :query properties: specifies what properties should be included for each feature representing an OSM element: ‘tags’ and/or 'metadata’; multiple values can be delimited by commas; default: empty
   :query clipGeometry: sboolean operator to specify whether the returned geometries of the features should be clipped to the query's spatial boundary (‘true’), or not (‘false’); default: ‘true’

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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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
    
Contributions Extraction
------------------------

.. http:post :: /contributions/(geometryType)

   Get the contributions provided to the OSM data. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.
   This endpoint supports the geometry types ``bbox``, ``centroid`` and ``geometry``.
   
   :query <other>: see above_ (except **format**)
   :query time: required; must consist of two ISO-8601 conform timestrings defining a time interval; no default value
   :query properties: specifies what properties should be included for each feature representing an OSM element: ‘tags’ and/or 'metadata’ and/or 'contributionTypes'; metadata gets also the contribution types until v2.0; multiple values can be delimited by commas; no default value
   :query clipGeometry:  boolean operator to specify whether the returned geometries of the features should be clipped to the query's spatial boundary (‘true’), or not (‘false’); default: ‘true’

**Example request**:

Get the changes of pharmacies with opening hours in a certain area of Heidelberg in times of COVID-19

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/geometry?bboxes=8.6720%2C49.3988%2C8.7026%2C49.4274&filter=amenity=pharmacy%20and%20opening_hours=*%20and%20type:node&time=2020-02-01%2C2020-06-29&showMetadata=yes&properties=metadata%2Ctags&clipGeometry=false'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/geometry' --data-urlencode 'bboxes=8.6720,49.3988,8.7026,49.4274' --data-urlencode 'time=2020-02-01,2020-06-29' --data-urlencode 'filter=amenity=pharmacy and opening_hours=* and type:node' --data-urlencode 'showMetadata=yes' --data-urlencode 'properties=metadata,tags' --data-urlencode 'clipGeometry=false'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/geometry'
        data = {"bboxes": "8.6720,49.3988,8.7026,49.4274", "time": "2020-02-01,2020-06-29", "filter": "amenity=pharmacy and opening_hours=* and type:node", "showMetadata": "yes", "properties": "metadata,tags", "clipGeometry": "false"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/geometry", encode = "form", body = list(bboxes = "8.6720,49.3988,8.7026,49.4274", time = "2020-02-01,2020-06-29", filter = "amenity=pharmacy and opening_hours=* and type:node", showMetadata = "yes", properties = "metadata,tags", clipGeometry = "false"))
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
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6720,49.3988,8.7026,49.4274&filter=amenity=pharmacy%20and%20opening_hours=*%20and%20type:node&time=2020-02-01,2020-06-29&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6902451,
              49.4080159
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/323191854",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 8,
            "addr:city" : "Heidelberg",
            "addr:housenumber" : "24",
            "addr:postcode" : "69115",
            "addr:street" : "Poststraße",
            "amenity" : "pharmacy",
            "contact:email" : "aesculap-heidelberg@web.de",
            "contact:fax" : "+49 6221 163746",
            "contact:phone" : "+49 6221 27634",
            "dispensing" : "yes",
            "healthcare" : "pharmacy",
            "name" : "Aesculap Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-13:00",
            "operator" : "Stefan Wowra",
            "website" : "https://aesculap-heidelberg.de",
            "wheelchair" : "yes"
          }
        }, { ...
        }, {
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6922106,
              49.4103048
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/5400804545",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 2,
            "amenity" : "pharmacy",
            "dispensing" : "yes",
            "fax" : "+49 6221 9831332",
            "healthcare" : "pharmacy",
            "name" : "ATOS-Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-14:00",
            "phone" : "+49 6221 9831331",
            "website" : "http://www.atos-apotheke.de",
            "wheelchair" : "yes"
          }
        }]
      }

    .. code-tab:: text curl (POST)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6720,49.3988,8.7026,49.4274&filter=amenity=pharmacy%20and%20opening_hours=*%20and%20type:node&time=2020-02-01,2020-06-29&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6902451,
              49.4080159
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/323191854",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 8,
            "addr:city" : "Heidelberg",
            "addr:housenumber" : "24",
            "addr:postcode" : "69115",
            "addr:street" : "Poststraße",
            "amenity" : "pharmacy",
            "contact:email" : "aesculap-heidelberg@web.de",
            "contact:fax" : "+49 6221 163746",
            "contact:phone" : "+49 6221 27634",
            "dispensing" : "yes",
            "healthcare" : "pharmacy",
            "name" : "Aesculap Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-13:00",
            "operator" : "Stefan Wowra",
            "website" : "https://aesculap-heidelberg.de",
            "wheelchair" : "yes"
          }
        }, { ...
        }, {
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6922106,
              49.4103048
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/5400804545",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 2,
            "amenity" : "pharmacy",
            "dispensing" : "yes",
            "fax" : "+49 6221 9831332",
            "healthcare" : "pharmacy",
            "name" : "ATOS-Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-14:00",
            "phone" : "+49 6221 9831331",
            "website" : "http://www.atos-apotheke.de",
            "wheelchair" : "yes"
          }
        }]
      }

    .. code-tab:: text Python

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6720,49.3988,8.7026,49.4274&filter=amenity=pharmacy%20and%20opening_hours=*%20and%20type:node&time=2020-02-01,2020-06-29&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6902451,
              49.4080159
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/323191854",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 8,
            "addr:city" : "Heidelberg",
            "addr:housenumber" : "24",
            "addr:postcode" : "69115",
            "addr:street" : "Poststraße",
            "amenity" : "pharmacy",
            "contact:email" : "aesculap-heidelberg@web.de",
            "contact:fax" : "+49 6221 163746",
            "contact:phone" : "+49 6221 27634",
            "dispensing" : "yes",
            "healthcare" : "pharmacy",
            "name" : "Aesculap Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-13:00",
            "operator" : "Stefan Wowra",
            "website" : "https://aesculap-heidelberg.de",
            "wheelchair" : "yes"
          }
        }, { ...
        }, {
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6922106,
              49.4103048
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/5400804545",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 2,
            "amenity" : "pharmacy",
            "dispensing" : "yes",
            "fax" : "+49 6221 9831332",
            "healthcare" : "pharmacy",
            "name" : "ATOS-Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-14:00",
            "phone" : "+49 6221 9831331",
            "website" : "http://www.atos-apotheke.de",
            "wheelchair" : "yes"
          }
        }]
      }

    .. code-tab:: text R

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6720,49.3988,8.7026,49.4274&filter=amenity=pharmacy%20and%20opening_hours=*%20and%20type:node&time=2020-02-01,2020-06-29&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6902451,
              49.4080159
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/323191854",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 8,
            "addr:city" : "Heidelberg",
            "addr:housenumber" : "24",
            "addr:postcode" : "69115",
            "addr:street" : "Poststraße",
            "amenity" : "pharmacy",
            "contact:email" : "aesculap-heidelberg@web.de",
            "contact:fax" : "+49 6221 163746",
            "contact:phone" : "+49 6221 27634",
            "dispensing" : "yes",
            "healthcare" : "pharmacy",
            "name" : "Aesculap Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-13:00",
            "operator" : "Stefan Wowra",
            "website" : "https://aesculap-heidelberg.de",
            "wheelchair" : "yes"
          }
        }, { ...
        }, {
          "type" : "Feature",
          "geometry" : {
            "type" : "Point",
            "coordinates" : [
              8.6922106,
              49.4103048
            ]
          },
          "properties" : {
            "@changesetId" : 83099383,
            "@osmId" : "node/5400804545",
            "@osmType" : "NODE",
            "@tagChange" : true,
            "@timestamp" : "2020-04-05T13:32:50Z",
            "@version" : 2,
            "amenity" : "pharmacy",
            "dispensing" : "yes",
            "fax" : "+49 6221 9831332",
            "healthcare" : "pharmacy",
            "name" : "ATOS-Apotheke",
            "opening_hours" : "Mo-Fr 08:30-18:30; Sa 09:00-14:00",
            "phone" : "+49 6221 9831331",
            "website" : "http://www.atos-apotheke.de",
            "wheelchair" : "yes"
          }
        }]
      }

.. http:post :: /contributions/latest/(geometryType)

   Get the the latest state of the contributions provided to the OSM data. This endpoint does not support the deprecated ``types``, ``keys``, ``values`` parameters.
   This endpoint supports the geometry types ``bbox``, ``centroid`` and ``geometry``.

   :query <other>: see above_ (except **format**)
   :query time: required; must consist of two ISO-8601 conform timestrings defining a time interval; no default value
   :query properties: specifies what properties should be included for each feature representing an OSM element: ‘tags’ and/or 'metadata’ and/or 'contributionTypes'; metadata gets also the contribution types until v2.0; multiple values can be delimited by commas; no default value
   :query clipGeometry:  boolean operator to specify whether the returned geometries of the features should be clipped to the query's spatial boundary (‘true’), or not (‘false’); default: ‘true’

**Example request**:

Get the latest change of constructions in a certain area of the Bahnstadt in Heidelberg

  .. tabs::

    .. code-tab:: bash curl (GET)

       curl -X GET 'https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6644%2C49.4010%2C8.6663%2C49.4027&filter=landuse=construction%20and%20type:way&time=2014-07-01%2C2020-06-29&showMetadata=yes&properties=metadata%2Ctags&clipGeometry=false'

    .. code-tab:: bash curl (POST)

       curl -X POST 'https://api.ohsome.org/v1/contributions/latest/geometry' --data-urlencode 'bboxes=8.6644,49.4010,8.6663,49.4027' --data-urlencode 'time=2014-07-01,2020-06-29' --data-urlencode 'filter=landuse=construction and type:way' --data-urlencode 'showMetadata=yes' --data-urlencode 'properties=metadata,tags' --data-urlencode 'clipGeometry=false'

    .. code-tab:: python Python

        import requests
        URL = 'https://api.ohsome.org/v1/contributions/latest/geometry'
        data = {"bboxes": "8.6644,49.4010,8.6663,49.4027", "time": "2014-07-01,2020-06-29", "filter": "landuse=construction and type:way", "showMetadata": "yes", "properties": "metadata,tags", "clipGeometry": "false"}
        response = requests.post(URL, data=data)
        print(response.json())

    .. code-tab:: r R

        library(httr)
        r <- POST("https://api.ohsome.org/v1/contributions/latest/geometry", encode = "form", body = list(bboxes = "8.6644,49.4010,8.6663,49.4027", time = "2014-07-01,2020-06-29", filter = "landuse=construction and type:way", showMetadata = "yes", properties = "metadata,tags", clipGeometry = "false"))
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
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6644159,49.401099,8.6663353,49.4027195&filter=landuse=construction%20and%20type:way&time=2020-06-29,2014-07-01&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.6654314,
                  49.4026779
                ], ...
          },
          "properties" : {
            "@changesetId" : 85604249,
            "@geometryChange" : true,
            "@osmId" : "way/795435536",
            "@osmType" : "WAY",
            "@timestamp" : "2020-05-22T10:22:53Z",
            "@version" : 3,
            "landuse" : "construction"
          }
        }, {
          "type" : "Feature",
          "geometry" : null,
          "properties" : {
            "@changesetId" : 51902131,
            "@deletion" : true,
            "@osmId" : "way/135635599",
            "@osmType" : "WAY",
            "@timestamp" : "2017-09-10T09:22:03Z",
            "@version" : 9
          }
        }]
      }

    .. code-tab:: text curl (POST)

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6644159,49.401099,8.6663353,49.4027195&filter=landuse=construction%20and%20type:way&time=2020-06-29,2014-07-01&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.6654314,
                  49.4026779
                ], ...
          },
          "properties" : {
            "@changesetId" : 85604249,
            "@geometryChange" : true,
            "@osmId" : "way/795435536",
            "@osmType" : "WAY",
            "@timestamp" : "2020-05-22T10:22:53Z",
            "@version" : 3,
            "landuse" : "construction"
          }
        }, {
          "type" : "Feature",
          "geometry" : null,
          "properties" : {
            "@changesetId" : 51902131,
            "@deletion" : true,
            "@osmId" : "way/135635599",
            "@osmType" : "WAY",
            "@timestamp" : "2017-09-10T09:22:03Z",
            "@version" : 9
          }
        }]
      }

    .. code-tab:: text Python

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6644159,49.401099,8.6663353,49.4027195&filter=landuse=construction%20and%20type:way&time=2020-06-29,2014-07-01&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.6654314,
                  49.4026779
                ], ...
          },
          "properties" : {
            "@changesetId" : 85604249,
            "@geometryChange" : true,
            "@osmId" : "way/795435536",
            "@osmType" : "WAY",
            "@timestamp" : "2020-05-22T10:22:53Z",
            "@version" : 3,
            "landuse" : "construction"
          }
        }, {
          "type" : "Feature",
          "geometry" : null,
          "properties" : {
            "@changesetId" : 51902131,
            "@deletion" : true,
            "@osmId" : "way/135635599",
            "@osmType" : "WAY",
            "@timestamp" : "2017-09-10T09:22:03Z",
            "@version" : 9
          }
        }]
      }

    .. code-tab:: text R

      {
        "attribution" : {
          "url" : "https://ohsome.org/copyrights",
          "text" : "© OpenStreetMap contributors"
        },
        "apiVersion" : "1.4.2",
        "metadata" : {
          "description" : "Latest contributions as GeoJSON features.",
          "requestUrl" : "https://api.ohsome.org/v1/contributions/latest/geometry?bboxes=8.6644159,49.401099,8.6663353,49.4027195&filter=landuse=construction%20and%20type:way&time=2020-06-29,2014-07-01&showMetadata=yes&properties=metadata,tags&clipGeometry=false"
        },
        "type" : "FeatureCollection",
        "features" : [{
          "type" : "Feature",
          "geometry" : {
            "type" : "Polygon",
            "coordinates" : [
              [
                [
                  8.6654314,
                  49.4026779
                ], ...
          },
          "properties" : {
            "@changesetId" : 85604249,
            "@geometryChange" : true,
            "@osmId" : "way/795435536",
            "@osmType" : "WAY",
            "@timestamp" : "2020-05-22T10:22:53Z",
            "@version" : 3,
            "landuse" : "construction"
          }
        }, {
          "type" : "Feature",
          "geometry" : null,
          "properties" : {
            "@changesetId" : 51902131,
            "@deletion" : true,
            "@osmId" : "way/135635599",
            "@osmType" : "WAY",
            "@timestamp" : "2017-09-10T09:22:03Z",
            "@version" : 9
          }
        }]
      }    
      
Metadata
--------

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
        "apiVersion" : "1.4.2",
        "timeout": 600.0,
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
        "apiVersion" : "1.4.2",
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
        "apiVersion" : "1.4.2",
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

.. _boundary: group-by.html#boundary
.. _key: group-by.html#key
.. _tag: group-by.html#tag
.. _type: group-by.html#type
.. _boundary and tag: group-by.html#boundary-and-tag 
.. _bboxes: boundaries.html#bboxes
.. _bcircles: boundaries.html#bcircles
.. _bpolys: boundaries.html#bpolys
.. _boundaries: boundaries.html#boundaries
.. _time: time.html#time
.. _filter: filter.html#filter
.. _above: endpoints.html#post--elements-(aggregation)
.. _/elements/(aggregation): endpoints.html#elements-aggregation
