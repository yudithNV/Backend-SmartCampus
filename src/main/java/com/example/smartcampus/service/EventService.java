package com.example.smartcampus.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.dto.*;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.EventRegistration;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    @Transactional
    public EventResponseDTO createEvent(EventCreateDTO dto, User user) {
        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime = parseDatetime(dto.getEndDate(), dto.getEndTime());

        // Validar conflicto de horario
        validarConflicto(dto.getLocationId(), startDatetime, endDatetime, 0L);

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
                .categoryId(dto.getCategoryId() != null ? dto.getCategoryId() : null)
                .authorId(user.getId())
                .isActive(dto.getPublish() != null && dto.getPublish())
                .build();

        return mapToDTO(eventRepository.save(event));
    }

    public List<EventResponseDTO> getAllPublished() {
        return eventRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
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

    @Transactional
    public EventResponseDTO updateEvent(Long id, EventCreateDTO dto, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para editar este evento");
        }

        OffsetDateTime start = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime end = parseDatetime(dto.getEndDate(), dto.getEndTime());

        validarConflicto(dto.getLocationId(), start, end, id);

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setEventType(dto.getEventType());
        event.setLocationId(dto.getLocationId());
        event.setStartDatetime(start);
        event.setEndDatetime(end);
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setPosterUrl(dto.getPosterUrl());
        event.setCareerId(dto.getCareerId());
        event.setCategoryId(dto.getCategoryId() != null ? dto.getCategoryId() : null);
        event.setIsActive(dto.getPublish() != null && dto.getPublish());

        return mapToDTO(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long id, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este evento");
        }
        eventRepository.deleteById(id);
    }

    private void validarConflicto(Integer locId, OffsetDateTime start, OffsetDateTime end, Long eventId) {
        Optional<Event> conflict = eventRepository.findConflictingEvent(locId, start, end, eventId);
        if (conflict.isPresent()) {
            throw new RuntimeException("Conflicto de horario en esta ubicación.");
        }
    }

    private EventResponseDTO mapToDTO(Event event) {
        String authorName = userRepository.findById(event.getAuthorId())
                .map(User::getFullName).orElse("Anonimo");

        LocationDTO locDTO = event.getLocationId() == null ? null :
                locationRepository.findById(event.getLocationId())
                        .map(l -> new LocationDTO(l.getId(), l.getName(), l.getBlock(), l.getDescription()))
                        .orElse(null);

        CareerDTO careerDTO = event.getCareerId() == null ? null :
                careerRepository.findById(event.getCareerId())
                        .map(c -> new CareerDTO(c.getId(), c.getName(), c.getCode()))
                        .orElse(null);

        CategoryDTO catDTO = event.getCategoryId() == null ? null :
                categoryRepository.findById(event.getCategoryId().longValue())
                        .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getColorHex()))
                        .orElse(null);

        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .location(locDTO)
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .maxCapacity(event.getMaxCapacity())
                .posterUrl(event.getPosterUrl())
                .career(careerDTO)
                .category(catDTO)
                .authorId(event.getAuthorId())
                .authorName(authorName)
                .isActive(event.getIsActive())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private OffsetDateTime parseDatetime(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return null;
        try {
            return LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr))
                    .atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato inválido: use YYYY-MM-DD y HH:mm");
        }
    }

    private String formatTime(OffsetDateTime dt) {
        return dt == null ? "N/A" : dt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Page<EventResponseDTO> getRecentEvents(String search, Integer catId, Integer carId, int page, int size, String sort, String type) {
        List<String> allowedFields = List.of("createdAt", "startDatetime", "endDatetime", "name", "maxCapacity");
        String safeSortBy = allowedFields.contains(sort) ? sort : "createdAt";
        Sort sorting = type != null && type.equalsIgnoreCase("ASC") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sorting);
        
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = catId != null;
        boolean hasCareer = carId != null;

        Page<Event> result;
        if (hasSearch && hasCategory && hasCareer) {
            result = eventRepository.findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryIdAndCareerId(search, catId, carId, pageable);
        } else if (hasSearch && hasCategory) {
            result = eventRepository.findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryId(search, catId, pageable);
        } else if (hasSearch && hasCareer) {
            result = eventRepository.findByIsActiveTrueAndNameContainingIgnoreCaseAndCareerId(search, carId, pageable);
        } else if (hasCategory && hasCareer) {
            result = eventRepository.findByIsActiveTrueAndCategoryIdAndCareerId(catId, carId, pageable);
        } else if (hasSearch) {
            result = eventRepository.findByIsActiveTrueAndNameContainingIgnoreCase(search, pageable);
        } else if (hasCategory) {
            result = eventRepository.findByIsActiveTrueAndCategoryId(catId, pageable);
        } else if (hasCareer) {
            result = eventRepository.findByIsActiveTrueAndCareerId(carId, pageable);
        } else {
            result = eventRepository.findAllByIsActiveTrue(pageable);
        }
        
        return result.map(this::mapToDTO);
    }

    public List<EventResponseDTO> getEventsByMonthAndFilters(Integer year, Integer month, Integer day, Integer carId, Integer catId) {
        if (year == null || month == null) {
            throw new IllegalArgumentException("year y month son parámetros requeridos");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month debe estar entre 1 y 12");
        }

        OffsetDateTime start;
        OffsetDateTime end;
        
        if (day != null) {
            // Si se proporciona día, filtrar solo ese día
            if (day < 1 || day > 31) {
                throw new IllegalArgumentException("day debe estar entre 1 y 31");
            }
            YearMonth ym = YearMonth.of(year, month);
            LocalDate date = ym.atDay(Math.min(day, ym.lengthOfMonth()));
            start = date.atTime(0, 0, 0).atOffset(ZoneOffset.UTC);
            end = date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        } else {
            // Si no se proporciona día, filtrar todo el mes
            YearMonth ym = YearMonth.of(year, month);
            start = ym.atDay(1).atTime(0, 0, 0).atOffset(ZoneOffset.UTC);
            end = ym.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        }
        
        List<Event> events;
        boolean hasCareer = carId != null;
        boolean hasCategory = catId != null;

        if (hasCareer && hasCategory) {
            events = eventRepository.findByMonthCareerAndCategory(start, end, carId, catId);
        } else if (hasCareer) {
            events = eventRepository.findByMonthAndCareer(start, end, carId);
        } else if (hasCategory) {
            events = eventRepository.findByMonthAndCategory(start, end, catId);
        } else {
            events = eventRepository.findByMonthAndFilters(start, end);
        }
        
        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getRegisteredEvents(java.util.UUID studentId) {
        List<EventRegistration> registrations = eventRegistrationRepository.findByStudentId(studentId);
        
        return registrations.stream()
                .map(reg -> eventRepository.findById(reg.getEventId())
                        .map(this::mapToDTO)
                        .orElse(null))
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getEventsByStudentCareer(java.util.UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        if (student.getCareerId() == null) {
            throw new RuntimeException("El estudiante no tiene carrera asignada");
        }
        
        return eventRepository.findByCareerIdOrderByStartDatetimeAsc(student.getCareerId()).stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}