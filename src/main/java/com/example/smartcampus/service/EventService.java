package com.example.smartcampus.service;

import com.example.smartcampus.dto.EventCreateDTO;
import com.example.smartcampus.dto.EventResponseDTO;
import com.example.smartcampus.entity.Event;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CareerService careerService;

    public EventResponseDTO createEvent(EventCreateDTO dto, User creator) {
        Event event = Event.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .eventDate(OffsetDateTime.parse(dto.getEventDate()))
            .location(dto.getLocation())
            .imageUrl(dto.getImageUrl())
            .category(dto.getCategory())
            .careerId(dto.getCareerId())
            .createdBy(creator.getFullName())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        Event saved = eventRepository.save(event);
        return toResponseDTO(saved);
    }

    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public List<EventResponseDTO> getUpcomingEvents() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(OffsetDateTime.now()).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        return toResponseDTO(event);
    }

    public EventResponseDTO updateEvent(Long id, EventCreateDTO dto, User updater) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(OffsetDateTime.parse(dto.getEventDate()));
        event.setLocation(dto.getLocation());
        event.setImageUrl(dto.getImageUrl());
        event.setCategory(dto.getCategory());
        event.setCareerId(dto.getCareerId());
        event.setUpdatedAt(OffsetDateTime.now());

        Event updated = eventRepository.save(event);
        return toResponseDTO(updated);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Evento no encontrado");
        }
        eventRepository.deleteById(id);
    }

    private EventResponseDTO toResponseDTO(Event event) {
        EventResponseDTO.CareerInfo careerInfo = null;
        if (event.getCareerId() != null) {
            careerInfo = careerService.getCareerById(event.getCareerId())
                .map(career -> new EventResponseDTO.CareerInfo(
                    career.getId(),
                    career.getName(),
                    career.getCode()
                ))
                .orElse(null);
        }

        return new EventResponseDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getEventDate() != null ? event.getEventDate().toString() : null,
            event.getLocation(),
            event.getImageUrl(),
            event.getCategory() != null ? event.getCategory().name() : null,
            careerInfo,
            event.getCreatedBy(),
            event.getCreatedAt() != null ? event.getCreatedAt().toString() : null,
            event.getUpdatedAt() != null ? event.getUpdatedAt().toString() : null
        );
    }
}
