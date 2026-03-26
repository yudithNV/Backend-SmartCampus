package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.SuggestionRequestDTO;
import com.example.smartcampus.dto.SuggestionResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.SuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService service;

    // POST /api/suggestions — crear sugerencia
    @PostMapping
    public ResponseEntity<ApiResponse<SuggestionResponseDTO>> create(
            @RequestBody @Valid SuggestionRequestDTO dto,
            @AuthenticationPrincipal User user) {
        SuggestionResponseDTO result = service.create(dto, user);
        return ResponseEntity.ok(ApiResponse.ok("Sugerencia enviada correctamente", result));
    }

    // GET /api/suggestions/my — historial del estudiante (escalable para futuro)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SuggestionResponseDTO>>> getMy(
            @AuthenticationPrincipal User user) {
        List<SuggestionResponseDTO> result = service.getMy(user);
        return ResponseEntity.ok(ApiResponse.ok("Sugerencias obtenidas correctamente", result));
    }
}