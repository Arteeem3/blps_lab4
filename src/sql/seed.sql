SET search_path TO s409599, public;

BEGIN;

INSERT INTO s409599.cian_users (id, email, password_hash, role, created_at) VALUES
  (1, 'admin@cian.local',
   '$2a$10$e/.nWCqPH.DmEvM2YHCn2OjvkrlW8IzfMriJCnSXE.HChMFVMYty.',
   'ADMIN', now() - interval '60 days'),
  (2, 'seller1@test.local',
   '$2a$10$CpXz6fwvojiCQEvJGUgYMOrt3yljTpoz3gtjH7n0IIDbFacf7Njs2',
   'SELLER', now() - interval '45 days'),
  (3, 'seller2@test.local',
   '$2a$10$CpXz6fwvojiCQEvJGUgYMOrt3yljTpoz3gtjH7n0IIDbFacf7Njs2',
   'SELLER', now() - interval '30 days'),
  (4, 'buyer1@test.local',
   '$2a$10$8zP0iM5LTgwpzJB7.aeF0uXG90fPswYlSTlJWkFa1dnqEPiu4f4xC',
   'BUYER', now() - interval '20 days'),
  (5, 'buyer2@test.local',
   '$2a$10$8zP0iM5LTgwpzJB7.aeF0uXG90fPswYlSTlJWkFa1dnqEPiu4f4xC',
   'BUYER', now() - interval '15 days'),
  (6, 'seller+buyer@test.local',
   '$2a$10$5mQ4ZJuA.qy0FKGhsfTcnOlRDchOQ/5wOYMIQBZDjB53OCrRxhRTO',
   'SELLER', now() - interval '10 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO s409599.cian_listings (
  id, seller_id, title, description, address, region, price, area_sqm, rooms,
  status, promotion, published_at, expires_at, closed_at, created_at
) VALUES
  (1, 2, 'Черновик: студия на окраине',
   'Черновик, фото добавлю позже', 'ул. Новая, 1', 'Москва',
   5200000.00, 28.5, 0, 'DRAFT', 'NONE',
   NULL, NULL, NULL, now() - interval '2 days'),
  (2, 2, 'Топ: 3-к квартира у метро',
   'Ремонт, кухня-гостиная', 'Невский пр., 10', 'Санкт-Петербург',
   18500000.00, 78.0, 3, 'ACTIVE', 'TOP',
   now() - interval '12 days', now() + interval '18 days', NULL, now() - interval '14 days'),
  (3, 2, 'Премиум: новостройка в центре',
   'Сдача в этом году', 'ул. Центральная, 5', 'Москва',
   24500000.00, 95.5, 4, 'ACTIVE', 'PREMIUM',
   now() - interval '25 days', now() + interval '5 days', NULL, now() - interval '26 days'),
  (4, 3, 'Обычное объявление: 2-к в Казани',
   'Тихий двор, парковка', 'ул. Баумана, 3', 'Казань',
   9200000.00, 55.0, 2, 'ACTIVE', 'NONE',
   now() - interval '8 days', now() + interval '22 days', NULL, now() - interval '9 days'),
  (5, 3, 'Архив: дача',
   'Участок 6 соток', 'СНТ Ромашка', 'Ленинградская область',
   3100000.00, 45.0, 2, 'ARCHIVED', 'NONE',
   now() - interval '120 days', now() - interval '90 days', NULL, now() - interval '121 days'),
  (6, 2, 'Закрыто: продано',
   'Сделка завершена', 'ул. Мира, 20', 'Москва',
   11200000.00, 60.0, 2, 'CLOSED', 'NONE',
   now() - interval '200 days', now() - interval '170 days',
   now() - interval '30 days', now() - interval '201 days'),
  (7, 6, 'Скоро истекает срок (для напоминаний)',
   'Тест ARCHIVATION_SOON по сроку', 'пр-т Мира, 100', 'Москва',
   6700000.00, 42.0, 2, 'ACTIVE', 'NONE',
   now() - interval '29 days', now() + interval '1 day', NULL, now() - interval '29 days')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('s409599.cian_users', 'id'),
              (SELECT COALESCE(MAX(id), 1) FROM s409599.cian_users));
SELECT setval(pg_get_serial_sequence('s409599.cian_listings', 'id'),
              (SELECT COALESCE(MAX(id), 1) FROM s409599.cian_listings));

COMMIT;
