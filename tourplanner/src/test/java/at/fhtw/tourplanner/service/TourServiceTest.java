package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoutingService routingService;

    @Mock
    private ImageService imageService;

    @Mock
    private at.fhtw.tourplanner.repository.TourLogRepository tourLogRepository;

    @InjectMocks
    private TourService tourService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllToursByUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        TourEntity tour1 = new TourEntity();
        tour1.setId(UUID.randomUUID());
        tour1.setName("Tour 1");
        tour1.setUser(user);
        
        TourEntity tour2 = new TourEntity();
        tour2.setId(UUID.randomUUID());
        tour2.setName("Tour 2");
        tour2.setUser(user);
        
        when(tourRepository.findByUserId(userId)).thenReturn(Arrays.asList(tour1, tour2));
        
        List<TourResponseDTO> result = tourService.getAllToursByUser(userId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tour 1", result.get(0).name());
        assertEquals("Tour 2", result.get(1).name());
    }

    @Test
    void testGetTourById() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Test Tour");
        tour.setUser(user);
        
        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(tour));
        
        TourResponseDTO result = tourService.getTourById(tourId, userId);
        
        assertNotNull(result);
        assertEquals("Test Tour", result.name());
        assertEquals(tourId, result.id());
    }

    @Test
    void testCreateTour() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        TourRequestDTO requestDTO = new TourRequestDTO(
                "New Tour",
                "Description",
                "foot",
                "Location A",
                48.2082,
                16.3738,
                "Location B",
                48.2100,
                16.3800,
                0.0,
                null,
                null,
                null
        );
        
        TourEntity savedTour = new TourEntity();
        savedTour.setId(UUID.randomUUID());
        savedTour.setName("New Tour");
        savedTour.setUser(user);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tourRepository.save(any(TourEntity.class))).thenReturn(savedTour);
        
        TourResponseDTO result = tourService.createTour(requestDTO, userId);
        
        assertNotNull(result);
        assertEquals("New Tour", result.name());
        verify(tourRepository, times(1)).save(any(TourEntity.class));
    }

    @Test
    void testDeleteTour() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Tour to delete");
        tour.setImagePath("/uploads/test.jpg");
        tour.setUser(user);
        
        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(tour));
        doNothing().when(imageService).deleteImage(anyString());
        doNothing().when(tourRepository).deleteById(any(UUID.class));
        
        boolean result = tourService.deleteTour(tourId, userId);
        
        assertTrue(result);
        verify(imageService, times(1)).deleteImage("/uploads/test.jpg");
        verify(tourRepository, times(1)).deleteById(tourId);
    }

    @Test
    void testDeleteTourWithoutImage() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setName("Tour to delete");
        tour.setUser(user);
        
        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(tour));
        doNothing().when(tourRepository).deleteById(any(UUID.class));
        
        boolean result = tourService.deleteTour(tourId, userId);
        
        assertTrue(result);
        verify(imageService, never()).deleteImage(anyString());
        verify(tourRepository, times(1)).deleteById(tourId);
    }

    @Test
    void testUpdateTour() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);

        TourEntity existing = new TourEntity();
        existing.setId(tourId);
        existing.setName("Old Name");
        existing.setUser(user);

        TourRequestDTO dto = new TourRequestDTO(
                "Updated Name", "Updated Desc", "bike",
                "From", 1.0, 2.0, "To", 3.0, 4.0,
                100.0, "3600", null, null);

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.of(existing));
        when(tourRepository.save(any(TourEntity.class))).thenReturn(existing);

        TourResponseDTO result = tourService.updateTour(tourId, dto, userId);

        assertNotNull(result);
        assertEquals("Updated Name", result.name());
    }

    @Test
    void testGetTourByIdNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.getTourById(tourId, userId));
    }

    @Test
    void testCreateTourUserNotFound() {
        UUID userId = UUID.randomUUID();
        TourRequestDTO dto = new TourRequestDTO(
                "Tour", "Desc", "foot",
                "A", null, null, "B", null, null,
                0.0, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.createTour(dto, userId));
        verify(tourRepository, never()).save(any());
    }

    @Test
    void testUpdateTourNotFound() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TourRequestDTO dto = new TourRequestDTO(
                "Name", "Desc", "car",
                "A", null, null, "B", null, null,
                0.0, null, null, null);

        when(tourRepository.findByIdAndUserId(tourId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.updateTour(tourId, dto, userId));
    }

    @Test
    void testSearchTours() {
        UUID userId = UUID.randomUUID();
        when(tourRepository.searchByUserId(eq(userId), anyString())).thenReturn(List.of());

        List<TourResponseDTO> results = tourService.searchTours(userId, "test");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testRecalculateComputedAttributes() {
        UUID tourId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);

        TourEntity tour = new TourEntity();
        tour.setId(tourId);
        tour.setUser(user);

        when(tourLogRepository.countByTourId(tourId)).thenReturn(3L);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(tour);

        tourService.recalculateComputedAttributes(tour);

        verify(tourRepository, times(1)).save(tour);
        assertEquals(60, tour.getPopularityScore());
    }
}
