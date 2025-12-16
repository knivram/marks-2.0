#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o xtrace

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
backend="${dir}/.."
echo "Workspace = ${backend}"

(cd ${backend}; docker compose -p studentmarks-build -f docker-compose-db-only.yml down)