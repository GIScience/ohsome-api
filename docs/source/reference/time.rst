.. reference guides are technical descriptions of the machinery and how to
   operate it. reference material is information-oriented.
   https://diataxis.fr/

Time
----

The ``time`` parameter allows to control the temporal extent of the request.

Depending on the endpoint this temporal extent is defined differently:
as single timestamp, time series, time bins or time range.

Timestamp
^^^^^^^^^

A single point in time (snapshot) of OSM data.


.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``time``
     - Single timestamp.
       Earliest allowed: ``2007-10-08``.
       Shorthand: ``latest``.
     - `ISO-8601`_ (UTC)

Examples:

.. code-block:: json

    "time": "2007-10-08T00:00:00Z"

.. code-block:: json

    "time": "latest"


Time Range
^^^^^^^^^^

.. code-block:: text

   Timerange: [===]
   The range contains all OSM data for its temporal duration

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - `ISO-8601`_ (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - `ISO-8601`_ (UTC)

Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
      }


Time Series
^^^^^^^^^^^

.. code-block:: text

   Timeseries: *----*-----*-----*
   Snapshots:  1    2     3     4
   Each * is a snapshot of OSM data at that point in the series
   Number of snapshots is restricted to a maximum of 1000.

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - `ISO-8601`_ (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - `ISO-8601`_ (UTC)
   * - ``interval``
     - Temporal duration between each point in the series (snapshot).
     - `ISO-8601 duration`_


Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
        "interval": "P1M"
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
        "interval": "P1Y"
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
        "interval": "P1D"
      }


Time Bins
^^^^^^^^^

.. code-block:: text

   Timebins: [===][===][===][===]
   Bins:       1    2    3    4
   Each bin contains all OSM data for its temporal duration
   Number of bins is restricted to a maximum of 1000.

.. list-table::
   :header-rows: 1
   :widths: 10 40 20

   * - Parameter
     - Description
     - Format
   * - ``start``
     - Start timestamp. Earliest allowed: ``2007-10-08``. Shorthand: ``earliest``.
     - `ISO-8601`_ (UTC)
   * - ``end``
     - End timestamp. Must be greater than start. Shorthand: ``latest``.
     - `ISO-8601`_ (UTC)
   * - ``binSize``
     - Temporal duration of each bin:
     - `ISO-8601 duration`_

Examples:

.. code-block:: json

    "time": {
        "start": "2025-01-01T00:00:00Z",
        "end": "2026-01-01T00:00:00Z",
        "binSize": "P1M"
      }

.. code-block:: json

    "time": {
        "start": "earliest",
        "end": "2026-01-01T00:00:00Z",
        "binSize": "P1Y"
      }


.. code-block:: json

    "time": {
        "start": "2026-01-01T00:00:00Z",
        "end": "latest",
        "binSize": "P1D"
      }

.. _ISO-8601: https://en.wikipedia.org/wiki/ISO_8601
.. _`ISO-8601 duration`: https://en.wikipedia.org/wiki/ISO_8601#Durations
