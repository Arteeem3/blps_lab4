SET search_path TO s409599, public;

DROP TABLE IF EXISTS s409599.cian_notifications CASCADE;
DROP TABLE IF EXISTS s409599.cian_inquiries CASCADE;
DROP TABLE IF EXISTS s409599.cian_payments CASCADE;
DROP TABLE IF EXISTS s409599.cian_listings CASCADE;
DROP TABLE IF EXISTS s409599.cian_users CASCADE;

DROP TYPE IF EXISTS s409599.cian_related_entity_type CASCADE;
DROP TYPE IF EXISTS s409599.cian_notification_type CASCADE;
DROP TYPE IF EXISTS s409599.cian_inquiry_status CASCADE;
DROP TYPE IF EXISTS s409599.cian_payment_status CASCADE;
DROP TYPE IF EXISTS s409599.cian_promotion_type CASCADE;
DROP TYPE IF EXISTS s409599.cian_listing_status CASCADE;
DROP TYPE IF EXISTS s409599.cian_user_role CASCADE;

CREATE TABLE s409599.cian_users (
  id            BIGSERIAL PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(50)  NOT NULL,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE s409599.cian_listings (
  id           BIGSERIAL PRIMARY KEY,
  seller_id    BIGINT NOT NULL REFERENCES s409599.cian_users(id) ON DELETE CASCADE,
  title        VARCHAR(500) NOT NULL,
  description  TEXT,
  address      VARCHAR(500),
  region       VARCHAR(255),
  price        NUMERIC(18,2) NOT NULL,
  area_sqm     NUMERIC(10,2),
  rooms        INTEGER,
  status       VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
  promotion    VARCHAR(50) NOT NULL DEFAULT 'NONE',
  published_at TIMESTAMPTZ,
  expires_at   TIMESTAMPTZ,
  closed_at    TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE s409599.cian_payments (
  id             BIGSERIAL PRIMARY KEY,
  listing_id     BIGINT NOT NULL REFERENCES s409599.cian_listings(id) ON DELETE CASCADE,
  user_id        BIGINT NOT NULL REFERENCES s409599.cian_users(id) ON DELETE CASCADE,
  promotion_type VARCHAR(50) NOT NULL,
  status         VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  external_id    VARCHAR(255),
  amount_cents   INTEGER,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT cian_chk_promotion_not_none CHECK (promotion_type IN ('TOP', 'PREMIUM'))
);

CREATE TABLE s409599.cian_inquiries (
  id            BIGSERIAL PRIMARY KEY,
  listing_id    BIGINT NOT NULL REFERENCES s409599.cian_listings(id) ON DELETE CASCADE,
  buyer_id      BIGINT NOT NULL REFERENCES s409599.cian_users(id) ON DELETE CASCADE,
  message       TEXT,
  status        VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  scheduled_at  TIMESTAMPTZ,
  contact_info  VARCHAR(500),
  reject_reason TEXT,
  will_buy      BOOLEAN,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE s409599.cian_notifications (
  id                  BIGSERIAL PRIMARY KEY,
  user_id             BIGINT NOT NULL REFERENCES s409599.cian_users(id) ON DELETE CASCADE,
  type                VARCHAR(50) NOT NULL,
  title               VARCHAR(500) NOT NULL,
  body                TEXT,
  related_entity_type VARCHAR(50),
  related_entity_id   BIGINT,
  read                BOOLEAN NOT NULL DEFAULT false,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_cian_listings_seller_id   ON s409599.cian_listings(seller_id);
CREATE INDEX idx_cian_listings_status      ON s409599.cian_listings(status);
CREATE INDEX idx_cian_listings_expires_at  ON s409599.cian_listings(expires_at) WHERE status = 'ACTIVE';
CREATE INDEX idx_cian_listings_search      ON s409599.cian_listings(region, price, area_sqm, rooms) WHERE status = 'ACTIVE';

CREATE INDEX idx_cian_payments_listing_id  ON s409599.cian_payments(listing_id);
CREATE INDEX idx_cian_payments_user_id     ON s409599.cian_payments(user_id);
CREATE INDEX idx_cian_payments_status      ON s409599.cian_payments(status);

CREATE INDEX idx_cian_inquiries_listing_id ON s409599.cian_inquiries(listing_id);
CREATE INDEX idx_cian_inquiries_buyer_id   ON s409599.cian_inquiries(buyer_id);
CREATE INDEX idx_cian_inquiries_status     ON s409599.cian_inquiries(status);

CREATE INDEX idx_cian_notifications_user_id      ON s409599.cian_notifications(user_id);
CREATE INDEX idx_cian_notifications_user_read    ON s409599.cian_notifications(user_id, read);
CREATE INDEX idx_cian_notifications_created_at   ON s409599.cian_notifications(created_at DESC);
