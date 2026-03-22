package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

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

        TourEntity saved = tourRepository.save(tour);
        return toResponseDTO(saved);
    }

    public TourResponseDTO updateTour(UUID tourId, TourRequestDTO dto, UUID userId) {
        return tourRepository.findByIdAndUserId(tourId, userId)
                .map(tour -> {
                    updateTourFromDTO(tour, dto);
                    tour.setUpdatedAt(LocalDateTime.now());
                    return toResponseDTO(tourRepository.save(tour));
                })
                .orElse(null);
    }

    public boolean deleteTour(UUID tourId, UUID userId) {
        if (tourRepository.findByIdAndUserId(tourId, userId).isPresent()) {
            tourRepository.deleteById(tourId);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void updateTourFromDTO(TourEntity tour, TourRequestDTO dto) {
        tour.setName(dto.name());
        tour.setDescription(dto.description());
        tour.setTransportType(dto.transportType());
        tour.setFromLocation(dto.fromLocation());
        tour.setToLocation(dto.toLocation());
        tour.setDistance(dto.distance());
        tour.setEstimatedTime(dto.estimatedTime());
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
        return new TourResponseDTO(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getTransportType(),
                tour.getFromLocation(),
                tour.getToLocation(),
                tour.getDistance(),
                tour.getEstimatedTime(),
                tour.getRouteInfo(),
                tour.getChildFriendliness(),
                tour.getPopularityScore(),
                tour.getUser().getId(),
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}
