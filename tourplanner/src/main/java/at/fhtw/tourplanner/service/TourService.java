package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourService {

    private static final Logger log = LoggerUtil.getLogger(TourService.class);

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourLogRepository tourLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private ImageService imageService;

    public List<TourResponseDTO> getAllToursByUser(UUID userId) {
        List<TourEntity> tours = tourRepository.findByUserId(userId);
        if (tours.isEmpty()) {
            log.info("No tours found for user: {}", userId);
        }
        return tours.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TourResponseDTO getTourById(UUID tourId, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
    }

    public TourResponseDTO createTour(TourRequestDTO dto, UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        TourEntity tour = new TourEntity();
        updateTourFromDTO(tour, dto);
        tour.setUser(user);
        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());

        if (dto.fromLatitude() != null && dto.toLatitude() != null) {
            try {
                RoutingService.RouteInfo route = routingService.calculateRoute(
                        dto.fromLatitude(), dto.fromLongitude(),
                        dto.toLatitude(), dto.toLongitude(),
                        dto.transportType()
                );
                double distanceKm = route.distanceMeters() / 1000.0;
                tour.setDistance(distanceKm);
                tour.setEstimatedTime(formatDuration(route.durationSeconds()));
            } catch (Exception e) {
                log.error("Routing calculation failed for tour '{}': {}", dto.name(), e.getMessage());
                log.warn("Created tour '{}' without route information due to routing service failure", dto.name());
            }
        }

        TourEntity saved = tourRepository.save(tour);
        log.info("Tour created successfully: {} (ID: {})", dto.name(), saved.getId());
        return toResponseDTO(saved);
    }

    public TourResponseDTO updateTour(UUID tourId, TourRequestDTO dto, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(tour -> {
                    boolean coordinatesChanged = !java.util.Objects.equals(tour.getFromLatitude(), dto.fromLatitude())
                            || !java.util.Objects.equals(tour.getFromLongitude(), dto.fromLongitude())
                            || !java.util.Objects.equals(tour.getToLatitude(), dto.toLatitude())
                            || !java.util.Objects.equals(tour.getToLongitude(), dto.toLongitude());

                    boolean transportTypeChanged = !java.util.Objects.equals(tour.getTransportType(), dto.transportType());

                    tour.setUpdatedAt(LocalDateTime.now());

                    if ((coordinatesChanged || transportTypeChanged) && dto.fromLatitude() != null && dto.toLatitude() != null) {
                        try {
                            RoutingService.RouteInfo route = routingService.calculateRoute(
                                    dto.fromLatitude(), dto.fromLongitude(),
                                    dto.toLatitude(), dto.toLongitude(),
                                    dto.transportType()
                            );
                            double distanceKm = route.distanceMeters() / 1000.0;
                            tour.setDistance(distanceKm);
                            tour.setEstimatedTime(formatDuration(route.durationSeconds()));
                            log.info("Recalculated route for tour '{}': {}km, transport: {}", dto.name(), distanceKm, dto.transportType());
                        } catch (Exception e) {
                            log.error("Failed to recalculate route for tour '{}': {}", dto.name(), e.getMessage());
                            log.warn("Updated tour '{}' without route recalculation due to routing service failure", dto.name());
                        }
                    }

                    updateTourFromDTO(tour, dto);
                    TourEntity updated = tourRepository.save(tour);
                    log.info("Tour updated successfully: {} (ID: {})", dto.name(), tourId);
                    return toResponseDTO(updated);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
    }

    @Transactional
    public boolean deleteTour(UUID tourId, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(tour -> {
                    if (tour.getImagePath() != null) {
                        imageService.deleteImage(tour.getImagePath());
                    }
                    tourLogRepository.deleteByTourId(tourId);
                    tourRepository.deleteById(tourId);
                    log.info("Tour deleted successfully: {} (ID: {})", tour.getName(), tourId);
                    return true;
                })
                .orElse(false);
    }

    public List<TourResponseDTO> searchTours(UUID userId, String query) {
        return tourRepository.searchByUserId(userId, query).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void recalculateComputedAttributes(TourEntity tour) {
        long logCount = tourLogRepository.countByTourId(tour.getId());
        int popularity = (int) Math.min(logCount * 20, 100);
        tour.setPopularityScore(popularity);

        List<TourLogEntity> logs = tourLogRepository.findByTourId(tour.getId());
        int childFriendliness = 0;
        if (!logs.isEmpty()) {
            double avgDifficulty = logs.stream()
                    .mapToInt(l -> switch (l.getDifficulty()) {
                        case EASY -> 1;
                        case MEDIUM -> 3;
                        case HARD -> 5;
                    })
                    .average()
                    .orElse(3);
            double avgTime = logs.stream().mapToInt(TourLogEntity::getTotalTime).average().orElse(0);
            double avgDistance = logs.stream().mapToDouble(TourLogEntity::getTotalDistance).average().orElse(0);

            double difficultyScore = Math.max(0, 100 - (avgDifficulty - 1) * 25);
            double timeScore = avgTime < 60 ? 50 : avgTime < 180 ? 25 : 0;
            double distanceScore = avgDistance < 10 ? 50 : avgDistance < 30 ? 25 : 0;

            childFriendliness = (int) Math.round(difficultyScore * 0.5 + timeScore * 0.25 + distanceScore * 0.25);
        }
        tour.setChildFriendliness(childFriendliness);

        tourRepository.save(tour);
    }

    public UserEntity getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public String exportTours(UUID userId) {
        List<TourResponseDTO> tours = getAllToursByUser(userId);
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tours);
        } catch (Exception e) {
            log.error("Failed to export tours: {}", e.getMessage());
            throw new RuntimeException("Failed to export tours: " + e.getMessage(), e);
        }
    }

    public int importTours(String jsonData, UserEntity user) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonData);
            int count = 0;

            if (root.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : root) {
                    TourRequestDTO dto = new TourRequestDTO(
                            node.get("name").asText(),
                            node.get("description").asText(),
                            node.get("transportType").asText(),
                            node.get("fromLocation").asText(),
                            node.has("fromLatitude") ? node.get("fromLatitude").asDouble() : null,
                            node.has("fromLongitude") ? node.get("fromLongitude").asDouble() : null,
                            node.get("toLocation").asText(),
                            node.has("toLatitude") ? node.get("toLatitude").asDouble() : null,
                            node.has("toLongitude") ? node.get("toLongitude").asDouble() : null,
                            node.has("distance") ? node.get("distance").asDouble() : 0.0,
                            node.has("estimatedTime") && !node.get("estimatedTime").isNull() ? node.get("estimatedTime").asText() : null,
                            node.has("routeInfo") && !node.get("routeInfo").isNull() ? mapper.convertValue(node.get("routeInfo"), Map.class) : null,
                            node.has("imagePath") && !node.get("imagePath").isNull() ? node.get("imagePath").asText() : null
                    );

                    TourEntity tour = new TourEntity();
                    updateTourFromDTO(tour, dto);
                    tour.setUser(user);
                    tour.setCreatedAt(LocalDateTime.now());
                    tour.setUpdatedAt(LocalDateTime.now());

                    tourRepository.save(tour);
                    count++;
                }
            }

            log.info("Imported {} tours for user {}", count, user.getUsername());
            return count;
        } catch (Exception e) {
            log.error("Failed to import tours: {}", e.getMessage());
            throw new RuntimeException("Failed to import tours: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTourFromDTO(TourEntity tour, TourRequestDTO dto) {
        tour.setName(dto.name());
        tour.setDescription(dto.description());
        tour.setTransportType(dto.transportType());
        tour.setFromLocation(dto.fromLocation());
        tour.setFromLatitude(dto.fromLatitude());
        tour.setFromLongitude(dto.fromLongitude());
        tour.setToLocation(dto.toLocation());
        tour.setToLatitude(dto.toLatitude());
        tour.setToLongitude(dto.toLongitude());
        tour.setImagePath(dto.imagePath());
        Object routeInfoObj = dto.routeInfo();
        if (routeInfoObj == null) {
            tour.setRouteInfo(null);
        } else if (routeInfoObj instanceof Map) {
            tour.setRouteInfo((Map<String, Object>) routeInfoObj);
        } else if (routeInfoObj instanceof String) {
            String jsonStr = (String) routeInfoObj;
            if (jsonStr.isEmpty()) {
                tour.setRouteInfo(null);
            } else {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                try {
                    tour.setRouteInfo(mapper.readValue(jsonStr, Map.class));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid routeInfo JSON: " + jsonStr);
                }
            }
        }
    }

    private TourResponseDTO toResponseDTO(TourEntity tour) {
        String imagePath = tour.getImagePath();
        if (imagePath != null && !imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }
        return new TourResponseDTO(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getTransportType(),
                tour.getFromLocation(),
                tour.getFromLatitude(),
                tour.getFromLongitude(),
                tour.getToLocation(),
                tour.getToLatitude(),
                tour.getToLongitude(),
                tour.getDistance(),
                tour.getEstimatedTime(),
                tour.getRouteInfo(),
                tour.getChildFriendliness(),
                tour.getPopularityScore(),
                tour.getUser().getId(),
                tour.getCreatedAt(),
                tour.getUpdatedAt(),
                imagePath
        );
    }

    private String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "0 min";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%d h %d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }
}
