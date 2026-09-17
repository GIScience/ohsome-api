from ohsome_api.response_renderers import CSVSnapshotsResponse


def test_csv_snapshots_response_neutralize_formula_prefixes():
    content = {
        "result": [
            {
                "timestamp": "2020-01-01T00:00:00Z",
                "value": 5,
                "group": "=1+1",
            },
            {
                "timestamp": "2020-01-01T00:00:00Z",
                "value": 5,
                "group": """
                    Hello
                    newline""",
            },
            {
                "timestamp": "2020-01-01T00:00:00Z",
                "value": 5,
                "group": 'Hello two double quotes ""',
            },
        ]
    }
    expected = """"# apiVersion: 2.0.0rc2"
"# attribution.url: https://ohsome.org/copyrights"
"# attribution.text: © OpenStreetMap contributors"
"timestamp";"value";"group"
"2020-01-01T00:00:00Z";"5";"=1+1"
"2020-01-01T00:00:00Z";"5";"
                    Hello
                    newline"
"2020-01-01T00:00:00Z";"5";"Hello two double quotes \"\"\"\""
"""
    assert CSVSnapshotsResponse(content).body.decode() == expected  # type: ignore
