package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PdfServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourLogRepository tourLogRepository;

    @InjectMocks
    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateReport_Success() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Test Tour PDF");
        tour.setDescription("A test tour for PDF generation");
        tour.setTransportType("bike");
        tour.setFromLocation("Vienna");
        tour.setFromLatitude(48.2082);
        tour.setFromLongitude(16.3738);
        tour.setToLocation("Linz");
        tour.setToLatitude(48.3069);
        tour.setToLongitude(14.2858);
        tour.setDistance(185.5);
        tour.setEstimatedTime("2 h 30 min");
        tour.setPopularityScore(60);
        tour.setChildFriendliness(40);

        TourLogEntity log1 = new TourLogEntity();
        log1.setId(UUID.randomUUID());
        log1.setDateTime(LocalDateTime.of(2026, 5, 15, 10, 0));
        log1.setComment("Great ride along the Danube");
        log1.setDifficulty(TourLogEntity.Difficulty.MEDIUM);
        log1.setTotalDistance(185.5);
        log1.setTotalTime(150);
        log1.setRating(4);

        TourLogEntity log2 = new TourLogEntity();
        log2.setId(UUID.randomUUID());
        log2.setDateTime(LocalDateTime.of(2026, 6, 1, 9, 30));
        log2.setComment("Beautiful weather, slightly windy");
        log2.setDifficulty(TourLogEntity.Difficulty.EASY);
        log2.setTotalDistance(180.0);
        log2.setTotalTime(140);
        log2.setRating(5);

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(tourId)).thenReturn(List.of(log1, log2));

        byte[] result = pdfService.generateTourReport(tourId, userId);

        assertNotNull(result);
        assertTrue(result.length > 200);
        String header = new String(result, 0, Math.min(5, result.length));
        assertEquals("%PDF-", header.substring(0, 5));
    }

    @Test
    void testGenerateReport_TourNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.empty());

        byte[] result = pdfService.generateTourReport(tourId, userId);

        assertNull(result);
    }

    @Test
    void testGenerateReport_NoLogs() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Empty Tour");
        tour.setDescription("Tour with no logs");
        tour.setTransportType("foot");
        tour.setFromLocation("A");
        tour.setFromLatitude(48.0);
        tour.setFromLongitude(16.0);
        tour.setToLocation("B");
        tour.setToLatitude(48.1);
        tour.setToLongitude(16.1);
        tour.setDistance(5.0);
        tour.setEstimatedTime("1 h");
        tour.setPopularityScore(0);
        tour.setChildFriendliness(0);

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(tourId)).thenReturn(List.of());

        byte[] result = pdfService.generateTourReport(tourId, userId);

        assertNotNull(result);
        assertTrue(result.length > 200);
        String header = new String(result, 0, Math.min(5, result.length));
        assertEquals("%PDF-", header.substring(0, 5));
    }
}
