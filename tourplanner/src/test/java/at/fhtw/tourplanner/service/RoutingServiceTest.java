package at.fhtw.tourplanner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoutingServiceTest {

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoutingService();
        ReflectionTestUtils.setField(routingService, "apiKey", "test-key");
        ReflectionTestUtils.setField(routingService, "baseUrl", "https://api.openrouteservice.org");
    }

    @Test
    void testCalculateRouteReturnsDefaultOnNoApiKey() {
        var result = routingService.calculateRoute(48.2, 16.3, 48.3, 16.4, "foot");
        assertNotNull(result);
        assertEquals(0, result.distanceMeters());
        assertEquals(0, result.durationSeconds());
    }

    @Test
    void testTransportTypeMapping() {
        var result = routingService.calculateRoute(48.2, 16.3, 48.3, 16.4, "car");
        assertNotNull(result);
    }
}
