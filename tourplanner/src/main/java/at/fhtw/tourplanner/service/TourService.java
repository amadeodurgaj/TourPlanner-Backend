package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private UserRepository userRepository;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private ImageService imageService;

    public List<TourResponseDTO> getAllToursByUser(UUID userId) {
        return tourRepository.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TourResponseDTO getTourById(UUID tourId, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public TourResponseDTO createTour(TourRequestDTO dto, UUID userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

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
            }
        }

        TourEntity saved = tourRepository.save(tour);
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
                        }
                    }

                    updateTourFromDTO(tour, dto);
                    return toResponseDTO(tourRepository.save(tour));
                })
                .orElse(null);
    }

    public boolean deleteTour(UUID tourId, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(tour -> {
                    if (tour.getImagePath() != null) {
                        imageService.deleteImage(tour.getImagePath());
                    }
                    tourRepository.deleteById(tourId);
                    return true;
                })
                .orElse(false);
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
