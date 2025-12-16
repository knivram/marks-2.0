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
docker run \
    --device /dev/urandom:/dev/random \
    --device /dev/urandom:/dev/urandom \
    -v $workspace:/workspace \
    -e "TZ=Europe/Zurich" \
    -e BRANCH_NAME \
    -e BUILD_NUMBER \
    -e APPLICATION_VERSION \
    --rm \
    "${buildstack}" \
    bash -ce "(cd /workspace; ${script})"