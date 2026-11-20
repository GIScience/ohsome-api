import io
import json
import logging
from abc import ABC, abstractmethod
from collections.abc import Callable
from copy import deepcopy
from dataclasses import dataclass
from importlib.metadata import version
from operator import itemgetter
from types import TracebackType
from typing import Any

import pyarrow.compute as pc
import pyproj
from pyarrow import (
    Array,
    RecordBatch,
    Schema,
    binary,
    bool_,
    default_memory_pool,
    field,
    float64,
    int32,
    int64,
    map_,
    schema,
    string,
    struct,
    timestamp,
)
from pyarrow.ipc import RecordBatchStreamWriter
from pyarrow.parquet import ParquetWriter

from ohsome_api.models import Attribution, ExtractionRow

logger = logging.getLogger("ohsome-api")

logger.info("Using pyarrow version: %s", version("pyarrow"))
logger.info("Using pyarrow memory pool backend: %s", default_memory_pool().backend_name)


crs_metadata = json.loads(pyproj.CRS.from_epsg(4326).to_json())
# Create the geom field with GeoArrow extension metadata
geom_field = field(
    "geom",
    binary(),
    metadata={
        b"ARROW:extension:name": b"geoarrow.wkb",
        b"ARROW:extension:metadata": json.dumps(crs_metadata).encode("utf-8"),
    },
)

bbox_field = field(
    "bbox",
    struct(
        [
            ("xmin", float64()),
            ("xmax", float64()),
            ("ymin", float64()),
            ("ymax", float64()),
        ]
    ),
)

FEATURE_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("edit_timestamp", timestamp("us", tz="UTC")),  # valid_from / edit_timestamp
        (
            "valid_to_timestamp",
            timestamp("us", tz="UTC"),
        ),  # valid_to from latest 2222-01-01
        ("version", int32()),
        ("minor_version", int32()),
        ("edits", int32()),
        ("user_id", int32()),
        ("user_name", string()),
        ("changeset_id", int64()),  # later hashtags
        ("tags", map_(string(), string())),
        bbox_field,
        ("geom_type", string()),
        geom_field,
        ("clipped", bool_()),
    ]
)

MEMBER_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("edit_timestamp", timestamp("us", tz="UTC")),
        (
            "valid_to_timestamp",
            timestamp("us", tz="UTC"),
        ),  # valid_to from latest 2222-01-01
        ("version", int32()),
        ("minor_version", int32()),
        ("edits", int32()),
        ("user_id", int32()),
        ("user_name", string()),
        ("changeset_id", int64()),
        ("tags", map_(string(), string())),
        ("collection_osm_id", int64()),
        ("member_role", string()),
        ("member_pos", int32()),
        bbox_field,
        ("geom_type", string()),
        geom_field,
        ("clipped", bool_()),
    ]
)

CONTRIBUTION_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("edit_timestamp", timestamp("us", tz="UTC")),  # valid_from / edit_timestamp
        (
            "valid_to_timestamp",
            timestamp("us", tz="UTC"),
        ),  # valid_to from latest 2222-01-01
        ("version", int32()),
        ("minor_version", int32()),
        ("edits", int32()),
        ("user_id", int32()),
        ("user_name", string()),
        ("changeset_id", int64()),
        ("changeset_tags", map_(string(), string())),
        ("contribution_type", string()),
        ("tags", map_(string(), string())),
        ("tags_before", map_(string(), string())),
        bbox_field,
        ("geom_type", string()),
        geom_field,
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
            "crs": crs_metadata,
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


def _api_meta() -> bytes:
    return json.dumps(
        {
            "version": version("ohsome-api"),
            "attribution": Attribution().model_dump(),
        }
    ).encode()


def _bbox(r: ExtractionRow) -> dict[str, float]:
    return {
        "xmin": r["xmin"],
        "xmax": r["xmax"],
        "ymin": r["ymin"],
        "ymax": r["ymax"],
    }


# Output column name -> how to get its value from an ExtractionRow.
# Columns whose name equals the row key need no entry.
_GETTERS: dict[str, Callable[[ExtractionRow], Any]] = {
    "edit_timestamp": itemgetter("valid_from"),
    "valid_to_timestamp": itemgetter("valid_to"),
    "version": itemgetter("osm_version"),
    "minor_version": itemgetter("osm_minor_version"),
    "edits": itemgetter("osm_edits"),
    "contribution_type": itemgetter("contrib_type"),
    "collection_osm_id": itemgetter("part_of"),
    "member_role": itemgetter("part_of_role"),
    "member_pos": itemgetter("part_of_pos"),
    "bbox": _bbox,
}

