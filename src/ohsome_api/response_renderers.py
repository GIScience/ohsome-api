import csv
from importlib.metadata import version
from io import StringIO

from fastapi import Response

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


ACTIVITY_AS_CSV_EXAMPLE = {
    "content": {
        "text/csv": {
            "schema": {"type": "string"},
            "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
1970-01-01T00:00:00Z;1970-02-01T00:00:00Z;0
""",
        },
    },
}

CURRENTNESS_AS_CSV_EXAMPLE = {
    "content": {
        "text/csv": {
            "schema": {"type": "string"},
            "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
start;end;value
1970-01-01T00:00:00Z;1970-02-01T00:00:00Z;0
""",
        },
    },
}


class CSVSnapshotsResponse(Response):
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


FEATURES_AS_CSV_EXAMPLE = {
    "content": {
        "text/csv": {
            "schema": {"type": "string"},
            "example": f"""# apiVersion: {VERSION}
# attribution.url: https://ohsome.org/copyrights
# attribution.text: © OpenStreetMap contributors
timestamp;result
1970-01-01T00:00:00Z;0
""",
        },
    },
}
