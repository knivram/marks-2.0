#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o xtrace

buildstack="${1}"
workspace="${2}"
script="${@:3}"

echo "Running ${script} with ${buildstack}"

echo "BUILD_NUMBER=${BUILD_NUMBER}"

docker pull "${buildstack}"
docker network ls
docker run \
    --device /dev/urandom:/dev/random \
    --device /dev/urandom:/dev/urandom \
    -v $workspace:/workspace \
    -e "TZ=Europe/Zurich" \
    -e BRANCH_NAME \
    -e BUILD_NUMBER \
    -e POSTGRES_HOST=postgres:5432 \
    -e APPLICATION_VERSION \
    --network studentmarks-build_build-net \
    --rm \
    "${buildstack}" \
    bash -ce "run-in-directory /workspace '(cd /workspace; ${script})'"