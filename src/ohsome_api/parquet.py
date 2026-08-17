import io
import json
from abc import ABC, abstractmethod
from copy import deepcopy
from importlib.metadata import version
from types import TracebackType

import pyproj
from pyarrow import (
    RecordBatch,
    binary,
    bool_,
    field,
    float64,
    int32,
    int64,
    map_,
    parquet,
    schema,
    string,
    struct,
    timestamp,
)
from pyarrow.ipc import new_stream

from ohsome_api.models import Attribution, ExtractionRow

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

FEATURE_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("edit_timestamp", timestamp("us", tz="UTC")),  # valid_from / edit_timestamp
        (
            "valid_to_timestamp",
            timestamp("us", tz="UTC"),
        ),  # valid_to from latest 2222-01-01 00:00:00.000 +0000 2525
        ("osm_version", int32()),
        ("minor_version", int32()),
        ("osm_edits", int32()),
        ("osm_user_id", int32()),
        ("osm_user_name", string()),
        ("osm_changeset_id", int64()),  # later hashtags
        ("osm_tags", map_(string(), string())),
        (
            "bbox",
            struct(
                [
                    ("xmin", float64()),
                    ("xmax", float64()),
                    ("ymin", float64()),
                    ("ymax", float64()),
                ]
            ),
        ),
        ("geom_type", string()),
        geom_field,
        ("clipped", bool_()),
    ]
)

MEMBER_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("last_edit", timestamp("us", tz="UTC")),
        ("osm_version", int32()),
        ("minor_version", int32()),
        ("osm_edits", int32()),
        ("osm_user_id", int32()),
        ("osm_user_name", string()),
        ("osm_changeset_id", int64()),
        ("osm_tags", map_(string(), string())),
        ("collection_osm_id", int64()),
        ("member_role", string()),
        ("member_pos", int32()),
        (
            "bbox",
            struct(
                [
                    ("xmin", float64()),
                    ("xmax", float64()),
                    ("ymin", float64()),
                    ("ymax", float64()),
                ]
            ),
        ),
        ("geom_type", string()),
        geom_field,
        ("clipped", bool_()),
    ]
)

