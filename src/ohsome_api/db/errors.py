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
            f"For statistics endpoints the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_stats}. "
            f"For extraction endpoints the timeout limit is "
            f"{CONFIG.ohsomedb.timeout_extraction}."
        )
        super().__init__(message)
