#!/bin/bash

# Creates a tagged version. Releases must be manually created.

if [ $# -ne 1 ]; then
  echo "Usage: $0 \"<release message>\""
  exit 1
fi

version=$(grep -E 'mod_version(\s*)=(\s*)' "./gradle.properties" | cut -d'=' -f2 | tr -d ' ')

echo "Creating tag for version $version with message '$1'"

git tag -s -a v"$version" -m "$1"

git push origin 1.20.1/dev v"$version"