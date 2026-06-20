package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourLogRequestDTO;
import at.fhtw.tourplanner.DTO.TourLogResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TourLogServiceTest {

    @Mock
    private TourLogRepository tourLogRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourService tourService;

    @InjectMocks
    private TourLogService tourLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLogsByTourId() {
        UUID tourId = UUID.randomUUID();
        
        TourEntity tour = new TourEntity();
        tour.setId(tourId);

        TourLogEntity log1 = new TourLogEntity();
        log1.setId(UUID.randomUUID());
        log1.setComment("First log");
        log1.setDifficulty(TourLogEntity.Difficulty.EASY);
        log1.setDateTime(LocalDateTime.now());
        log1.setTour(tour);
        
        TourLogEntity log2 = new TourLogEntity();
        log2.setId(UUID.randomUUID());
        log2.setComment("Second log");
        log2.setDifficulty(TourLogEntity.Difficulty.MEDIUM);
        log2.setDateTime(LocalDateTime.now().minusDays(1));
        log2.setTour(tour);
        
        when(tourLogRepository.findByTourId(tourId)).thenReturn(Arrays.asList(log1, log2));
        
        List<TourLogResponseDTO> result = tourLogService.getLogsByTourId(tourId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("First log", result.get(0).comment());
        assertEquals("Second log", result.get(1).comment());
    }

    @Test
    void testCreateLog() {
        UUID tourId = UUID.randomUUID();
        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        
        TourLogRequestDTO requestDTO = new TourLogRequestDTO(
                LocalDateTime.now(),
                "Test log comment",
                TourLogEntity.Difficulty.HARD,
                10.5,
                60,
                4
        );
        
        TourLogEntity savedLog = new TourLogEntity();
        savedLog.setId(UUID.randomUUID());
        savedLog.setComment("Test log comment");
        savedLog.setDifficulty(TourLogEntity.Difficulty.HARD);
        savedLog.setTour(tour);
        
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tour));
        when(tourLogRepository.save(any(TourLogEntity.class))).thenReturn(savedLog);
        
        TourLogResponseDTO result = tourLogService.createLog(requestDTO, tourId);
        
        assertNotNull(result);
        assertEquals("Test log comment", result.comment());
        assertEquals("HARD", result.difficulty());
        verify(tourLogRepository, times(1)).save(any(TourLogEntity.class));
    }

    @Test
    void testCreateLogTourNotFound() {
        UUID tourId = UUID.randomUUID();
        
        TourLogRequestDTO requestDTO = new TourLogRequestDTO(
                LocalDateTime.now(),
                "Test log comment",
                TourLogEntity.Difficulty.MEDIUM,
                5.0,
                30,
                3
        );
        
        when(tourRepository.findById(tourId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> tourLogService.createLog(requestDTO, tourId));
        verify(tourLogRepository, never()).save(any(TourLogEntity.class));
    }

    @Test
    void testDeleteLog() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        
        TourLogEntity log = new TourLogEntity();
        log.setId(logId);
        log.setComment("Log to delete");
        
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.of(log));
        doNothing().when(tourLogRepository).deleteById(any(UUID.class));
        
        boolean result = tourLogService.deleteLog(logId, tourId);
        
        assertTrue(result);
        verify(tourLogRepository, times(1)).deleteById(logId);
    }

    @Test
    void testDeleteLogNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> tourLogService.deleteLog(logId, tourId));
        verify(tourLogRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testGetLogById() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        TourEntity tour = new TourEntity();
        tour.setId(tourId);

        TourLogEntity log = new TourLogEntity();
        log.setId(logId);
        log.setComment("Found log");
        log.setDifficulty(TourLogEntity.Difficulty.EASY);
        log.setTour(tour);
        log.setDateTime(LocalDateTime.now());

        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.of(log));

        TourLogResponseDTO result = tourLogService.getLogById(logId, tourId);

        assertNotNull(result);
        assertEquals("Found log", result.comment());
    }

    @Test
    void testGetLogByIdNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourLogService.getLogById(logId, tourId));
    }

    @Test
    void testUpdateLog() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        TourEntity tour = new TourEntity();
        tour.setId(tourId);

        TourLogEntity existing = new TourLogEntity();
        existing.setId(logId);
        existing.setComment("Old comment");
        existing.setDifficulty(TourLogEntity.Difficulty.EASY);
        existing.setTour(tour);
        existing.setDateTime(LocalDateTime.now());

        TourLogRequestDTO updateDTO = new TourLogRequestDTO(
                LocalDateTime.now(),
                "Updated comment",
                TourLogEntity.Difficulty.HARD,
                15.0,
                120,
                5
        );

        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.of(existing));
        when(tourLogRepository.save(any(TourLogEntity.class))).thenReturn(existing);
        when(tourRepository.findById(tourId)).thenReturn(Optional.of(tour));

        TourLogResponseDTO result = tourLogService.updateLog(logId, updateDTO, tourId);

        assertNotNull(result);
        assertEquals("Updated comment", result.comment());
        assertEquals("HARD", result.difficulty());
        verify(tourLogRepository, times(1)).save(any(TourLogEntity.class));
    }

    @Test
    void testUpdateLogNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        when(tourLogRepository.findByIdAndTourId(logId, tourId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourLogService.updateLog(logId, null, tourId));
    }
}
