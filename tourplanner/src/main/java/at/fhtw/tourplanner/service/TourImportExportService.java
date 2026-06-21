package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TourImportExportService {

    private static final Logger log = LoggerUtil.getLogger(TourImportExportService.class);

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public TourImportExportService(TourRepository tourRepository, UserRepository userRepository,
                                   ObjectMapper objectMapper) {
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public String exportTours(UUID userId) {
        List<TourEntity> tours = tourRepository.findByUserId(userId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    tours.stream().map(this::toExportDTO).toList()
            );
        } catch (Exception e) {
            log.error("Failed to export tours: {}", e.getMessage());
            throw new RuntimeException("Failed to export tours: " + e.getMessage(), e);
        }
    }

    @Transactional
    public int importTours(String jsonData, UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            int count = 0;

            if (root.isArray()) {
                for (JsonNode node : root) {
                    TourRequestDTO dto = new TourRequestDTO(
                            getRequiredText(node, "name"),
                            getRequiredText(node, "description"),
                            getRequiredText(node, "transportType"),
                            getRequiredText(node, "fromLocation"),
                            node.has("fromLatitude") ? node.get("fromLatitude").asDouble() : null,
                            node.has("fromLongitude") ? node.get("fromLongitude").asDouble() : null,
                            getRequiredText(node, "toLocation"),
                            node.has("toLatitude") ? node.get("toLatitude").asDouble() : null,
                            node.has("toLongitude") ? node.get("toLongitude").asDouble() : null,
                            node.has("distance") ? node.get("distance").asDouble() : 0.0,
                            node.has("estimatedTime") && !node.get("estimatedTime").isNull() ? node.get("estimatedTime").asText() : null,
                            node.has("routeInfo") && !node.get("routeInfo").isNull() ? objectMapper.convertValue(node.get("routeInfo"), Map.class) : null,
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

    private TourResponseDTO toExportDTO(TourEntity tour) {
        String imagePath = tour.getImagePath();
        if (imagePath != null && !imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }
        return new TourResponseDTO(
                tour.getId(), tour.getName(), tour.getDescription(),
                tour.getTransportType(), tour.getFromLocation(),
                tour.getFromLatitude(), tour.getFromLongitude(),
                tour.getToLocation(), tour.getToLatitude(), tour.getToLongitude(),
                tour.getDistance(), tour.getEstimatedTime(),
                tour.getRouteInfo(), tour.getChildFriendliness(),
                tour.getPopularityScore(), tour.getUser().getId(),
                tour.getCreatedAt(), tour.getUpdatedAt(), imagePath
        );
    }

    private static String getRequiredText(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return node.get(field).asText();
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
        } else if (routeInfoObj instanceof String jsonStr) {
            if (jsonStr.isEmpty()) {
                tour.setRouteInfo(null);
            } else {
                try {
                    tour.setRouteInfo(objectMapper.readValue(jsonStr, Map.class));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid routeInfo JSON: " + jsonStr);
                }
            }
        }
    }
}
