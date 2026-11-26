-- Migration manuelle à lancer UNE FOIS sur une base PostgreSQL existante.
-- ddl-auto=update crée les nouvelles tables et colonnes, mais ne modifie pas les contraintes existantes.
-- (À remplacer par Flyway/Liquibase, voir docs/checklist/09-production-readiness.md)

BEGIN;

-- 1. price_categories.name n'est plus unique dans toute la base, seulement par événement.
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
          AND con.conkey[1] = (SELECT attnum FROM pg_attribute
                               WHERE attrelid = rel.oid AND attname = 'name')
    LOOP
        EXECUTE format('ALTER TABLE price_categories DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

ALTER TABLE price_categories DROP CONSTRAINT IF EXISTS uk_price_category_event_name;
ALTER TABLE price_categories
    ADD CONSTRAINT uk_price_category_event_name UNIQUE (event_id, name);

-- 2. Nouveau statut EXPIRED pour les réservations : Hibernate a pu créer une contrainte CHECK
--    sur les anciennes valeurs, qui refuserait EXPIRED.
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;
ALTER TABLE bookings
    ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED', 'EXPIRED'));

COMMIT;
