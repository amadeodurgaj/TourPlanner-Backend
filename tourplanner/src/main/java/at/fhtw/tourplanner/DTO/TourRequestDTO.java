package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record TourRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
        String name,

        @NotBlank(message = "Description is required")
        @Size(min = 1, max = 5000, message = "Description must be between 1 and 5000 characters")
        String description,

        @NotBlank(message = "Transport type is required")
        @Size(min = 1, max = 50, message = "Transport type must be between 1 and 50 characters")
        @Pattern(regexp = "foot|bike|running|car", message = "Transport type must be one of: foot, bike, running, car")
        String transportType,

        @NotBlank(message = "From location is required")
        @Size(min = 1, max = 500, message = "From location must be between 1 and 500 characters")
        String fromLocation,

        @NotNull(message = "From latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double fromLatitude,

        @NotNull(message = "From longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double fromLongitude,

        @NotBlank(message = "To location is required")
        @Size(min = 1, max = 500, message = "To location must be between 1 and 500 characters")
        String toLocation,

        @NotNull(message = "To latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double toLatitude,

        @NotNull(message = "To longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double toLongitude,

        @PositiveOrZero(message = "Distance must be >= 0")
        double distance,

        @Size(max = 100, message = "Estimated time must be <= 100 characters")
        String estimatedTime,

        Object routeInfo,

        @Size(max = 2000, message = "Image path must be <= 2000 characters")
        String imagePath
) {}
