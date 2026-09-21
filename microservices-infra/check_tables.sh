#!/bin/bash
for db in cart_db order_db payment_db inventory_db notification_db review_db shipping_db recommendation_db; do
  echo ">>> $db:"
  psql -U admin -d "$db" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';"
done
