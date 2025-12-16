#!/bin/bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset
set -o xtrace

BACKUP_DIR=$1
DB_HOST=$2
DB_PASSWORD=$3
DB_PORT=$4
DB_USER=$5
DB_NAME=$6
RETENTION=$7

error_handler() {
    rm $BACKUP_FILE

    curl -i -X POST -H 'Content-Type: application/json' -d '{
        "username": "backuper",
        "channel": "marksystem-development",
        "attachments": [
            {
                "fallback": "Database backup failed!",
                "color": "#FF0000",
                "text": "Database backup failed! Please check the system status."
            }
        ]
    }' https://mattermost.ergon.ch/hooks/5nwncqnmybn1mynhqfsewinjbc
}

# Calls the error_handler if an error occurs
trap error_handler ERR

DATE=$(date +\%Y\%m\%d-\%H\%M\%S)

mkdir -p "$BACKUP_DIR"

BACKUP_FILE="$BACKUP_DIR/backup-$DATE.sql.gz"

PGPASSWORD=$DB_PASSWORD pg_dump -h $DB_HOST -p $DB_PORT -U $DB_USER -F c -b -v -f $BACKUP_FILE $DB_NAME

# Delete all backups older than 60 days
find $BACKUP_DIR -type f -name '*.gz' $RETENTION -delete