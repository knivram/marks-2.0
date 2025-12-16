#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <BACKUP_FILE>"
    echo "You need to provide a backup file."
    exit 1
fi

BACKUP_FILE=$1

export PGPASSWORD=$DB_PASSWORD

pg_restore -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME $BACKUP_FILE
