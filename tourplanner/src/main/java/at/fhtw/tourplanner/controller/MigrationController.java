package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.service.RoutingService;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class MigrationController {

    private static final Logger log = LoggerUtil.getLogger(MigrationController.class);

    private final TourRepository tourRepository;
    private final RoutingService routingService;

    public MigrationController(TourRepository tourRepository, RoutingService routingService) {
        this.tourRepository = tourRepository;
        this.routingService = routingService;
    }

    @PostMapping("/migrate-geometry")
    public ResponseEntity<Map<String, Object>> migrateGeometry() {
        var allTours = tourRepository.findAll();
        var tours = allTours.stream()
                .filter(t -> t.getRouteInfo() == null || !t.getRouteInfo().containsKey("geometry"))
                .toList();
        int updated = 0;
        int failed = 0;

        for (TourEntity tour : tours) {
            if (tour.getFromLatitude() == null || tour.getToLatitude() == null) {
                log.warn("Skipping tour '{}' - missing coordinates", tour.getName());
                failed++;
                continue;
            }

            try {
                RoutingService.RouteInfo route = routingService.calculateRoute(
                        tour.getFromLatitude(), tour.getFromLongitude(),
                        tour.getToLatitude(), tour.getToLongitude(),
                        tour.getTransportType()
                );

                if (route.geometry() != null) {
                    tour.setRouteInfo(Map.of("geometry", route.geometry()));
                    tourRepository.save(tour);
                    updated++;
                    log.info("Migrated geometry for tour '{}'", tour.getName());
                } else {
                    log.warn("No geometry returned for tour '{}'", tour.getName());
                    failed++;
                }
            } catch (Exception e) {
                log.error("Failed to migrate tour '{}': {}", tour.getName(), e.getMessage());
                failed++;
            }
        }

        return ResponseEntity.ok(Map.of(
                "total", tours.size(),
                "updated", updated,
                "failed", failed
        ));
    }
}
