package com.events.migration;

import com.events.common.abstraction.SpringSecurityAuditorAware;
import com.events.common.config.JpaConfig;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie les migrations Flyway sur un vrai PostgreSQL 16 (embarqué, sans Docker) :
 * une base neuve doit correspondre exactement aux entités, et une base existante
 * créée par ddl-auto=update doit être mise à niveau par V2.
 */
class FlywayMigrationTest {

    private static EmbeddedPostgres postgres;

    @BeforeAll
    static void startPostgres() throws IOException {
        postgres = EmbeddedPostgres.builder().start();
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) {
            postgres.close();
        }
    }

    private static DataSource freshDatabase(String name) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE DATABASE " + name);
        }
        return postgres.getDatabase("postgres", name);
    }

    private static Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
    }

    @Test
    void freshDatabaseAppliesAllMigrations() throws Exception {
        MigrateResult result = flyway(freshDatabase("fresh_db")).migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isEqualTo(2);
    }

    @Test
    void existingDdlAutoDatabaseIsUpgradedByV2() throws Exception {
        DataSource ds = freshDatabase("legacy_db");
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        // Base "historique" : schéma créé par Hibernate avant les nouvelles fonctionnalités, sans Flyway
        String v1 = new ClassPathResource("db/migration/V1__initial_schema.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        jdbc.execute(v1);
        jdbc.execute("DROP TABLE booking_items CASCADE");
        jdbc.execute("ALTER TABLE bookings DROP COLUMN event_name, DROP COLUMN expires_at");
        jdbc.execute("ALTER TABLE tickets DROP COLUMN booking_id, DROP COLUMN category, "
                + "DROP COLUMN checked_in_at, DROP COLUMN checked_in_by");
        jdbc.execute("ALTER TABLE price_categories DROP CONSTRAINT uk_price_category_event_name");
        jdbc.execute("ALTER TABLE price_categories ADD CONSTRAINT uk_old_name UNIQUE (name)");
        jdbc.execute("ALTER TABLE bookings DROP CONSTRAINT bookings_status_check");
        jdbc.execute("ALTER TABLE bookings ADD CONSTRAINT bookings_status_check "
                + "CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','FAILED'))");

        MigrateResult result = flyway(ds).migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isEqualTo(1); // V1 considérée comme baseline, V2 appliquée
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.tables "
                + "WHERE table_name = 'booking_items'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.columns "
                + "WHERE table_name = 'tickets' AND column_name = 'checked_in_at'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_constraint WHERE conname = 'uk_old_name'",
                Integer.class)).isZero();

        // Le statut EXPIRED est désormais accepté, un statut inconnu toujours refusé
        jdbc.execute("ALTER TABLE bookings ALTER COLUMN created_at DROP NOT NULL");
        jdbc.update("INSERT INTO bookings (id, event_id, user_id, total_amount, booking_reference, status, updated_at) "
                + "VALUES (gen_random_uuid(), gen_random_uuid(), gen_random_uuid(), 10, 'BK-1', 'EXPIRED', now())");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO bookings (id, event_id, user_id, total_amount, "
                + "booking_reference, status, updated_at) VALUES (gen_random_uuid(), gen_random_uuid(), "
                + "gen_random_uuid(), 10, 'BK-2', 'BOGUS', now())"))
                .hasMessageContaining("bookings_status_check");
    }

    /**
     * Base migrée par Flyway, puis Hibernate en mode validate : le schéma doit correspondre aux entités.
     */
    @Nested
    @DataJpaTest(properties = {"spring.flyway.enabled=true", "spring.jpa.hibernate.ddl-auto=validate"})
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @Import({JpaConfig.class, SpringSecurityAuditorAware.class})
    class HibernateValidation {

        @TestConfiguration
        static class PostgresDataSource {
            @Bean
            DataSource dataSource() throws Exception {
                return freshDatabase("validated_db");
            }
        }

        @Autowired
        private DataSource dataSource;

        @Test
        void schemaMatchesEntities() throws Exception {
            // Le contexte n'a démarré que si Flyway a migré et qu'Hibernate a validé le schéma
            try (Connection c = dataSource.getConnection()) {
                assertThat(c.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            }
        }
    }
}
