class OhsomeAPIError(ValueError):
    pass


class TimeSeriesTooLargeError(OhsomeAPIError):
    pass


class ResultTooLargeError(OhsomeAPIError):
    pass
