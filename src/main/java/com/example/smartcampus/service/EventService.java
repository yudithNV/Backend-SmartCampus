package com.example.smartcampus.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.dto.CareerDTO;
import com.example.smartcampus.dto.CategoryDTO;
import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.dto.LocationDTO;
import com.example.smartcampus.dto.RegisteredEventsResponse;
import com.example.smartcampus.dto.UserPreferencesDTO;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.EventRegistration;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.CareerRepository;
import com.example.smartcampus.repository.CategoryRepository;
import com.example.smartcampus.repository.EventRegistrationRepository;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.LocationRepository;
import com.example.smartcampus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final UserPreferencesService preferencesService;

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public EventResponseDTO createEvent(EventCreateDTO dto, User user) {
        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime   = parseDatetime(dto.getEndDate(),   dto.getEndTime());

        checkLocationConflict(dto.getLocationId(), startDatetime, endDatetime, 0L);

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

        return mapToDTO(eventRepository.save(event));
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public List<EventResponseDTO> getAllPublished() {
        return eventRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getUpcomingEvents() {
        return eventRepository
                .findByStartDatetimeAfterOrderByStartDatetimeAsc(OffsetDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getEventsByAuthor(User user) {
        return eventRepository
                .findByAuthorIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<EventResponseDTO> getEventsByAuthor(User user, int page, int size, String sortBy, String sortType) {
        Sort sort = buildSort(sortBy, sortType,
                List.of("createdAt", "startDatetime", "name", "maxCapacity"),
                "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Event> result = eventRepository.findByAuthorId(user.getId(), pageable);
        return result.map(this::mapToDTO);
    }

    public List<EventResponseDTO> getEventsByCareer(Integer careerId) {
        return eventRepository
                .findByCareerIdOrderByStartDatetimeAsc(careerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EventResponseDTO getEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    public EventResponseDTO getEventById(Long id, User user) {
        return eventRepository.findById(id)
                .map(event -> mapToDTO(event, user.getId()))
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public EventResponseDTO updateEvent(Long id, EventCreateDTO dto, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para editar este evento");
        }

        OffsetDateTime startDatetime = parseDatetime(dto.getStartDate(), dto.getStartTime());
        OffsetDateTime endDatetime   = parseDatetime(dto.getEndDate(),   dto.getEndTime());

        checkLocationConflict(dto.getLocationId(), startDatetime, endDatetime, id);

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

        return mapToDTO(eventRepository.save(event));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public void deleteEvent(Long id, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!event.getAuthorId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este evento");
        }

        eventRepository.deleteById(id);
    }

    // ─── PAGINADO RECIENTE (filtros: search, category, career) ────────────────

    public Page<EventResponseDTO> getRecentEvents(
            String search, Integer categoryId, Integer careerId,
            int page, int size, String sortBy, String sortType) {

        Sort sort = buildSort(sortBy, sortType,
                List.of("createdAt", "startDatetime", "endDatetime", "name", "maxCapacity"),
                "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasSearch   = search != null && !search.isBlank();
        boolean hasCategory = categoryId != null;
        boolean hasCareer   = careerId != null;

        Page<Event> result;

        if (hasSearch && hasCategory && hasCareer) {
            result = eventRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryIdAndCareerId(
                    search, categoryId, careerId, pageable);
        } else if (hasSearch && hasCategory) {
            result = eventRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
                    search, categoryId, pageable);
        } else if (hasSearch && hasCareer) {
            result = eventRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseAndCareerId(
                    search, careerId, pageable);
        } else if (hasCategory && hasCareer) {
            result = eventRepository
                .findByIsActiveTrueAndCategoryIdAndCareerId(categoryId, careerId, pageable);
        } else if (hasSearch) {
            result = eventRepository
                .findByIsActiveTrueAndNameContainingIgnoreCase(search, pageable);
        } else if (hasCategory) {
            result = eventRepository.findByIsActiveTrueAndCategoryId(categoryId, pageable);
        } else if (hasCareer) {
            result = eventRepository.findByIsActiveTrueAndCareerId(careerId, pageable);
        } else {
            result = eventRepository.findAllByIsActiveTrue(pageable);
        }

        return result.map(this::mapToDTO);
    }

    public Page<EventResponseDTO> listEvents(
            int page, int size, String sortBy, String sortType) {

        Sort sort = buildSort(sortBy, sortType,
                List.of("createdAt", "startDatetime", "name", "maxCapacity"),
                "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Event> result = eventRepository.findAllByIsActiveTrue(pageable);
        return result.map(this::mapToDTO);
    }

    public Page<EventResponseDTO> listEventsOld(
            int page, int size, String sortBy, String eventType, Integer careerId) {

        Sort sort = buildSort(sortBy, null,
                List.of("startDatetime", "createdAt", "name", "maxCapacity"),
                "startDatetime");
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasType   = eventType != null && !eventType.isBlank();
        boolean hasCareer = careerId  != null;

        Page<Event> result;

        if (hasType && hasCareer) {
            result = eventRepository
                .findByIsActiveTrueAndEventTypeAndCareerId(eventType, careerId, pageable);
        } else if (hasType) {
            result = eventRepository
                .findByIsActiveTrueAndEventType(eventType, pageable);
        } else if (hasCareer) {
            result = eventRepository.findByIsActiveTrueAndCareerId(careerId, pageable);
        } else {
            result = eventRepository.findAllByIsActiveTrue(pageable);
        }

        return result.map(this::mapToDTO);
    }

    
    public List<EventResponseDTO> applyPreferencesOrder(
            List<EventResponseDTO> events, UUID userId) {

        UserPreferencesDTO prefs = preferencesService.getPreferences(userId);

        boolean hasPrefs = !prefs.getEventTypes().isEmpty()
                        || !prefs.getCategoryIds().isEmpty();

        if (!hasPrefs) {
            return events;
        }

        List<EventResponseDTO> recommended = new ArrayList<>();
        List<EventResponseDTO> rest        = new ArrayList<>();

        for (EventResponseDTO event : events) {
            // Comparar eventType: usar getValue() para obtener el String del enum
            boolean matchesType = event.getEventType() != null
                    && prefs.getEventTypes().contains(event.getEventType().getValue());

            // Comparar categoryId directamente (campo plano en el DTO)
            boolean matchesCategory = event.getCategoryId() != null
                    && prefs.getCategoryIds().contains(event.getCategoryId());

            if (matchesType || matchesCategory) {
                event.setRecommended(true);   // SCRUM-260 / SCRUM-404
                recommended.add(event);
            } else {
                event.setRecommended(false);
                rest.add(event);
            }
        }

        recommended.addAll(rest);
        return recommended;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void checkLocationConflict(
            Integer locationId, OffsetDateTime start, OffsetDateTime end, Long excludeId) {

        if (locationId == null || start == null || end == null) return;

        eventRepository.findConflictingEvent(locationId, start, end, excludeId)
                .ifPresent(conflict -> {
                    String locationName = locationRepository.findById(locationId)
                            .map(loc -> loc.getName())
                            .orElse("Ubicación desconocida");
                    throw new RuntimeException(
                            String.format("Error: Ya existe el evento \"%s\" en %s de %s a %s.",
                                    conflict.getName(),
                                    locationName,
                                    formatTime(conflict.getStartDatetime()),
                                    formatTime(conflict.getEndDatetime())));
                });
    }

    private EventResponseDTO mapToDTO(Event event) {
        return mapToDTO(event, null);
    }

    private EventResponseDTO mapToDTO(Event event, java.util.UUID studentId) {
        String authorName = userRepository.findById(event.getAuthorId())
                .map(User::getFullName)
                .orElse("Autor desconocido");

        LocationDTO locationDTO = null;
        if (event.getLocationId() != null) {
            locationDTO = locationRepository.findById(event.getLocationId())
                    .map(loc -> new LocationDTO(loc.getId(), loc.getName(),
                                                loc.getBlock(), loc.getDescription()))
                    .orElse(null);
        }

        CareerDTO careerDTO = event.getCareerId() != null
                ? careerRepository.findById(event.getCareerId())
                    .map(c -> new CareerDTO(c.getId(), c.getName(), c.getCode()))
                    .orElse(null)
                : null;

        CategoryDTO categoryDTO = event.getCategoryId() != null
                ? categoryRepository.findById(event.getCategoryId().longValue())
                    .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getColorHex()))
                    .orElse(null)
                : null;

        long registeredCount = eventRegistrationRepository.countByEventId(event.getId());
        Boolean isRegistered = studentId != null 
                ? eventRegistrationRepository.existsByEventIdAndStudentId(event.getId(), studentId)
                : null;

        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .location(locationDTO)
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .maxCapacity(event.getMaxCapacity())
                .posterUrl(event.getPosterUrl())
                .career(careerDTO)
                .authorId(event.getAuthorId())
                .authorName(authorName)
                .isActive(event.getIsActive())
                .category(categoryDTO)
                // ← campo plano necesario para applyPreferencesOrder (SCRUM-260)
                .categoryId(event.getCategoryId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .registeredCount(registeredCount)
                .isRegistered(isRegistered)
                // recommended se establece luego en applyPreferencesOrder
                .build();
    }

    private String formatTime(OffsetDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private OffsetDateTime parseDatetime(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return null;
        try {
            return LocalDate.parse(dateStr)
                    .atTime(LocalTime.parse(timeStr))
                    .atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Formato de fecha o hora inválido. Usa YYYY-MM-DD y HH:mm");
        }
    }

    private Sort buildSort(String sortBy, String sortType,
                           List<String> allowed, String defaultField) {
        String safe = allowed.contains(sortBy) ? sortBy : defaultField;
        return (sortType != null && sortType.equalsIgnoreCase("ASC"))
                ? Sort.by(safe).ascending()
                : Sort.by(safe).descending();
    }

    @Transactional
    public EventResponseDTO registerStudent(Long eventId, User student) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (eventRegistrationRepository.existsByEventIdAndStudentId(eventId, student.getId())) {
            throw new RuntimeException("Ya estás inscrito en este evento");
        }

        long registeredCount = eventRegistrationRepository.countByEventId(eventId);
        if (event.getMaxCapacity() != null && registeredCount >= event.getMaxCapacity()) {
            throw new RuntimeException("El evento ha alcanzado su capacidad máxima");
        }

        EventRegistration registration = EventRegistration.builder()
                .eventId(eventId)
                .studentId(student.getId())
                .build();

        eventRegistrationRepository.save(registration);
        return mapToDTO(event, student.getId());
    }

    @Transactional
    public EventResponseDTO unregisterStudent(Long eventId, User student) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        long deleted = eventRegistrationRepository.deleteByEventIdAndStudentId(eventId, student.getId());
        if (deleted == 0) {
            throw new RuntimeException("No estabas inscrito en este evento");
        }

        return mapToDTO(event, student.getId());
    }

    // ─── MÉTODOS PARA CALENDARIO Y REGISTROS POR FECHA ──────────────────────

    public List<EventResponseDTO> getEventsByMonthAndFilters(
            Integer year, Integer month, Integer day, Integer careerId, Integer categoryId) {

        OffsetDateTime startDate = LocalDate.of(year, month, day != null ? day : 1)
                .atTime(LocalTime.MIN)
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime endDate;
        if (day != null) {
            // Si especifica día, solo ese día
            endDate = startDate.plusDays(1);
        } else {
            // Si solo mes, todo el mes
            endDate = startDate.plusMonths(1);
        }

        List<Event> result;
        if (careerId != null && categoryId != null) {
            result = eventRepository.findByMonthCareerAndCategory(startDate, endDate, careerId, categoryId);
        } else if (careerId != null) {
            result = eventRepository.findByMonthAndCareer(startDate, endDate, careerId);
        } else if (categoryId != null) {
            result = eventRepository.findByMonthAndCategory(startDate, endDate, categoryId);
        } else {
            result = eventRepository.findByMonthAndFilters(startDate, endDate);
        }

        return result.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getRegisteredEvents(UUID studentId) {
        List<EventRegistration> registrations = eventRegistrationRepository.findByStudentId(studentId);
        return registrations.stream()
                .map(reg -> eventRepository.findById(reg.getEventId())
                        .map(event -> mapToDTO(event, studentId))
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public RegisteredEventsResponse getRegisteredEvents(UUID studentId, String startDateStr, String endDateStr) {
        OffsetDateTime startDate = parseDatetime(startDateStr, "00:00");
        OffsetDateTime endDate = parseDatetime(endDateStr, "23:59");

        List<EventRegistration> registrations = eventRegistrationRepository.findRegisteredEventsByMonthAndStudent(studentId, startDate, endDate);
        List<EventResponseDTO> events = registrations.stream()
                .map(reg -> eventRepository.findById(reg.getEventId())
                        .map(event -> mapToDTO(event, studentId))
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return RegisteredEventsResponse.builder()
                .events(events)
                .total(events.size())
                .build();
    }

    public RegisteredEventsResponse getRegisteredEvents(
            UUID studentId, String startDateStr, String endDateStr, 
            Integer careerId, Integer categoryId) {
        
        OffsetDateTime startDate = parseDatetime(startDateStr, "00:00");
        OffsetDateTime endDate = parseDatetime(endDateStr, "23:59");

        List<EventRegistration> registrations;
        
        // Usar diferentes métodos del repositorio según los filtros disponibles
        if (careerId != null && categoryId != null) {
            registrations = eventRegistrationRepository.findRegisteredEventsByMonthStudentCareerAndCategory(
                studentId, startDate, endDate, careerId, categoryId);
        } else if (careerId != null) {
            registrations = eventRegistrationRepository.findRegisteredEventsByMonthStudentAndCareer(
                studentId, startDate, endDate, careerId);
        } else if (categoryId != null) {
            registrations = eventRegistrationRepository.findRegisteredEventsByMonthStudentAndCategory(
                studentId, startDate, endDate, categoryId);
        } else {
            registrations = eventRegistrationRepository.findRegisteredEventsByMonthAndStudent(
                studentId, startDate, endDate);
        }

        List<EventResponseDTO> events = registrations.stream()
                .map(reg -> eventRepository.findById(reg.getEventId())
                        .map(event -> mapToDTO(event, studentId))
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return RegisteredEventsResponse.builder()
                .events(events)
                .total(events.size())
                .build();
    }

    public List<EventResponseDTO> getEventsByStudentCareer(UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        if (student.getCareerId() == null) {
            return new ArrayList<>();
        }

        return eventRepository.findByCareerIdOrderByStartDatetimeAsc(student.getCareerId())
                .stream()
                .map(event -> mapToDTO(event, studentId))
                .collect(Collectors.toList());
    }
}
