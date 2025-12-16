#!/bin/sh

set -o errexit
set -o nounset
set -o xtrace

host="$(echo "$POSTGRES_HOST" | cut -d':' -f1)"
port="$(echo "$POSTGRES_HOST" | cut -d':' -f2)"

cmd="$@"
max_retries=30
retry_count=0

until nc -z "$host" "$port" >/dev/null 2>&1; do
  retry_count=$((retry_count+1))

  if [ $retry_count -ge $max_retries ]; then
    >&2 echo "Error: $host:$port was not reachable after $max_retries attempts."
    exit 1
  fi

  >&2 echo "Waiting for $host:$port to be available... (Attempt: $retry_count)"
  sleep 1
done

>&2 echo "$host:$port is available - executing command"
exec $cmd
