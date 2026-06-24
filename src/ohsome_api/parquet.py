import io
import json
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


class DummyBytesIO(io.RawIOBase):
    def __init__(self) -> None:
        self.position = 0
        self.data: list[bytes] = []

    # TODO: do we need this? Yes it is called from ParquetWriter in the beginning
    def writable(self) -> bool:
        return True

    def write(
        self,
        buffer: Buffer,
    ) -> int:
        data: bytes = bytes(buffer)
        self.position += len(data)
        self.data.append(data)
        return len(data)

    def fetch_all(self) -> list[bytes]:
        res = self.data
        self.data = []
        return res

    # TODO: do we need this?
    def tell(self) -> int:
        return self.position

    def close(self) -> None:
        pass


def bbox(r: ExtractionRow) -> dict[str, float]:
    return {
        "xmin": r["xmin"],
        "xmax": r["xmax"],
        "ymin": r["ymin"],
        "ymax": r["ymax"],
    }


class AsyncParquetSink:
    def __init__(self) -> None:
        self.io: DummyBytesIO = DummyBytesIO()
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

    def write_batch(self, rows: list[ExtractionRow]) -> None:
        if self._closed:
            raise ValueError("I/O operation on closed file.")
        batch = pa.RecordBatch.from_arrays(
            #        batch = pa.record_batch(
            [
                [r["osm_type"] for r in rows],
                [r["osm_id"] for r in rows],
                [r["valid_from"].timestamp() * 1000000 for r in rows],
                [r["osm_version"] for r in rows],
                [r["osm_edits"] for r in rows],
                [r["user_id"] for r in rows],
                [r["user_name"] for r in rows],
                [r["changeset_id"] for r in rows],
                [r["tags"] for r in rows],
                [bbox(r) for r in rows],
                [r["geom"] for r in rows],
                [r["clipped"] for r in rows],
            ],
            schema=EXTRACTION_SCHEMA,
        )
        self.xmin = min(self.xmin, *(r["xmin"] for r in rows))
        self.ymin = min(self.ymin, *(r["ymin"] for r in rows))
        self.xmax = max(self.xmax, *(r["xmax"] for r in rows))
        self.ymax = max(self.ymax, *(r["ymax"] for r in rows))

        self.writer.write(batch, row_group_size=10000)

    def close(self) -> None:
        self._closed = True
        self.writer.add_key_value_metadata(
            {b"geo": _geoparquet_meta(self.xmin, self.ymin, self.xmax, self.ymax)}
        )
        self.writer.close()
        self.io.close()
