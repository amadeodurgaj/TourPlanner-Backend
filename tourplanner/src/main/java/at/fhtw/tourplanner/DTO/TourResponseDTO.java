package at.fhtw.tourplanner.DTO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record TourResponseDTO(
        UUID id,
        String name,
        String description,
        String transportType,
        String fromLocation,
        String toLocation,
        double distance,
        String estimatedTime,
        Map<String, Object> routeInfo,
        int childFriendliness,
        int popularityScore,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
