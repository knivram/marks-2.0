#!/bin/bash

set -e

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
buildstack=docker.ergon.ch/ergon/docker-buildstack-corretto-17:619-cf1ee06-1
workspace="${dir}/../.."
echo "Workspace = ${workspace}"
script="${@}"

"${dir}/buildstack.sh" "${buildstack}" "${workspace}" "./backend/wait-for-db.sh" "${script}"