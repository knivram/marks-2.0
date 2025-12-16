#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset

export IMAGE_TAG=$1
ENV="$2"

if [ "$ENV" = "Development" ]; then
    SWARM_SERVICE_NAME="marks-dev"
    COMPOSE_FILE=./docker-compose-dev.yml
elif [ "$ENV" = "Production" ]; then
    SWARM_SERVICE_NAME="marks-prod"
    COMPOSE_FILE=./docker-compose-prod.yml
else
    echo "Unknown environment: $ENV"
    echo "
Usage:
deploy-build.sh <image> <env>
    image: eq: latest
    env: Development|Production"
    exit 1
fi

docker -H docker.swarm.ergon.ch stack rm "$SWARM_SERVICE_NAME"
docker -H docker.swarm.ergon.ch stack deploy --compose-file "$COMPOSE_FILE" "$SWARM_SERVICE_NAME"