CONTRIBUTION_EXTRACTION_SCHEMA = schema(
    [
        ("osm_type", string()),
        ("osm_id", int64()),
        ("valid_from", timestamp("us", tz="UTC")),
        ("valid_to", timestamp("us", tz="UTC")),
        ("osm_version", int32()),
        ("minor_version", int32()),
        ("osm_edits", int32()),
        ("osm_user_id", int32()),
        ("osm_user_name", string()),
        ("osm_changeset_id", int64()),
        ("contribution_type", string()),
        ("osm_tags", map_(string(), string())),
        ("osm_tags_before", map_(string(), string())),
        (
            "bbox",
            struct(
                [
                    ("xmin", float64()),
                    ("xmax", float64()),
                    ("ymin", float64()),
                    ("ymax", float64()),
                ]
            ),
        ),
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


def bbox(r: ExtractionRow) -> dict[str, float]:
    return {
        "xmin": r["xmin"],
        "xmax": r["xmax"],
        "ymin": r["ymin"],
        "ymax": r["ymax"],
    }


def record_batch(rows: list[ExtractionRow]) -> RecordBatch:
    return RecordBatch.from_arrays(
        [
            [r["osm_type"] for r in rows],
            [r["osm_id"] for r in rows],
            [r["valid_from"].timestamp() * 1000000 for r in rows],
            [r["valid_to"].timestamp() * 1000000 for r in rows],
            [r["osm_version"] for r in rows],
            [r["osm_minor_version"] for r in rows],
            [r["osm_edits"] for r in rows],
            [r["user_id"] for r in rows],
            [r["user_name"] for r in rows],
            [r["changeset_id"] for r in rows],
            [r["tags"] for r in rows],
            [bbox(r) for r in rows],
            [r["geom_type"] for r in rows],
            [r["geom"] for r in rows],
            [r["clipped"] for r in rows],
        ],
        schema=FEATURE_EXTRACTION_SCHEMA,
    )


def record_batch_contribution(rows: list[ExtractionRow]) -> RecordBatch:
    return RecordBatch.from_arrays(
        [
            [r["osm_type"] for r in rows],
            [r["osm_id"] for r in rows],
            [r["valid_from"].timestamp() * 1000000 for r in rows],
            [r["valid_to"].timestamp() * 1000000 for r in rows],
            [r["osm_version"] for r in rows],
            [r["osm_minor_version"] for r in rows],
            [r["osm_edits"] for r in rows],
            [r["user_id"] for r in rows],
            [r["user_name"] for r in rows],
            [r["changeset_id"] for r in rows],
            [r["contrib_type"] for r in rows],
            [r["tags"] for r in rows],
            [r["tags_before"] for r in rows],
            [bbox(r) for r in rows],
            [r["geom_type"] for r in rows],
            [r["geom"] for r in rows],
        ],
        schema=CONTRIBUTION_EXTRACTION_SCHEMA,
    )


def record_batch_member(rows: list[ExtractionRow]) -> RecordBatch:
    return RecordBatch.from_arrays(
        [
            [r["osm_type"] for r in rows],
            [r["osm_id"] for r in rows],
            [r["valid_from"].timestamp() * 1000000 for r in rows],
            #            [r["valid_to"].timestamp()   * 1000000 for r in rows],
            [r["osm_version"] for r in rows],
            [r["osm_minor_version"] for r in rows],
            [r["osm_edits"] for r in rows],
            [r["user_id"] for r in rows],
            [r["user_name"] for r in rows],
            [r["changeset_id"] for r in rows],
            [r["tags"] for r in rows],
            [r["part_of"] for r in rows],
            [r["part_of_role"] for r in rows],
            [r["part_of_pos"] for r in rows],
            [bbox(r) for r in rows],
            [r["geom_type"] for r in rows],
            [r["geom"] for r in rows],
            [r["clipped"] for r in rows],
        ],
        schema=MEMBER_EXTRACTION_SCHEMA,
    )


class Sink(ABC):
    def __init__(self) -> None:
        self.buffer = io.BytesIO()

    def read_bytes(self) -> bytes:
        content = self.buffer.getvalue()
        self.buffer.seek(0)
        self.buffer.truncate()
        return content

    def write_batch(self, rows: list[ExtractionRow]) -> bytes:
        if not rows:
            return b""
        return self.write_batch_(rows)

    def get_schema(self) -> schema:
        return FEATURE_EXTRACTION_SCHEMA

    def convert_batch(self, rows: list[ExtractionRow]) -> RecordBatch:
        return record_batch(rows)

    @abstractmethod
    def write_batch_(self, rows: list[ExtractionRow]) -> bytes:
        pass

    @abstractmethod
    def close(self) -> None:
        pass

    def __enter__(self) -> Sink:
        return self

    def __exit__(
        self,
        exc_type: type[BaseException] | None,
        exc_val: BaseException | None,
        exc_tb: TracebackType | None,
    ) -> None:
        self.close()


class ArrowSink(Sink):
    def __init__(self) -> None:
        super().__init__()
        self.writer = new_stream(
            self.buffer,
            self.get_schema().with_metadata({b"ohsome API": _api_meta()}),
        )

    def write_batch_(self, rows: list[ExtractionRow]) -> bytes:
        batch = self.convert_batch(rows)
        self.writer.write_batch(batch)
        return self.read_bytes()

    def close(self) -> None:
        self.writer.close()


class ParquetSink(Sink):
    # PERF: Current implementation result in copy of retrieved batches multiple times
    #   -> Memory Issues?

    def __init__(self) -> None:
        super().__init__()
        self.xmin = float("inf")
        self.ymin = float("inf")
        self.xmax = float("-inf")
        self.ymax = float("-inf")
        self.writer = parquet.ParquetWriter(
            self.buffer,
            schema=self.get_schema(),
            compression="zstd",
        )

    def write_batch_(self, rows: list[ExtractionRow]) -> bytes:
        batch = self.convert_batch(rows)
        # Which group size should we use?
        self.writer.write(batch, row_group_size=10000)

        self.xmin = min(self.xmin, *(r["xmin"] for r in rows))
        self.ymin = min(self.ymin, *(r["ymin"] for r in rows))
        self.xmax = max(self.xmax, *(r["xmax"] for r in rows))
        self.ymax = max(self.ymax, *(r["ymax"] for r in rows))

        return self.read_bytes()

    def _write_metadata(self) -> None:
        self.writer.add_key_value_metadata(
            {
                b"geo": _geoparquet_meta(self.xmin, self.ymin, self.xmax, self.ymax),
                b"ohsome API": _api_meta(),
            }
        )

    def close(self) -> None:
        self._write_metadata()
        self.writer.close()


class MemberParquetSink(ParquetSink):
    def get_schema(self) -> schema:
        return MEMBER_EXTRACTION_SCHEMA

    def convert_batch(self, rows: list[ExtractionRow]) -> RecordBatch:
        return record_batch_member(rows)


class MemberArrowSink(ArrowSink):
    def get_schema(self) -> schema:
        return MEMBER_EXTRACTION_SCHEMA

    def convert_batch(self, rows: list[ExtractionRow]) -> RecordBatch:
        return record_batch_member(rows)


class ContributionParquetSink(ParquetSink):
    def get_schema(self) -> schema:
        return CONTRIBUTION_EXTRACTION_SCHEMA

    def convert_batch(self, rows: list[ExtractionRow]) -> RecordBatch:
        return record_batch_contribution(rows)
