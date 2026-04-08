package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.LocationSearchResultDTO;
import at.fhtw.tourplanner.service.GeocodingService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private GeocodingService geocodingService;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ApiResponseUtil.success(List.of(), "Query too short");
        }

        List<LocationSearchResultDTO> results = geocodingService.search(q.trim());
        return ApiResponseUtil.success(results, "Locations found");
    }
}
