package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.TourRequestDTO;
import at.fhtw.tourplanner.DTO.TourResponseDTO;
import at.fhtw.tourplanner.service.PdfService;
import at.fhtw.tourplanner.service.TourService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import at.fhtw.tourplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private PdfService pdfService;

    private UUID getUserIdFromJwt(HttpServletRequest request) {
        String jwt = cookieUtil.getJwtFromCookies(request);
        if (jwt == null) return null;
        return jwtUtil.extractUserId(jwt);
    }

    @GetMapping
    public ResponseEntity<?> getAllTours(HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        List<TourResponseDTO> tours = tourService.getAllToursByUser(userId);
        return ApiResponseUtil.success(tours, "Tours retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourById(@PathVariable UUID id, HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        TourResponseDTO tour = tourService.getTourById(id, userId);
        if (tour == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(tour, "Tour retrieved successfully");
    }

    @PostMapping
    public ResponseEntity<?> createTour(@Valid @RequestBody TourRequestDTO dto, HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        TourResponseDTO created = tourService.createTour(dto, userId);
        if (created == null) {
            return ApiResponseUtil.error("Failed to create tour", HttpStatus.BAD_REQUEST);
        }

        return ApiResponseUtil.success(created, "Tour created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable UUID id,
                                       @Valid @RequestBody TourRequestDTO dto,
                                       HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        TourResponseDTO updated = tourService.updateTour(id, dto, userId);
        if (updated == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(updated, "Tour updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTour(@PathVariable UUID id, HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        boolean deleted = tourService.deleteTour(id, userId);
        if (!deleted) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }

        return ApiResponseUtil.success(null, "Tour deleted successfully");
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<?> downloadTourReport(@PathVariable UUID id, HttpServletRequest request) {
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized: Please log in to download tour reports", HttpStatus.UNAUTHORIZED);
        }

        byte[] pdfBytes = pdfService.generateTourReport(id, userId);
        if (pdfBytes == null) {
            return ApiResponseUtil.error("Tour not found or report generation failed. Please ensure the tour exists and try again.", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tour_report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
