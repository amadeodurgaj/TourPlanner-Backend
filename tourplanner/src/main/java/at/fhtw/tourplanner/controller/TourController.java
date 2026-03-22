package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllTours(@CookieValue(name = "jwt", required = false) String token,
                                         @RequestParam String username) {
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
        }

        List<TourResponseDTO> tours = tourService.getAllToursByUser(user.getId());
        return ApiResponseUtil.success(tours, "Tours retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourById(@PathVariable UUID id,
                                        @CookieValue(name = "jwt", required = false) String token,
                                        @RequestParam String username) {
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
        }

        TourResponseDTO tour = tourService.getTourById(id, user.getId());
        if (tour == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(tour, "Tour retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<?> createTour(@Valid @RequestBody TourRequestDTO dto,
                                       @CookieValue(name = "jwt", required = false) String token,
                                       @RequestParam String username) {
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
        }

        TourResponseDTO created = tourService.createTour(dto, user.getId());
        if (created == null) {
            return ApiResponseUtil.error("Failed to create tour", HttpStatus.BAD_REQUEST);
        }

        return ApiResponseUtil.success(created, "Tour created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable UUID id,
                                       @Valid @RequestBody TourRequestDTO dto,
                                       @CookieValue(name = "jwt", required = false) String token,
                                       @RequestParam String username) {
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
        }

        TourResponseDTO updated = tourService.updateTour(id, dto, user.getId());
        if (updated == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(updated, "Tour updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTour(@PathVariable UUID id,
                                       @CookieValue(name = "jwt", required = false) String token,
                                       @RequestParam String username) {
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
        }

        boolean deleted = tourService.deleteTour(id, user.getId());
        if (!deleted) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(null, "Tour deleted successfully");
    }
}
