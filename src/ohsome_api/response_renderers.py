import csv
from io import StringIO

from fastapi import Response


class CSVCountResponse(Response):
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
