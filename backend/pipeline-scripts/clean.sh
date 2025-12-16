#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset
set -o xtrace

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
root=${dir}/../..

(cd ${root}; ./gradlew --stacktrace clean)