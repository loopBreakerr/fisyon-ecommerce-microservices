#!/bin/bash
# 10 mikroservis database'inin (keycloak_db haric) schema-only pg_dump ciktisini
# tek bir SQL akisi olarak stdout'a yazar. dbdiagram.io "Import from SQL" (PostgreSQL)
# ile dogrudan uyumlu olmasi icin --no-owner --no-privileges --no-comments kullanilir.
set -e

DATABASES="catalog_db cart_db order_db payment_db inventory_db notification_db review_db shipping_db recommendation_db user_profile_db"

for db in $DATABASES; do
  echo "-- ============================================================"
  echo "-- DATABASE: $db"
  echo "-- ============================================================"
  # \restrict / \unrestrict satirlari pg_dump'in yeni psql-only guvenlik komutlaridir,
  # gecerli SQL degildir; dbdiagram.io gibi SQL parser'lari bunlari anlamaz, filtreleniyor.
  pg_dump -U admin -d "$db" --schema-only --no-owner --no-privileges --no-tablespaces --no-comments \
    | grep -vE '^\\(restrict|unrestrict)\b'
  echo ""
done
