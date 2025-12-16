#!/bin/bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset
set -o xtrace

id

NEW_CRON_JOBS="$CRON_SCHEDULE_EXPRESSION /backup.sh $BACKUP_DIR $DB_HOST $DB_PASSWORD $DB_PORT $DB_USER $DB_NAME '$RETENTION'"
echo "$NEW_CRON_JOBS" | crontab -

sudo cron -f