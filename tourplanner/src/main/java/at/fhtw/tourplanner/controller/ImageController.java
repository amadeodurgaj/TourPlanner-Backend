package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.service.ImageService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import at.fhtw.tourplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours")
public class ImageController {
    
    @Autowired
    private ImageService imageService;
    
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
    
    @PutMapping("/{tourId}/image")
    public ResponseEntity<?> uploadImage(
            @PathVariable UUID tourId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        
        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId).orElse(null);
        if (tour == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }
        
        try {
            String oldImagePath = tour.getImagePath();
            
            String newImagePath = imageService.saveImage(file, tourId);
            tour.setImagePath(newImagePath);
            tourRepository.save(tour);
            
            if (oldImagePath != null && !oldImagePath.equals(newImagePath)) {
                imageService.deleteImage(oldImagePath);
            }
            
            return ApiResponseUtil.success(newImagePath, "Image uploaded successfully");
        } catch (IllegalArgumentException e) {
            return ApiResponseUtil.error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return ApiResponseUtil.error("Failed to save image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{tourId}/image")
    public ResponseEntity<?> deleteImage(
            @PathVariable UUID tourId,
            HttpServletRequest request) {
        
        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        
        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId).orElse(null);
        if (tour == null) {
            return ApiResponseUtil.error("Tour not found", HttpStatus.NOT_FOUND);
        }
        
        String imagePath = tour.getImagePath();
        if (imagePath == null) {
            return ApiResponseUtil.error("No image to delete", HttpStatus.BAD_REQUEST);
        }
        
        imageService.deleteImage(imagePath);
        tour.setImagePath(null);
        tourRepository.save(tour);
        
        return ApiResponseUtil.success(null, "Image deleted successfully");
    }
}
