#!/bin/bash

set -o errexit
set -o errtrace
set -o pipefail
set -o xtrace

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
frontend="${dir}/.."

(cd "${frontend}"; npm run lint; npm run check)