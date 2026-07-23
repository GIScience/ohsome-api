#!/bin/sh

set -e

if [ $# -eq 0 ]; then
    echo "Please supply one of the following values as argument:"
    echo "major, minor, patch, stable, alpha, beta, rc, post, dev"
    exit
fi

OLD_VERSION=$(uv version --short)

uv version --bump "$1"

NEW_VERSION=$(uv version --short)

sed --in-place "s/$OLD_VERSION/$NEW_VERSION/g" tests/conftest.py

git add -p pyproject.toml tests/conftest.py
git add uv.lock

git commit -m "$NEW_VERSION"
git tag "$NEW_VERSION" -m "$NEW_VERSION"
git push origin main
git push origin "$NEW_VERSION"
