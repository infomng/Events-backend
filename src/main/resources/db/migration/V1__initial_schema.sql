-- V1 : schéma initial, généré depuis les entités JPA (dialecte PostgreSQL) le 2026-11-26.
-- Sur une base existante créée par ddl-auto=update, Flyway considère ce script comme déjà appliqué
-- (spring.flyway.baseline-on-migrate=true, baseline-version=1) : il ne s'exécute que sur une base vide.

-- Recherche par proximité (IEventRepository.findByLocationNear : earth_distance / ll_to_earth)
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;


    create table booking_items (
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

    create table booking_seats (
        is_active boolean,
        price numeric(10,2) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        booking_id uuid not null,
        id uuid not null,
        seat_id uuid not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id),
        constraint uk_booking_seat unique (booking_id, seat_id)
    );

    create table bookings (
        is_active boolean,
        total_amount numeric(10,2) not null,
        created_at timestamp(6) not null,
        expires_at timestamp(6),
        updated_at timestamp(6) not null,
        event_id uuid not null,
        id uuid not null,
        user_id uuid not null,
        booking_reference varchar(255) not null unique,
        created_by varchar(255),
        event_name varchar(255),
        payment_id varchar(255),
        payment_status varchar(255) check (payment_status in ('PENDING','SUCCESS','FAILED')),
        status varchar(255) check (status in ('PENDING','CONFIRMED','CANCELLED','FAILED','EXPIRED')),
        updated_by varchar(255),
        primary key (id)
    );

    create table cart_items (
        is_active boolean,
        old_unit_price numeric(38,2) not null,
        quantity integer not null,
        total_price numeric(38,2) not null,
        unit_price numeric(38,2) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        cart_id uuid not null,
        event_id uuid not null,
        id uuid not null,
        price_category_id uuid not null,
        created_by varchar(255),
        event_name varchar(255) not null,
        price_category_name varchar(255) not null,
        status varchar(255) not null check (status in ('ACTIVE','ORDERED','ABANDONED')),
        updated_by varchar(255),
        primary key (id)
    );

    create table carts (
        is_active boolean,
        total_items integer not null,
        total_price numeric(38,2) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        id uuid not null,
        user_id uuid not null unique,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id)
    );

    create table categories (
        is_active boolean,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        id uuid not null,
        created_by varchar(255),
        description TEXT,
        name varchar(255) not null unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table countries (
        code varchar(2) not null unique,
        is_active boolean,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        id uuid not null,
        created_by varchar(255),
        name varchar(255) not null unique,
        pays_enum varchar(255) not null check (pays_enum in ('AFGHANISTAN','ALBANIA','ALGERIA','ANDORRA','ANGOLA','ARGENTINA','ARMENIA','AUSTRALIA','AUSTRIA','AZERBAIJAN','BAHAMAS','BAHRAIN','BANGLADESH','BARBADOS','BELARUS','BELGIUM','BELIZE','BENIN','BHUTAN','BOLIVIA','BOSNIA_AND_HERZEGOVINA','BOTSWANA','BRAZIL','BRUNEI','BULGARIA','BURKINA_FASO','BURUNDI','CAMBODIA','CAMEROON','CANADA','CAPE_VERDE','CENTRAL_AFRICAN_REPUBLIC','CHAD','CHILE','CHINA','COLOMBIA','COMOROS','CONGO','COSTA_RICA','CROATIA','CUBA','CYPRUS','CZECH_REPUBLIC','DENMARK','DJIBOUTI','DOMINICA','DOMINICAN_REPUBLIC','ECUADOR','EGYPT','EL_SALVADOR','EQUATORIAL_GUINEA','ERITREA','ESTONIA','ESWATINI','ETHIOPIA','FIJI','FINLAND','FRANCE','GABON','GAMBIA','GEORGIA','GERMANY','GHANA','GREECE','GRENADA','GUATEMALA','GUINEA','GUINEA_BISSAU','GUYANA','HAITI','HONDURAS','HUNGARY','ICELAND','INDIA','INDONESIA','IRAN','IRAQ','IRELAND','ISRAEL','ITALY','JAMAICA','JAPAN','JORDAN','KAZAKHSTAN','KENYA','KIRIBATI','KOSOVO','KUWAIT','KYRGYZSTAN','LAOS','LATVIA','LEBANON','LESOTHO','LIBERIA','LIBYA','LIECHTENSTEIN','LITHUANIA','LUXEMBOURG','MADAGASCAR','MALAWI','MALAYSIA','MALDIVES','MALI','MALTA','MARSHALL_ISLANDS','MAURITANIA','MAURITIUS','MEXICO','MICRONESIA','MOLDOVA','MONACO','MONGOLIA','MONTENEGRO','MOROCCO','MOZAMBIQUE','MYANMAR','NAMIBIA','NAURU','NEPAL','NETHERLANDS','NEW_ZEALAND','NICARAGUA','NIGER','NIGERIA','NORTH_KOREA','NORTH_MACEDONIA','NORWAY','OMAN','PAKISTAN','PALAU','PALESTINE','PANAMA','PAPUA_NEW_GUINEA','PARAGUAY','PERU','PHILIPPINES','POLAND','PORTUGAL','QATAR','ROMANIA','RUSSIA','RWANDA','SAINT_KITTS_AND_NEVIS','SAINT_LUCIA','SAINT_VINCENT_AND_THE_GRENADINES','SAMOA','SAN_MARINO','SAO_TOME_AND_PRINCIPE','SAUDI_ARABIA','SENEGAL','SERBIA','SEYCHELLES','SIERRA_LEONE','SINGAPORE','SLOVAKIA','SLOVENIA','SOLOMON_ISLANDS','SOMALIA','SOUTH_AFRICA','SOUTH_KOREA','SOUTH_SUDAN','SPAIN','SRI_LANKA','SUDAN','SURINAME','SWEDEN','SWITZERLAND','SYRIA','TAIWAN','TAJIKISTAN','TANZANIA','THAILAND','TIMOR_LESTE','TOGO','TONGA','TRINIDAD_AND_TOBAGO','TUNISIA','TURKEY','TURKMENISTAN','TUVALU','UGANDA','UKRAINE','UNITED_ARAB_EMIRATES','UNITED_KINGDOM','UNITED_STATES','URUGUAY','UZBEKISTAN','VANUATU','VATICAN_CITY','VENEZUELA','VIETNAM','YEMEN','ZAMBIA','ZIMBABWE')),
        updated_by varchar(255),
        primary key (id)
    );

    create table event_attendees (
        event_id uuid not null,
        user_id uuid not null,
        primary key (event_id, user_id)
    );

    create table event_staff (
        event_id uuid not null,
        user_id uuid not null,
        primary key (event_id, user_id)
    );

    create table events (
        available_tickets integer,
        has_invitation_code boolean not null,
        has_seats boolean not null,
        is_active boolean,
        is_featured boolean not null,
        is_public boolean not null,
        is_ticket_sales_active boolean not null,
        latitude float(53),
        longitude float(53),
        total_tickets integer,
        created_at timestamp(6) not null,
        end_date timestamp(6) not null,
        start_date timestamp(6) not null,
        ticket_sales_end_date timestamp(6),
        ticket_sales_start_date timestamp(6) not null,
        updated_at timestamp(6) not null,
        category_id uuid,
        country_id uuid,
        id uuid not null,
        organizer_id uuid,
        created_by varchar(255),
        description varchar(255) not null,
        location varchar(255) not null,
        name varchar(255) not null,
        status varchar(255) check (status in ('DRAFT','PUBLISHED','CANCELLED','COMPLETED','CREATED')),
        updated_by varchar(255),
        primary key (id),
        unique (organizer_id, name, start_date, location)
    );

    create table favorite (
        is_active boolean,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        event_id uuid not null,
        id uuid not null,
        user_id uuid not null,
        created_by varchar(255),
        updated_by varchar(255),
        primary key (id),
        unique (user_id, event_id)
    );

    create table images (
        is_active boolean,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        event_id uuid,
        id uuid not null,
        created_by varchar(255),
        updated_by varchar(255),
        url varchar(255) not null,
        primary key (id)
    );

    create table payments (
        amount numeric(19,2) not null,
        currency varchar(3) not null,
        is_active boolean,
        created_at timestamp(6) not null,
        paid_at timestamp(6),
        updated_at timestamp(6) not null,
        booking_id uuid not null,
        id uuid not null,
        user_id uuid not null,
        created_by varchar(255),
        failure_reason varchar(255),
        gateway_response TEXT,
        metadata TEXT,
        payment_gateway varchar(255) not null check (payment_gateway in ('STRIPE','PAYPAL','ORANGE_MONEY','MTN_MONEY','WAVE','FLUTTERWAVE','INTERNAL')),
        payment_method varchar(255) not null check (payment_method in ('CREDIT_CARD','DEBIT_CARD','PAYPAL','STRIPE','MOBILE_MONEY','BANK_TRANSFER','CASH')),
        payment_reference varchar(255) unique,
        status varchar(255) not null check (status in ('PENDING','PROCESSING','COMPLETED','FAILED','CANCELLED','REFUNDED')),
        stripe_client_secret varchar(255),
        stripe_payment_intent_id varchar(255),
        transaction_id varchar(255) unique,
        updated_by varchar(255),
        primary key (id)
    );

    create table price_categories (
        available_tickets integer not null,
        deleted boolean not null,
        is_active boolean,
        price numeric(38,2) not null,
        sold_tickets integer,
        total_tickets integer not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        event_id uuid not null,
        id uuid not null,
        created_by varchar(255),
        name varchar(255) not null,
        updated_by varchar(255),
        primary key (id),
        constraint uk_price_category_event_name unique (event_id, name)
    );

    create table refresh_token (
        expiration_date timestamp(6) with time zone,
        id bigint generated by default as identity,
        user_id uuid,
        token TEXT,
        primary key (id)
    );

    create table seats (
        is_active boolean,
        price numeric(10,2) not null,
        created_at timestamp(6) not null,
        reserved_until timestamp(6),
        updated_at timestamp(6) not null,
        version bigint,
        booking_id uuid,
        event_id uuid,
        id uuid not null,
        seat_number varchar(20) not null,
        created_by varchar(255),
        reserved_by varchar(255),
        row_number varchar(255),
        seat_type varchar(255) not null check (seat_type in ('REGULAR','VIP','PREMIUM')),
        section varchar(255),
        status varchar(255) not null check (status in ('AVAILABLE','RESERVED','BOOKED','BLOCKED')),
        updated_by varchar(255),
        primary key (id),
        constraint uk_event_seat unique (event_id, seat_number)
    );

    create table tickets (
        is_active boolean,
        price float(53) not null,
        checked_in_at timestamp(6),
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        booking_id uuid,
        checked_in_by uuid,
        event_id uuid not null,
        id uuid not null,
        user_id uuid not null,
        category varchar(255),
        created_by varchar(255),
        seat_number varchar(255),
        status varchar(255) not null check (status in ('VALID','USED','CANCELLED')),
        ticket_code varchar(255) not null unique,
        updated_by varchar(255),
        qr_code oid,
        primary key (id)
    );

    create table user_favorite_events (
        event_id uuid not null,
        user_id uuid not null,
        primary key (event_id, user_id)
    );

    create table users (
        is_account_non_expired boolean default true not null,
        is_account_non_locked boolean default true not null,
        is_active boolean,
        is_credentials_non_expired boolean default true not null,
        is_enabled boolean default true not null,
        is_verified boolean default false not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        country_id uuid,
        id uuid not null,
        created_by varchar(255),
        email varchar(255),
        full_name varchar(255),
        password varchar(255),
        reset_password_token varchar(255),
        role varchar(255) check (role in ('ADMIN','USER','ORGANIZER')),
        updated_by varchar(255),
        verification_token varchar(255),
        primary key (id)
    );

    create index idx_booking_item_booking
       on booking_items (booking_id);

    create index idx_seat_id
       on booking_seats (seat_id);

    create index idx_user_id
       on bookings (user_id);

    create index idx_booking_reference
       on bookings (booking_reference);

    create index idx_status
       on bookings (status);

    create index idx_booking_status_expires
       on bookings (status, expires_at);

    create index idx_event_status
       on seats (event_id, status);

    create index idx_reserved_until
       on seats (reserved_until);

    alter table if exists booking_items
       add constraint FKrw74irmyat5c39cnjkn02u99m
       foreign key (booking_id)
       references bookings;

    alter table if exists booking_seats
       add constraint FKmbi9ciapn0nvat63t0a8tv478
       foreign key (booking_id)
       references bookings;

    alter table if exists booking_seats
       add constraint FKm2vak166qv8osqwe5qcxsn1p
       foreign key (seat_id)
       references seats;

    alter table if exists cart_items
       add constraint FKpcttvuq4mxppo8sxggjtn5i2c
       foreign key (cart_id)
       references carts;

    alter table if exists event_attendees
       add constraint FK3mumymyj0ryrrywpf5ivgnf1f
       foreign key (user_id)
       references users;

    alter table if exists event_attendees
       add constraint FKg0w14vgqmpawqmil4fceac4yl
       foreign key (event_id)
       references events;

    alter table if exists event_staff
       add constraint FKnmedgj2x7kcloagd7oeandugm
       foreign key (user_id)
       references users;

    alter table if exists event_staff
       add constraint FKcx5y53tbd63eh8afafjecpq79
       foreign key (event_id)
       references events;

    alter table if exists events
       add constraint FKo6mla8j1p5bokt4dxrlmgwc28
       foreign key (category_id)
       references categories;

    alter table if exists events
       add constraint FK3oeo2ckntx5yi2kitu27qdc5j
       foreign key (country_id)
       references countries;

    alter table if exists events
       add constraint FKdocju8m76a3f8o6ljh2jrn2ra
       foreign key (organizer_id)
       references users;

    alter table if exists images
       add constraint FKjqkpwof0kqmekscxaso6u1bl3
       foreign key (event_id)
       references events;

    alter table if exists price_categories
       add constraint FK7hdx44av336jncr7nvbyrdlu8
       foreign key (event_id)
       references events;

    alter table if exists refresh_token
       add constraint FKjtx87i0jvq2svedphegvdwcuy
       foreign key (user_id)
       references users;

    alter table if exists seats
       add constraint FKku1rurcfuh5d7pve1vsyomxa6
       foreign key (booking_id)
       references bookings;

    alter table if exists seats
       add constraint FKn8dwqflg9k82ygrbsseghd7ca
       foreign key (event_id)
       references events;

    alter table if exists tickets
       add constraint FK4eqsebpimnjen0q46ja6fl2hl
       foreign key (user_id)
       references users;

    alter table if exists user_favorite_events
       add constraint FK7ufpx0akujq4yjngwto7e6eb0
       foreign key (event_id)
       references events;

    alter table if exists user_favorite_events
       add constraint FKswjisu2tkl8p2bkc3gh17mutt
       foreign key (user_id)
       references users;

    alter table if exists users
       add constraint FKjlpks00ofkq3sqd9hqiavv5lg
       foreign key (country_id)
       references countries;
