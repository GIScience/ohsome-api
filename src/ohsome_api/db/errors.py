from ohsome_api import CONFIG


class OhsomeAPIError(ValueError):
    pass


class TimeSeriesTooLargeError(OhsomeAPIError):
    pass


class ResultTooLargeError(OhsomeAPIError):
    pass


class StartGreaterThanEndError(OhsomeAPIError):
    pass


class OhsomeApiTimeoutError(TimeoutError):
    pass


class PoolAcquireTimeoutError(OhsomeApiTimeoutError):
    pass


class QueryTimeoutError(OhsomeApiTimeoutError):
    def __init__(self) -> None:
        message = (
            f"Query timeout limit has been exceeded. "
            f"For statistics requests the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_stats}s. "
            f"For extraction requests the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_extraction}s. "
            f"Try reducing request complexity "
            f"(e.g. simplify the geometry, make it smaller "
            f"or reduce the number of points/bins of the time series/bins "
            f"and try again."
        )
        super().__init__(message)
