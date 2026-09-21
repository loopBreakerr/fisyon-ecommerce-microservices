#!/bin/bash
for f in /schema_fixes/*.sql; do
  db=$(basename "$f" .sql)
  echo ">>> $db guncelleniyor"
  psql -U admin -d "$db" -f "$f"
done
