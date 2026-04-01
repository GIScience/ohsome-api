from importlib.metadata import version


def test_schema_version():
    assert version("ohsomedb_schema") == "0.1.1"
