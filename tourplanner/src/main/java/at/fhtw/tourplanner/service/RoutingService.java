package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RoutingService {

    private static final Logger log = LoggerUtil.getLogger(RoutingService.class);

    @Value("${openrouteservice.api-key}")
    private String apiKey;

    @Value("${openrouteservice.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public record RouteInfo(double distanceMeters, long durationSeconds) {}

    private static final Map<String, String> TRANSPORT_TYPE_MAP = Map.of(
            "foot", "foot-walking",
            "bike", "cycling-regular",
            "running", "foot-walking",
            "car", "driving-car"
    );

    public RouteInfo calculateRoute(double fromLat, double fromLng, double toLat, double toLng, String transportType) {
        String orsProfile = TRANSPORT_TYPE_MAP.getOrDefault(transportType, "foot-walking");
        log.info("Calculating route with profile: {} from ({},{}) to ({},{})", orsProfile, fromLat, fromLng, toLat, toLng);

        try {
            String url = baseUrl + "/v2/directions/" + orsProfile;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = "{\"coordinates\":[[" + fromLng + "," + fromLat + "],[" + toLng + "," + toLat + "]]}";
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                log.warn("Empty response from OpenRouteService");
                return new RouteInfo(0, 0);
            }

            Map<String, Object> body = response.getBody();

            if (body.containsKey("routes")) {
                @SuppressWarnings("unchecked")
                var routes = (java.util.List<Map<String, Object>>) body.get("routes");

                if (!routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    Map<String, Object> summary = (Map<String, Object>) route.get("summary");

                    double distance = 0;
                    long duration = 0;

                    if (summary != null) {
                        Object distanceObj = summary.get("distance");
                        Object durationObj = summary.get("duration");

                        if (distanceObj instanceof Number) {
                            distance = ((Number) distanceObj).doubleValue();
                        }
                        if (durationObj instanceof Number) {
                            duration = ((Number) durationObj).longValue();
                        }
                    }

                    log.info("Route calculated: {}m, {}s", distance, duration);
                    return new RouteInfo(distance, duration);
                }
            }

            log.warn("No routes found in OpenRouteService response");
            return new RouteInfo(0, 0);

        } catch (Exception e) {
            log.error("Routing error: {}", e.getMessage(), e);
            return new RouteInfo(0, 0);
        }
    }
}
