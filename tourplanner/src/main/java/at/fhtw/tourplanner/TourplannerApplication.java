package at.fhtw.tourplanner;

import at.fhtw.tourplanner.util.LoggerUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TourplannerApplication {

    private static final Logger log = LoggerUtil.getLogger(TourplannerApplication.class);

    public static void main(String[] args) {
        loadEnvFile();
        validateRequiredEnvVars();
        SpringApplication.run(TourplannerApplication.class, args);
    }

    private static void loadEnvFile() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                if (!System.getenv().containsKey(entry.getKey())) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception e) {
            log.warn("Could not load .env file. Using system environment variables.", e);
        }
    }

    private static void validateRequiredEnvVars() {
        String jwtSecret = System.getProperty("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.isEmpty() || jwtSecret.startsWith("YOUR_")) {
            log.warn("JWT_SECRET is not set or uses a placeholder value. Set a secure JWT_SECRET in your .env file or environment variables.");
        }

        String orsApiKey = System.getProperty("ORS_API_KEY");
        if (orsApiKey == null || orsApiKey.isEmpty() || orsApiKey.startsWith("YOUR_")) {
            log.warn("ORS_API_KEY is not set or uses a placeholder value. Set your OpenRouteService API key in your .env file.");
        }
    }
}
