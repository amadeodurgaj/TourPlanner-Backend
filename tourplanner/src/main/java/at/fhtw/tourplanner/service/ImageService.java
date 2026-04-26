package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    
    private static final Logger log = LoggerUtil.getLogger(ImageService.class);
    
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public String saveImage(MultipartFile file, UUID tourId) throws IOException {
        validateFile(file);
        
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        
        Path tourDir = Paths.get(uploadDir, "tours", tourId.toString());
        Files.createDirectories(tourDir);
        
        Path filePath = tourDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        String relativePath = "/uploads/tours/" + tourId + "/" + fileName;
        log.info("Saved image: {} for tour: {}", relativePath, tourId);
        
        return relativePath;
    }
    
    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            String cleanPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            Path filePath = Paths.get(cleanPath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted image: {}", imagePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete image: {}", imagePath, e);
        }
    }
    
    public void deleteTourImages(UUID tourId) {
        try {
            Path tourDir = Paths.get(uploadDir, "tours", tourId.toString());
            if (Files.exists(tourDir)) {
                Files.walk(tourDir)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", path, e);
                            }
                        });
                log.info("Deleted all images for tour: {}", tourId);
            }
        } catch (IOException e) {
            log.error("Failed to delete images for tour: {}", tourId, e);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum of 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPEG, PNG, WebP");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "png" -> "png";
            case "webp" -> "webp";
            default -> "jpg";
        };
    }
}
