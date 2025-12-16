#!/bin/bash

set -e

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
buildstack=node:22.4.0
workspace="${dir}/../.."
echo "Workspace = ${workspace}"
script="${@}"

"${dir}/buildstack.sh" "${buildstack}" "${workspace}" "${script}"