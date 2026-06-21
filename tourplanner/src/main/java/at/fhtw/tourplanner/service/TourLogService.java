package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourLogRequestDTO;
import at.fhtw.tourplanner.DTO.TourLogResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourLogService {

    private static final Logger logger = LoggerUtil.getLogger(TourLogService.class);

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final TourService tourService;

    public TourLogService(TourLogRepository tourLogRepository, TourRepository tourRepository,
                          TourService tourService) {
        this.tourLogRepository = tourLogRepository;
        this.tourRepository = tourRepository;
        this.tourService = tourService;
    }

    public List<TourLogResponseDTO> getLogsByTourId(UUID tourId) {
        return tourLogRepository.findByTourId(tourId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TourLogResponseDTO getLogById(UUID logId, UUID tourId) {
        return tourLogRepository.findByIdAndTourId(logId, tourId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Tour Log", logId));
    }

    public TourLogResponseDTO createLog(TourLogRequestDTO dto, UUID tourId) {
        TourEntity tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));

        TourLogEntity log = new TourLogEntity();
        updateLogFromDTO(log, dto);
        log.setTour(tour);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());

        TourLogResponseDTO saved = toResponseDTO(tourLogRepository.save(log));
        tourService.recalculateComputedAttributes(tour);
        logger.info("Recalculated computed attributes for tour '{}' after adding log", tour.getName());
        return saved;
    }

    public TourLogResponseDTO updateLog(UUID logId, TourLogRequestDTO dto, UUID tourId) {
        return tourLogRepository.findByIdAndTourId(logId, tourId)
                .map(log -> {
                    updateLogFromDTO(log, dto);
                    log.setUpdatedAt(LocalDateTime.now());
                    TourLogResponseDTO saved = toResponseDTO(tourLogRepository.save(log));
                    tourRepository.findById(tourId).ifPresent(tourService::recalculateComputedAttributes);
                    logger.info("Tour log updated successfully: {} (ID: {})", dto.comment(), logId);
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Tour Log", logId));
    }

    public List<TourLogResponseDTO> searchLogs(UUID tourId, String query) {
        return tourLogRepository.searchByTourId(tourId, query).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean deleteLog(UUID logId, UUID tourId) {
        return tourRepository.findById(tourId)
                .map(tour -> {
                    if (tourLogRepository.findByIdAndTourId(logId, tourId).isPresent()) {
                        tourLogRepository.deleteById(logId);
                        tourService.recalculateComputedAttributes(tour);
                        logger.info("Tour log deleted successfully: {} (ID: {}) from tour: {}", logId, tour.getName(), tourId);
                        return true;
                    }
                    logger.warn("Tour log not found: {} for tour: {}", logId, tourId);
                    throw new ResourceNotFoundException("Tour Log", logId);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
    }

    private void updateLogFromDTO(TourLogEntity log, TourLogRequestDTO dto) {
        log.setDateTime(dto.dateTime());
        log.setComment(dto.comment());
        log.setDifficulty(dto.difficulty());
        log.setTotalDistance(dto.totalDistance());
        log.setTotalTime(dto.totalTime());
        log.setRating(dto.rating());
    }

    private TourLogResponseDTO toResponseDTO(TourLogEntity log) {
        return new TourLogResponseDTO(
                log.getId(),
                log.getDateTime(),
                log.getComment(),
                log.getDifficulty().name(),
                log.getTotalDistance(),
                log.getTotalTime(),
                log.getRating(),
                log.getTour().getId(),
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }
}
