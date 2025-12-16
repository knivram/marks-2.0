#!/bin/bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset
set -o xtrace

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

(cd "$dir"; docker build -t docker.ergon.ch/marksystem/frontend-v2:"$APPLICATION_VERSION" -t docker.ergon.ch/marksystem/frontend-v2:latest .)
docker push docker.ergon.ch/marksystem/frontend-v2:"$APPLICATION_VERSION"
docker push docker.ergon.ch/marksystem/frontend-v2:latest
