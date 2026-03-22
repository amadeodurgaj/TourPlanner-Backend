package at.fhtw.tourplanner.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db-test")
    public Map<String, String> testConnection() {
        jdbcTemplate.execute("SELECT 1;");
        //return "Database connection successful!";
        return Map.of("message", "Database connection successful!");
    }

}