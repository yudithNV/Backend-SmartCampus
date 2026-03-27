package com.example.smartcampus.service;

import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponseDTO createEvent(EventCreateDTO dto, User user) {
        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime = parseDatetime(dto.getEndDate(), dto.getEndTime());

        // Validar conflicto de horario en la misma ubicación
        boolean hasConflict = eventRepository.existsConflictingEvent(
                dto.getLocationId(),
                startDatetime,
                endDatetime,
                0L // Para creación, no hay ID previo
        );

        if (hasConflict) {
            throw new RuntimeException("Ya existe un evento en esta ubicación en el horario seleccionado");
        }

        Event event = Event.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .eventType(dto.getEventType())
                .locationId(dto.getLocationId())
                .startDatetime(startDatetime)
                .endDatetime(endDatetime)
                .maxCapacity(dto.getMaxCapacity())
                .posterUrl(dto.getPosterUrl())
                .careerId(dto.getCareerId())
                .categoryId(dto.getCategoryId())
                .authorId(user.getId())
                .isActive(dto.getPublish() != null && dto.getPublish())
                .build();

        Event saved = eventRepository.save(event);
        return mapToDTO(saved);
    }

    public List<EventResponseDTO> getAllPublished() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getIsActive() != null && e.getIsActive())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getUpcomingEvents() {
        return eventRepository.findByStartDatetimeAfterOrderByStartDatetimeAsc(OffsetDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getEventsByAuthor(User user) {
        return eventRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getEventsByCareer(Integer careerId) {
        return eventRepository.findByCareerIdOrderByStartDatetimeAsc(careerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EventResponseDTO getEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    public EventResponseDTO updateEvent(Long id, EventCreateDTO dto, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Validar que el usuario sea el autor del evento
        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para editar este evento");
        }

        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime = parseDatetime(dto.getEndDate(), dto.getEndTime());

        // Validar conflicto de horario en la misma ubicación
        boolean hasConflict = eventRepository.existsConflictingEvent(
                dto.getLocationId(),
                startDatetime,
                endDatetime,
                id // Para actualización, excluir el evento actual
        );

        if (hasConflict) {
            throw new RuntimeException("Ya existe un evento en esta ubicación en el horario seleccionado");
        }

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setEventType(dto.getEventType());
        event.setLocationId(dto.getLocationId());
        event.setStartDatetime(startDatetime);
        event.setEndDatetime(endDatetime);
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setPosterUrl(dto.getPosterUrl());
        event.setCareerId(dto.getCareerId());
        event.setCategoryId(dto.getCategoryId());
        event.setIsActive(dto.getPublish() != null && dto.getPublish());

        Event updated = eventRepository.save(event);
        return mapToDTO(updated);
    }

    private EventResponseDTO mapToDTO(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventType(),
                event.getLocationId(),
                event.getStartDatetime(),
                event.getEndDatetime(),
                event.getMaxCapacity(),
                event.getPosterUrl(),
                event.getCareerId(),
                event.getAuthorId(),
                event.getIsActive(),
                event.getCategoryId(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    /**
     * Combina una fecha y hora en formato string a OffsetDateTime
     * @param dateStr Formato: "YYYY-MM-DD"
     * @param timeStr Formato: "HH:mm"
     * @return OffsetDateTime en UTC, o null si alguno es null
     */
    private OffsetDateTime parseDatetime(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            return date.atTime(time).atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha o hora inválido. Usa YYYY-MM-DD y HH:mm");
        }
    }
}
