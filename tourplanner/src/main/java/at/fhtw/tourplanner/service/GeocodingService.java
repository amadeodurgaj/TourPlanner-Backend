package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.LocationSearchResultDTO;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    private static final Logger log = LoggerUtil.getLogger(GeocodingService.class);

    @Value("${openrouteservice.api-key}")
    private String apiKey;

    @Value("${openrouteservice.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<LocationSearchResultDTO> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            String url = baseUrl + "/geocode/search?text=" + query.trim() + "&size=5&lang=en";

            var headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", apiKey);
            var entity = new org.springframework.http.HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                return List.of();
            }

            Map<String, Object> body = response.getBody();
            List<LocationSearchResultDTO> results = new ArrayList<>();

            if (body.containsKey("features")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");

                for (Map<String, Object> feature : features) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
                    @SuppressWarnings("unchecked")
                    List<Double> coordinates = (List<Double>) geometry.get("coordinates");

                    if (properties != null && coordinates != null && coordinates.size() >= 2) {
                        String label = buildLabel(properties);
                        double longitude = coordinates.get(0);
                        double latitude = coordinates.get(1);

                        results.add(new LocationSearchResultDTO(label, latitude, longitude));
                    }
                }
            }

            return results;

        } catch (Exception e) {
            log.error("Geocoding error: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private String buildLabel(Map<String, Object> properties) {
        StringBuilder label = new StringBuilder();

        Object name = properties.get("name");
        if (name != null) {
            label.append(name);
        }

        Object locality = properties.get("locality");
        Object country = properties.get("country");

        if (locality != null) {
            if (label.length() > 0) label.append(", ");
            label.append(locality);
        }

        if (country != null) {
            if (label.length() > 0) label.append(", ");
            label.append(country);
        }

        return label.length() > 0 ? label.toString() : "Unknown Location";
    }
}
