import csv
from _csv import Writer
from abc import ABC, abstractmethod
from importlib.metadata import version
from io import StringIO

from fastapi import Response

from ohsome_api.models import Attribution

VERSION = version("ohsome-api")
ATTRIBUTION = Attribution()


class CSVResponse(Response, ABC):
    media_type = "text/csv"
    description = (
        "CSV Response Format:\n"
        "- Delimiter: `;`\n"
        "- Comments: `#`\n"
        "- Line terminator: `\\n`\n"
        "- Quote character: `\\`"
    )

    def render(self, content: dict) -> bytes:
        csvfile = StringIO()
        writer = csv.writer(csvfile, delimiter=";", lineterminator="\n")
        comment = [
            [f"# apiVersion: {VERSION}"],
            [f"# attribution.url: {ATTRIBUTION.url}"],
            [f"# attribution.text: {ATTRIBUTION.text}"],
        ]
        writer.writerows(comment)
        self._render(writer, content)
        return csvfile.getvalue().encode()

    @abstractmethod
    def _render(self, writer: Writer, content: dict) -> None:
        pass


class CSVTimeBinsResponse(CSVResponse):
    example = f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
2007-10-08T00:00:00Z;2026-01-01T00:00:00Z;163
"""

    def _render(self, writer: Writer, content: dict) -> None:
        header = ["start", "end", "value"]
        rows = [
            (
                r["start"],
                r["end"],
                r["value"],
            )
            for r in content["result"]
        ]

        writer.writerow(header)
        writer.writerows(rows)


class CSVSnapshotsResponse(CSVResponse):
    example = f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
timestamp;result
2026-01-01T00:00:00Z;163
"""

    def _render(self, writer: Writer, content: dict) -> None:
        results = content["result"]
        is_grouped_by_tag = len(results) > 0 and "tagvalue" in results[0]
        if is_grouped_by_tag:
            header = ["timestamp", "value", "tagvalue"]
            rows = [
                (
                    r["timestamp"],
                    r["value"],
                    r["tagvalue"],
                )
                for r in results
            ]
        else:
            header = ["timestamp", "value"]
            rows = [
                (
                    r["timestamp"],
                    r["value"],
                )
                for r in results
            ]
        writer.writerow(header)
        writer.writerows(rows)
