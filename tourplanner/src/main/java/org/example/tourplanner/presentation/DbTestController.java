package org.example.tourplanner.presentation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db-test")
    public String testConnection() {
        jdbcTemplate.execute("SELECT 1");
        return "Database connection successful!";
    }
}