# Low cardinality string columns which benefit from dictionary encoding.
# Dictionary encoding of unique values (geom, tags, ids) only costs CPU.
_DICTIONARY_COLUMNS = {"osm_type", "user_name", "contribution_type", "geom_type"}


def _min_max(array: Array) -> tuple[Any, Any]:
    # pc.min_max is generated at runtime and unknown to type checkers,
    # call_function is the statically defined equivalent
    result: Any = pc.call_function("min_max", [array])
    return result["min"].as_py(), result["max"].as_py()


@dataclass(frozen=True)
class RowFormat:
    """Output schema of an extraction and how to convert rows into it."""

    schema: Schema

    def to_record_batch(self, rows: list[ExtractionRow]) -> RecordBatch:
        return RecordBatch.from_arrays(
            [
                [getter(r) for r in rows]
                for getter in (
                    _GETTERS.get(name) or itemgetter(name) for name in self.schema.names
                )
            ],
            schema=self.schema,
        )

    @property
    def dictionary_columns(self) -> list[str]:
        return [name for name in self.schema.names if name in _DICTIONARY_COLUMNS]


FEATURES = RowFormat(FEATURE_EXTRACTION_SCHEMA)
MEMBERS = RowFormat(MEMBER_EXTRACTION_SCHEMA)
CONTRIBUTIONS = RowFormat(CONTRIBUTION_EXTRACTION_SCHEMA)


class Sink(ABC):
    """Encodes batches of rows into a byte stream.

    Use as context manager: `write_batch` returns the bytes encoded so far,
    `finish` closes the writer and returns the remaining bytes (footer or
    end-of-stream marker). If the context is left without `finish` (e.g. on an
    error) the writer is closed and the remaining bytes are discarded.
    """

    def __init__(self, row_format: RowFormat) -> None:
        self.row_format = row_format
        self.buffer = io.BytesIO()
        self._closed = False

    def write_batch(self, rows: list[ExtractionRow]) -> bytes:
        if not rows:
            return b""
        self._write(self.row_format.to_record_batch(rows))
        return self._take_bytes()

    def finish(self) -> bytes:
        self._close()
        tail = self._take_bytes()
        self.buffer.close()
        logger.debug("finished sink, %d bytes remaining", len(tail))
        return tail

    def _take_bytes(self) -> bytes:
        content = self.buffer.getvalue()
        self.buffer.seek(0)
        self.buffer.truncate()
        return content

    def _close(self) -> None:
        if not self._closed:
            self._closed = True
            self._close_writer()

    @abstractmethod
    def _write(self, batch: RecordBatch) -> None: ...

    @abstractmethod
    def _close_writer(self) -> None: ...

    def __enter__(self) -> "Sink":
        return self

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc_val: BaseException | None,
        exc_tb: TracebackType | None,
    ) -> None:
        self._close()


class ArrowSink(Sink):
    def __init__(self, row_format: RowFormat) -> None:
        super().__init__(row_format)
        self.writer = RecordBatchStreamWriter(
            self.buffer,
            row_format.schema.with_metadata({b"ohsome API": _api_meta()}),
        )

    def _write(self, batch: RecordBatch) -> None:
        self.writer.write_batch(batch)

    def _close_writer(self) -> None:
        self.writer.close()


class ParquetSink(Sink):
    def __init__(self, row_format: RowFormat) -> None:
        super().__init__(row_format)
        self.xmin = float("inf")
        self.ymin = float("inf")
        self.xmax = float("-inf")
        self.ymax = float("-inf")
        self.writer = ParquetWriter(
            self.buffer,
            schema=row_format.schema,
            compression="zstd",
            use_dictionary=row_format.dictionary_columns,
        )

    def _write(self, batch: RecordBatch) -> None:
        # each write results in (at least) one row group
        self.writer.write_batch(batch)
        self._update_bbox(batch)

    def _update_bbox(self, batch: RecordBatch) -> None:
        bbox = batch.column("bbox")
        xmin, _ = _min_max(bbox.field("xmin"))
        ymin, _ = _min_max(bbox.field("ymin"))
        _, xmax = _min_max(bbox.field("xmax"))
        _, ymax = _min_max(bbox.field("ymax"))
        if xmin is not None:
            self.xmin = min(self.xmin, xmin)
            self.ymin = min(self.ymin, ymin)
            self.xmax = max(self.xmax, xmax)
            self.ymax = max(self.ymax, ymax)

    def _close_writer(self) -> None:
        self.writer.add_key_value_metadata(
            {
                b"geo": _geoparquet_meta(self.xmin, self.ymin, self.xmax, self.ymax),
                b"ohsome API": _api_meta(),
            }
        )
        self.writer.close()
