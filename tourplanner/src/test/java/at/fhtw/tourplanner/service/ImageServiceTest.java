package at.fhtw.tourplanner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {

    private ImageService imageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageService = new ImageService();
    }

    @Test
    void testSaveImageRejectsInvalidFileType() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        assertThrows(IllegalArgumentException.class, () ->
                imageService.saveImage(file, UUID.randomUUID()));
    }

    @Test
    void testDeleteImageHandlesNullPath() {
        assertDoesNotThrow(() -> imageService.deleteImage(null));
        assertDoesNotThrow(() -> imageService.deleteImage(""));
    }
}
