#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

print_error_and_exit() {
    local errorMsg=$1
    (>&2 echo "Error: ${errorMsg}")
    exit 1
}

# Ensure build number is set
if [[ -z "${BUILD_NUMBER+x}" ]]; then
    print_error_and_exit "Environment variable BUILD_NUMBER must be set"
fi

# shellcheck disable=SC2006
commitHash=`git rev-parse --short=11 HEAD`

echo "v${BUILD_NUMBER}-${commitHash}"