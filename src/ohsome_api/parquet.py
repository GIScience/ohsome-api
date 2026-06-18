import io
import json
import queue
from copy import deepcopy

import pyarrow as pa
import pyarrow.parquet as pq
import pyproj
from typing_extensions import Buffer

from ohsome_api.models import ExtractionRow

EXTRACTION_SCHEMA = pa.schema(
    [
        ("osm_type", pa.string()),
        ("osm_id", pa.int64()),
        ("last_edit", pa.timestamp("us", tz="UTC")),
        ("osm_version", pa.int32()),
        ("osm_edits", pa.int32()),
        ("osm_user_id", pa.int32()),
        ("osm_user_name", pa.string()),
        ("osm_changeset_id", pa.int64()),
        ("osm_tags", pa.map_(pa.string(), pa.string())),
        (
            "bbox",
            pa.struct(
                [
                    ("xmin", pa.float64()),
                    ("xmax", pa.float64()),
                    ("ymin", pa.float64()),
                    ("ymax", pa.float64()),
                ]
            ),
        ),
        ("geom", pa.binary()),
        ("clipped", pa.bool_()),
    ]
)

GEOPARQUET_META = {
    "version": "1.1.0",
    "primary_column": "geom",
    "columns": {
        "geom": {
            "encoding": "WKB",
            "geometry_types": [
                "Point",
                "LineString",
                "Polygon",
                "MultiPolygon",
            ],
            "crs": json.loads(pyproj.CRS.from_epsg(4326).to_json()),
            "covering": {
                "bbox": {
                    "xmin": ["bbox", "xmin"],
                    "ymin": ["bbox", "ymin"],
                    "xmax": ["bbox", "xmax"],
                    "ymax": ["bbox", "ymax"],
                }
            },
        }
    },
}


def _geoparquet_meta(xmin: float, ymin: float, xmax: float, ymax: float) -> bytes:
    # https://geoparquet.org/releases/v1.1.0/
    meta = deepcopy(GEOPARQUET_META)
    meta["columns"]["geom"]["bbox"] = [xmin, ymin, xmax, ymax]  # type: ignore
    return json.dumps(meta).encode()


# Sync file-like object for PyArrow.
#
# PyArrow calls write() from a worker thread.
# FastAPI consumes bytes asynchronously from the queue.


class QueueBytesIO(io.RawIOBase):
    def __init__(self, max_chunks: int) -> None:
        self.queue: queue.Queue[bytes | None] = queue.Queue(maxsize=max_chunks)
        self.position = 0

    # TODO: do we need this?
    def writable(self) -> bool:
        return True

    def write(self, buffer: Buffer, /) -> int:
        data = bytes(buffer)
        self.position += len(data)
        self.queue.put(data)  # blocks for backpressure; safe from any thread
        return len(data)

    # TODO: do we need this?
    def tell(self) -> int:
        return self.position

    def close(self) -> None:
        self.queue.put(None)  # sentinel to signal end of stream


class AsyncParquetSink:
    def __init__(self, max_chunks: int = 8) -> None:
        self.io: QueueBytesIO = QueueBytesIO(max_chunks)
        self._closed = False
        self.xmin = float("inf")
        self.ymin = float("inf")
        self.xmax = float("-inf")
        self.ymax = float("-inf")
        self.writer = pq.ParquetWriter(
            self.io,
            schema=EXTRACTION_SCHEMA,
            compression="zstd",
        )

    def write_batch(self, batch: list[ExtractionRow]) -> None:
        if self._closed:
            raise ValueError("I/O operation on closed file.")
        table = pa.Table.from_arrays(
            [
                pa.array([r["osm_type"] for r in batch], type=pa.string()),
                pa.array([r["osm_id"] for r in batch], type=pa.int64()),
                pa.array(
                    [r["valid_from"] for r in batch], type=pa.timestamp("us", tz="UTC")
                ),
                pa.array([r["osm_version"] for r in batch], type=pa.int32()),
                pa.array([r["osm_edits"] for r in batch], type=pa.int32()),
                pa.array([r["user_id"] for r in batch], type=pa.int32()),
                pa.array([r["user_name"] for r in batch], type=pa.string()),
                pa.array([r["changeset_id"] for r in batch], type=pa.int64()),
                pa.array(
                    [list(r["tags"].items()) for r in batch],
                    type=pa.map_(pa.string(), pa.string()),
                ),
                pa.StructArray.from_arrays(
                    [
                        pa.array([r["xmin"] for r in batch], type=pa.float64()),
                        pa.array([r["xmax"] for r in batch], type=pa.float64()),
                        pa.array([r["ymin"] for r in batch], type=pa.float64()),
                        pa.array([r["ymax"] for r in batch], type=pa.float64()),
                    ],
                    names=["xmin", "xmax", "ymin", "ymax"],
                ),
                pa.array([r["geom"] for r in batch], type=pa.binary()),
                pa.array([r["clipped"] for r in batch], type=pa.bool_()),
            ],
            schema=EXTRACTION_SCHEMA,
        )
        self.xmin = min(self.xmin, *(r["xmin"] for r in batch))
        self.ymin = min(self.ymin, *(r["ymin"] for r in batch))
        self.xmax = max(self.xmax, *(r["xmax"] for r in batch))
        self.ymax = max(self.ymax, *(r["ymax"] for r in batch))

        self.writer.write_table(table, row_group_size=10000)

    def close(self) -> None:
        self._closed = True
        self.writer.add_key_value_metadata(
            {b"geo": _geoparquet_meta(self.xmin, self.ymin, self.xmax, self.ymax)}
        )
        self.writer.close()
        self.io.close()
