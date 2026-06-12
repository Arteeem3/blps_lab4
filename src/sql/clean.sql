SET search_path TO s409599, public;

TRUNCATE TABLE s409599.cian_notifications,
               s409599.cian_inquiries,
               s409599.cian_payments,
               s409599.cian_listings,
               s409599.cian_users
RESTART IDENTITY CASCADE;
