package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponseDTO>> create(
            @Valid @RequestBody EventCreateDTO dto,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = eventService.createEvent(dto, user);
        return ResponseEntity.ok(ApiResponse.ok("Evento creado exitosamente", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getAll() {
        List<EventResponseDTO> result = eventService.getAllPublished();
        return ResponseEntity.ok(ApiResponse.ok("Eventos obtenidos correctamente", result));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getUpcoming() {
        List<EventResponseDTO> result = eventService.getUpcomingEvents();
        return ResponseEntity.ok(ApiResponse.ok("Eventos próximos obtenidos", result));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getMyEvents(
            @AuthenticationPrincipal User user) {
        List<EventResponseDTO> result = eventService.getEventsByAuthor(user);
        return ResponseEntity.ok(ApiResponse.ok("Tus eventos obtenidos correctamente", result));
    }

    @GetMapping("/career/{careerId}")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getEventsByCareer(
            @PathVariable Integer careerId) {
        List<EventResponseDTO> result = eventService.getEventsByCareer(careerId);
        return ResponseEntity.ok(ApiResponse.ok("Eventos por carrera obtenidos", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDTO>> getById(@PathVariable Long id) {
        EventResponseDTO result = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.ok("Evento obtenido correctamente", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody EventCreateDTO dto,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = eventService.updateEvent(id, dto, user);
        return ResponseEntity.ok(ApiResponse.ok("Evento actualizado exitosamente", result));
    }
}
