package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.LocationDTO;
import com.example.smartcampus.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationDTO>>> getAllLocations() {
        List<LocationDTO> locations = locationRepository.findAll().stream()
                .map(loc -> new LocationDTO(
                        loc.getId(),
                        loc.getName(),
                        loc.getBlock(),
                        loc.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Ubicaciones obtenidas correctamente", locations));
    }
}
