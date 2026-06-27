import csv
from importlib.metadata import version
from io import StringIO

from fastapi import Response
from fastapi.responses import PlainTextResponse

VERSION = version("ohsome-api")


class CSVTimeBinsResponse(Response):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        csvfile = StringIO()
        writer = csv.writer(csvfile, delimiter=";", lineterminator="\n")
        comment = [
            [f"# apiVersion: {content['apiVersion']}"],
            [f"# attribution.url: {content['attribution']['url']}"],
            [f"# attribution.text: {content['attribution']['text']}"],
        ]
        header = ["start", "end", "value"]
        rows = [
            (
                r["start"],
                r["end"],
                r["value"],
            )
            for r in content["result"]
        ]
        writer.writerows(comment)
        writer.writerow(header)
        writer.writerows(rows)
        return csvfile.getvalue().encode()


CSV_TIME_BINS_RESPONSE_EXAMPLE = f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
2007-10-08T00:00:00Z;2026-01-01T00:00:00Z;163
"""


class CSVSnapshotsResponse(PlainTextResponse):
    media_type = "text/csv"

    def render(self, content: dict) -> bytes:
        csvfile = StringIO()
        writer = csv.writer(csvfile, delimiter=";", lineterminator="\n")
        comment = [
            [f"# apiVersion: {content['apiVersion']}"],
            [f"# attribution.url: {content['attribution']['url']}"],
            [f"# attribution.text: {content['attribution']['text']}"],
        ]
        header = ["timestamp", "value"]
        rows = [
            (
                r["timestamp"],
                r["value"],
            )
            for r in content["result"]
        ]
        writer.writerows(comment)
        writer.writerow(header)
        writer.writerows(rows)
        return csvfile.getvalue().encode()


CSV_SNAPSHOT_EXAMPLE = f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
timestamp;result
2026-01-01T00:00:00Z;163
"""

CSV_RESPONSE_DESCRIPTION = (
    "CSV Response Format:\n"
    "- Delimiter: `;`\n"
    "- Comments: `#`\n"
    "- Line terminator: `\\n`\n"
    "- Quote character: `\\`"
)
