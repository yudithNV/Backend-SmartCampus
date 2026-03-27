package com.example.smartcampus.service;

import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.LocationRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public EventResponseDTO createEvent(EventCreateDTO dto, User user) {
        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime = parseDatetime(dto.getEndDate(), dto.getEndTime());

        // Validar conflicto de horario en la misma ubicación
        Optional<Event> conflictingEvent = eventRepository.findConflictingEvent(
                dto.getLocationId(),
                startDatetime,
                endDatetime,
                0L // Para creación, no hay ID previo
        );

        if (conflictingEvent.isPresent()) {
            Event conflict = conflictingEvent.get();
            String locationName = locationRepository.findById(dto.getLocationId())
                    .map(loc -> loc.getName())
                    .orElse("Ubicación desconocida");

            String startTime = formatTime(conflict.getStartDatetime());
            String endTime = formatTime(conflict.getEndDatetime());

            throw new RuntimeException(
                    String.format("Error: Ya existe el evento \"%s\" en %s de %s a %s.",
                            conflict.getName(),
                            locationName,
                            startTime,
                            endTime)
            );
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
        Optional<Event> conflictingEvent = eventRepository.findConflictingEvent(
                dto.getLocationId(),
                startDatetime,
                endDatetime,
                id // Para actualización, excluir el evento actual
        );

        if (conflictingEvent.isPresent()) {
            Event conflict = conflictingEvent.get();
            String locationName = locationRepository.findById(dto.getLocationId())
                    .map(loc -> loc.getName())
                    .orElse("Ubicación desconocida");

            String startTime = formatTime(conflict.getStartDatetime());
            String endTime = formatTime(conflict.getEndDatetime());

            throw new RuntimeException(
                    String.format("Error: Ya existe el evento \"%s\" en %s de %s a %s.",
                            conflict.getName(),
                            locationName,
                            startTime,
                            endTime)
            );
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
        String authorName = userRepository.findById(event.getAuthorId())
                .map(User::getFullName)
                .orElse("Autor desconocido");

        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .locationId(event.getLocationId())
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .maxCapacity(event.getMaxCapacity())
                .posterUrl(event.getPosterUrl())
                .careerId(event.getCareerId())
                .authorId(event.getAuthorId())
                .authorName(authorName)
                .isActive(event.getIsActive())
                .categoryId(event.getCategoryId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    /**
     * Formatea OffsetDateTime a formato HH:mm
     */
    private String formatTime(OffsetDateTime datetime) {
        if (datetime == null) return "N/A";
        return datetime.format(DateTimeFormatter.ofPattern("HH:mm"));
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
    public void deleteEvent(Long id, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Validar que el usuario sea el autor del evento antes de borrar
        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este evento");
        }

        eventRepository.delete(event);
    }
}
