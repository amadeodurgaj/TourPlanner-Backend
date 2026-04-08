package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourLogRequestDTO;
import at.fhtw.tourplanner.DTO.TourLogResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourLogService {

    @Autowired
    private TourLogRepository tourLogRepository;

    @Autowired
    private TourRepository tourRepository;

    public List<TourLogResponseDTO> getLogsByTourId(UUID tourId) {
        return tourLogRepository.findByTourId(tourId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TourLogResponseDTO getLogById(UUID logId, UUID tourId) {
        return tourLogRepository.findByIdAndTourId(logId, tourId)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public TourLogResponseDTO createLog(TourLogRequestDTO dto, UUID tourId) {
        TourEntity tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) return null;

        TourLogEntity log = new TourLogEntity();
        updateLogFromDTO(log, dto);
        log.setTour(tour);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());

        return toResponseDTO(tourLogRepository.save(log));
    }

    public TourLogResponseDTO updateLog(UUID logId, TourLogRequestDTO dto, UUID tourId) {
        return tourLogRepository.findByIdAndTourId(logId, tourId)
                .map(log -> {
                    updateLogFromDTO(log, dto);
                    log.setUpdatedAt(LocalDateTime.now());
                    return toResponseDTO(tourLogRepository.save(log));
                })
                .orElse(null);
    }

    public boolean deleteLog(UUID logId, UUID tourId) {
        if (tourLogRepository.findByIdAndTourId(logId, tourId).isPresent()) {
            tourLogRepository.deleteById(logId);
            return true;
        }
        return false;
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
