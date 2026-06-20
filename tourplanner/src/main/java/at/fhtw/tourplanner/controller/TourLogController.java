package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.TourLogRequestDTO;
import at.fhtw.tourplanner.DTO.TourLogResponseDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.service.TourLogService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import at.fhtw.tourplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
public class TourLogController {

    @Autowired
    private TourLogService tourLogService;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    private UUID getUserIdFromJwt(HttpServletRequest request) {
        String jwt = cookieUtil.getJwtFromCookies(request);
        if (jwt == null) return null;
        return jwtUtil.extractUserId(jwt);
    }

    private ResponseEntity<?> validateTourAccess(UUID tourId, HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId).orElse(null);
        if (tour == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getLogsForTour(@PathVariable UUID tourId, HttpServletRequest request) {
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        List<TourLogResponseDTO> logs = tourLogService.getLogsByTourId(tourId);
        return ApiResponseUtil.success(logs, "Logs retrieved successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchLogs(
            @PathVariable UUID tourId,
            @RequestParam("q") String query,
            HttpServletRequest request) {
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        if (query == null || query.trim().isEmpty()) {
            return ApiResponseUtil.error("Query parameter 'q' is required and cannot be empty. Please provide a search term.", HttpStatus.BAD_REQUEST);
        }

        var results = tourLogService.searchLogs(tourId, query.trim());
        return ApiResponseUtil.success(results, "Log search completed successfully");
    }

    @GetMapping("/{logId}")
    public ResponseEntity<?> getLogById(@PathVariable UUID tourId, @PathVariable UUID logId, HttpServletRequest request) {
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        TourLogResponseDTO log = tourLogService.getLogById(logId, tourId);
        if (log == null) {
            return ApiResponseUtil.error("Log not found", HttpStatus.NOT_FOUND);
        }
        return ApiResponseUtil.success(log, "Log retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<?> createLog(
            @PathVariable UUID tourId,
            @Valid @RequestBody TourLogRequestDTO dto,
            HttpServletRequest request) {
        
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        TourLogResponseDTO created = tourLogService.createLog(dto, tourId);
        if (created == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }
        return ApiResponseUtil.success(created, "Log created successfully");
    }

    @PutMapping("/{logId}")
    public ResponseEntity<?> updateLog(
            @PathVariable UUID tourId,
            @PathVariable UUID logId,
            @Valid @RequestBody TourLogRequestDTO dto,
            HttpServletRequest request) {
        
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        TourLogResponseDTO updated = tourLogService.updateLog(logId, dto, tourId);
        if (updated == null) {
            return ApiResponseUtil.error("Log not found", HttpStatus.NOT_FOUND);
        }
        return ApiResponseUtil.success(updated, "Log updated successfully");
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<?> deleteLog(
            @PathVariable UUID tourId,
            @PathVariable UUID logId,
            HttpServletRequest request) {
        
        var validationError = validateTourAccess(tourId, request);
        if (validationError != null) return validationError;

        boolean deleted = tourLogService.deleteLog(logId, tourId);
        if (!deleted) {
            return ApiResponseUtil.error("Log not found", HttpStatus.NOT_FOUND);
        }
        return ApiResponseUtil.success(null, "Log deleted successfully");
    }
}
