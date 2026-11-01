#!/bin/sh
set -eu
cd "$(dirname "$0")"
mvn -q package
exec java -jar target/biomedical-streams-1.0.jar "$@"

