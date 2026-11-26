-- V2 : met à niveau une base créée par ddl-auto=update avant le parcours d'achat et le scan des billets.
-- Idempotent : sans effet sur une base créée par V1.

-- 1. Parcours d'achat : lignes de réservation, échéance de paiement
CREATE TABLE IF NOT EXISTS booking_items (
    is_active boolean,
    quantity integer not null,
    total_price numeric(10,2) not null,
    unit_price numeric(10,2) not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    booking_id uuid not null,
    id uuid not null,
    price_category_id uuid not null,
    created_by varchar(255),
    price_category_name varchar(255) not null,
    updated_by varchar(255),
    primary key (id)
);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS event_name varchar(255);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS expires_at timestamp(6);
CREATE INDEX IF NOT EXISTS idx_booking_item_booking ON booking_items (booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_status_expires ON bookings (status, expires_at);
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_booking_items_booking') AND NOT EXISTS (
        SELECT 1 FROM pg_constraint con JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'booking_items' AND con.contype = 'f') THEN
        ALTER TABLE booking_items
            ADD CONSTRAINT fk_booking_items_booking FOREIGN KEY (booking_id) REFERENCES bookings;
    END IF;
END $$;

-- 2. Billets : réservation d'origine, catégorie, passage à l'entrée
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS booking_id uuid;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS category varchar(255);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS checked_in_at timestamp(6);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS checked_in_by uuid;

-- 3. price_categories.name : unique par événement, plus dans toute la base
DO $$
DECLARE c record;
BEGIN
    FOR c IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'price_categories'
          AND con.contype = 'u'
          AND array_length(con.conkey, 1) = 1
          AND con.conkey[1] = (SELECT attnum FROM pg_attribute WHERE attrelid = rel.oid AND attname = 'name')
    LOOP
        EXECUTE format('ALTER TABLE price_categories DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;
ALTER TABLE price_categories DROP CONSTRAINT IF EXISTS uk_price_category_event_name;
ALTER TABLE price_categories ADD CONSTRAINT uk_price_category_event_name UNIQUE (event_id, name);

-- 4. Statut EXPIRED des réservations (contrainte CHECK créée par Hibernate sur les anciennes valeurs)
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED', 'EXPIRED'));
