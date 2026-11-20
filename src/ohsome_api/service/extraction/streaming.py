"""Helpers to stream extraction batches through a sink.

Batches can be large (see `batch_mem_bytes`). A suspended generator keeps its
local variables alive, so every generator here drops its reference to a batch
(`del`) as soon as it is passed on. `yield` does not copy: producer and consumer
share the same object, and it is freed once the last reference is gone. This
keeps at most one batch per request in memory.
"""

from collections.abc import AsyncIterator, Awaitable, Callable

from ohsome_api.models import ExtractionRow
from ohsome_api.parquet import RowFormat, Sink

Batch = list[ExtractionRow]


async def prepend(first: Batch, rest: AsyncIterator[Batch]) -> AsyncIterator[Batch]:
    """Yield `first`, then all batches of `rest`."""
    yield first
    del first
    async for batch in rest:
        yield batch
        del batch


async def map_batches(
    batches: AsyncIterator[Batch],
    func: Callable[[Batch], Awaitable[Batch]],
) -> AsyncIterator[Batch]:
    """Apply an async function to each batch."""
    async for batch in batches:
        result = await func(batch)
        del batch
        yield result
        del result


async def encode(
    sink_type: type[Sink],
    row_format: RowFormat,
    batches: AsyncIterator[Batch],
) -> AsyncIterator[bytes]:
    """Encode batches with a new sink and yield the bytes, footer last."""
    with sink_type(row_format) as sink:
        async for rows in batches:
            chunk = sink.write_batch(rows)
            del rows
            yield chunk
            del chunk
        yield sink.finish()
