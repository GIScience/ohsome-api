Time
====

The temporal filter is defined via the ``time`` query parameter. It consists of one or more
ISO-8601 conform timestring(s).

.. note:: The ohsome API only supports the UTC time zone: ``Z``

Supported time formats
----------------------

* timestamp: ``2014-01-01``
* list of timestamps: ``2014-01-01,2015-07-01,2018-10-10``
* interval: ``2014-01-01/2018-01-01/P1Y``

detailed information on timestamp formats and how to use the earliest/latest timestamps:

    * ``YYYY-MM-DD or YYYY-MM-DDThh:mm:ss``: if 'T' is given, hh:mm must also be given. 
    * ``YYYY-MM-DD/YYYY-MM-DD``: start/end timestamps
    * ``YYYY-MM-DD/YYYY-MM-DD/PnYnMnD``: start/end/period where n refers to the size of the respective period
    * ``/YYYY-MM-DD``: #/end where # equals the earliest timestamp in the OSHDB
    * ``/YYYY-MM-DD/PnYnMnD``: #/end/period
    * ``YYYY-MM-DD/``: start/# where # equals the latest timestamp in the OSHDB
    * ``YYYY-MM-DD//PnYnMnD``: start/#/period where # equals the latest timestamp in the OSHDB
    * ``/``: #/# where # equals the earliest and latest timestamp in the OSHDB
    * ``//PnYnMnD``: #/#/period where # equals the earliest and latest timestamp in the OSHDB

.. note:: If ``-MM-DD`` or just ``-DD`` is not given, ``01`` is used as default for month and day. If ``Thh:mm:ss`` is not given, ``00:00:00Z`` is used as a default for the time.
          If the time parameter is undefined, the latest available timestamp within the underlying OSHDB is used per default. It reflects the time of the last edit in the current data set.

.. note:: If you use the time interval syntax and use an end timestamp that does not perfectly align with the given start
          timestamp and period, you will get a response where the last result has as an end timestamp value which is the
          start timestamp plus a multiple of the period, and not one equal to the given end time. For example, if you
          use ``2010-01-01/2012-02-01/P1Y``, the actually used final timestamp will be ``2012-01-01``.