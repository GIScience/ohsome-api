# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html
import json

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information
import tomllib
from datetime import datetime
from pathlib import Path

from sphinx.application import Sphinx

from ohsome_api.api import app as ohsome_api_app


def find_pyproject(path: Path | None = None) -> Path:
    """Recursively walk up to root from current working dir to find pyproject.toml"""
    if path is None:
        path = Path.cwd()
    if str(path) == path.root:
        raise FileNotFoundError("No pyproject.toml found.")
    config_path = path / "pyproject.toml"
    if config_path.exists():
        return config_path
    else:
        return find_pyproject(path.parent)


def read_metadata(path: Path) -> dict:
    with open(path, "rb") as file:
        config = tomllib.load(file)
    return config["project"]


pyproject_path = find_pyproject()
metadata = read_metadata(pyproject_path)

project = "ohsome API"
author = metadata["authors"][0]["name"]
release = metadata["version"]
copyright = "{}, {}".format(datetime.today().year, author)  # noqa: A001

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
    "sphinx_tabs.tabs",
    "sphinx_copybutton",
    "myst_nb",
    "sphinxcontrib.openapi",
]
templates_path = ["_templates"]
exclude_patterns = []

nb_execution_mode = "off"


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_static_path = ["_static"]
# html_logo = "_static/ohsome-logo.svg"
html_favicon = "_static/heigit-favicon.svg"
html_css_files = [
    "style.css",
]
html_theme = "alabaster"
html_theme_options = {
    # "logo": "ohsome-logo.svg",
    # "logo_name": True
    "description": f"Version: <code>{release}</code>",
    "extra_nav_links": {
        "Swagger UI": metadata["urls"]["Swagger"],
        "Source Code": metadata["urls"]["Repository"],
        "Contact": metadata["urls"]["Contact"],
    },
}


# -- Custom functions --------------------------------------------------------


def generate_openapi_json(app: Sphinx):
    out_dir = Path(app.srcdir) / "_static"
    with open(out_dir / "openapi.json", "w") as f:
        json.dump(ohsome_api_app.openapi(), f, indent=2)


def setup(app: Sphinx):
    app.connect("builder-inited", generate_openapi_json)
