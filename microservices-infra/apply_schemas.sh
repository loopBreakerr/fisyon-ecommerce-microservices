#!/bin/bash
for f in /service_schemas/*.sql; do
  db=$(basename "$f" .sql)
  echo ">>> $db uygulaniyor"
  psql -U admin -d "$db" -f "$f"
done
