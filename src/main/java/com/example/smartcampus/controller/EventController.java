package com.example.smartcampus.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.dto.RegisteredEventsResponse;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<Page<EventResponseDTO>> listEvents(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "startDatetime") String sortBy,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer careerId,
            @AuthenticationPrincipal User user) {

        Page<EventResponseDTO> result = eventService.listEvents(page, size, sortBy, eventType, careerId);

        // SCRUM-145 / SCRUM-403: si es ESTUDIANTE, reordenar según preferencias
        if (user != null && user.getRole() == Role.ESTUDIANTE) {
            List<EventResponseDTO> sorted =
                eventService.applyPreferencesOrder(result.getContent(), user.getId());

            return ResponseEntity.ok(
                new PageImpl<>(sorted, result.getPageable(), result.getTotalElements())
            );
        }

        return ResponseEntity.ok(result);
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

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getCalendar(
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer careerId,
            @RequestParam(required = false) Integer categoryId) {
        List<EventResponseDTO> result = eventService.getEventsByMonthAndFilters(year, month, day, careerId, categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Eventos del calendario obtenidos correctamente", result));
    }

    @GetMapping("/registered")
    public ResponseEntity<ApiResponse<RegisteredEventsResponse>> getRegisteredEvents(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal User user) {
        RegisteredEventsResponse result = eventService.getRegisteredEvents(user.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Eventos registrados obtenidos correctamente", result));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<EventResponseDTO>> registerToEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = eventService.registerStudent(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Inscripción realizada con éxito", result));
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<ApiResponse<EventResponseDTO>> unregisterFromEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = eventService.unregisterStudent(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Inscripción cancelada con éxito", result));
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<EventResponseDTO>> getRecent(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer careerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortType) {
        return ResponseEntity.ok(eventService.getRecentEvents(search, categoryId, careerId, page, size, sortBy, sortType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDTO>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = user != null 
            ? eventService.getEventById(id, user)
            : eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.ok("Evento obtenido correctamente", result));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<EventResponseDTO>>> getEventsByStudent(
            @PathVariable java.util.UUID studentId) {
        List<EventResponseDTO> result = eventService.getEventsByStudentCareer(studentId);
        return ResponseEntity.ok(ApiResponse.ok("Eventos del estudiante obtenidos correctamente", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody EventCreateDTO dto,
            @AuthenticationPrincipal User user) {
        EventResponseDTO result = eventService.updateEvent(id, dto, user);
        return ResponseEntity.ok(ApiResponse.ok("Evento actualizado exitosamente", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        eventService.deleteEvent(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Evento eliminado exitosamente", null));
    }
}
