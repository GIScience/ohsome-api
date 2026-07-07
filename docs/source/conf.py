# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information
import tomllib
from pathlib import Path


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

project = metadata["name"]
author = metadata["authors"][0]["name"]
release = metadata["version"]

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = ["sphinx_tabs.tabs"]
templates_path = ["_templates"]
exclude_patterns = []


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = "alabaster"
html_static_path = ["_static"]
# html_logo = "_static/ohsome-logo.svg"
html_favicon = "_static/heigit-favicon.svg"
html_css_files = [
    "style.css",
]
