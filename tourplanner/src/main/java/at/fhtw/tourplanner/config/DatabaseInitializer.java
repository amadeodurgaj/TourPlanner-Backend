package at.fhtw.tourplanner.config;

import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerUtil.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        int updated = jdbcTemplate.update(
            "UPDATE users SET registration_date = NOW() WHERE registration_date IS NULL"
        );
        if (updated > 0) {
            log.warn("Backfilled registration_date for {} user(s) where it was NULL", updated);
        }
    }
}
