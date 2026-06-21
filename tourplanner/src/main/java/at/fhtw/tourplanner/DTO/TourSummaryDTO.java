package at.fhtw.tourplanner.DTO;

import java.util.UUID;

public record TourSummaryDTO(UUID id, String name, double distance) {}
