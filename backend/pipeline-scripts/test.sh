#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset
set -o xtrace

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
root=${dir}/../..

export GRADLE_USER_HOME=${root}

(cd ${root}; ./gradlew -PPOSTGRES_HOST=postgres:5432 --exclude-task build --exclude-task check test)