package com.example.smartcampus.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.smartcampus.dto.AccessLogMetricsDTO;
import com.example.smartcampus.dto.AdminDashboardDTO;
import com.example.smartcampus.dto.ComplaintByCategoryDTO;
import com.example.smartcampus.dto.ComplaintMetricsDTO;
import com.example.smartcampus.dto.EventByCategory;
import com.example.smartcampus.dto.EventMetricsDTO;
import com.example.smartcampus.dto.EventRegistrationByCategory;
import com.example.smartcampus.dto.EventRegistrationMetricsDTO;
import com.example.smartcampus.dto.NewsByCategory;
import com.example.smartcampus.dto.NewsMetricsDTO;
import com.example.smartcampus.dto.SuggestionByCategory;
import com.example.smartcampus.dto.SuggestionMetricsDTO;
import com.example.smartcampus.dto.SuspiciousEmailDTO;
import com.example.smartcampus.dto.UserByCareerDTO;
import com.example.smartcampus.dto.UserMetricsDTO;
import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.repository.AccessLogRepository;
import com.example.smartcampus.repository.CareerRepository;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.EventRegistrationRepository;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.SuggestionRepository;
import com.example.smartcampus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final AccessLogRepository accessLogRepository;
    private final CareerRepository careerRepository;
    private final SuggestionRepository suggestionRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final NewsRepository newsRepository;

    /**
     * Obtiene todas las métricas para el dashboard del administrador
     * Optimizado: usa queries agregadas en BD en lugar de findAll()
     */
    public AdminDashboardDTO getDashboardMetrics() {
        return AdminDashboardDTO.builder()
                .users(getUserMetrics())
                .complaints(getComplaintMetrics())
                .accessLogs(getAccessLogMetrics())
                .suggestions(getSuggestionMetrics())
                .eventRegistrations(getEventRegistrationMetrics())
                .events(getEventMetrics())
                .news(getNewsMetrics())
                .totalSuggestions(suggestionRepository.countAllSuggestions())
                .publishedEvents(eventRepository.countPublishedEvents())
                .totalEventRegistrations(eventRegistrationRepository.countAllEventRegistrations())
                .publishedNews(newsRepository.countPublishedNews())
                .build();
    }

    /**
     * Calcula métricas de usuarios usando queries agregadas en BD
     * Optimizado: 3 queries SELECT COUNT en lugar de traer toda la tabla
     */
    private UserMetricsDTO getUserMetrics() {
        // Total usuarios (Query agregada)
        long total = userRepository.countAllUsers();

        // Por rol (Query agregada)
        Map<String, Long> byRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            byRole.put(role.name(), 0L);
        }
        userRepository.countUsersByRole().forEach(row -> {
            Role role = (Role) row[0];
            Long count = (Long) row[1];
            byRole.put(role.name(), count);
        });

        // Por estado (Query agregada)
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status status : Status.values()) {
            byStatus.put(status.name(), 0L);
        }
        userRepository.countUsersByStatus().forEach(row -> {
            Status status = (Status) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        });

        // Por carrera (Query agregada con JOIN)
        List<UserByCareerDTO> byCareer = userRepository.countUsersByCareer().stream()
                .map(row -> UserByCareerDTO.builder()
                        .careerId(((Number) row[0]).intValue())
                        .careerName((String) row[1])
                        .total((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        return UserMetricsDTO.builder()
                .total(total)
                .byRole(byRole)
                .byStatus(byStatus)
                .byCareer(byCareer)
                .build();
    }

    /**
     * Calcula métricas de reclamos usando queries agregadas en BD
     * Optimizado: 2 queries SELECT COUNT GROUP BY en lugar de traer toda la tabla
     */
    private ComplaintMetricsDTO getComplaintMetrics() {
        // Total reclamos (Query agregada)
        long total = complaintRepository.countAllComplaints();

        // Por estado (Query agregada)
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        complaintRepository.countComplaintsByStatus().forEach(row -> {
            ComplaintStatus status = (ComplaintStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        });

        // Por categoría (Query agregada)
        List<ComplaintByCategoryDTO> byCategory = complaintRepository.countComplaintsByCategory().stream()
                .map(row -> ComplaintByCategoryDTO.builder()
                        .category((String) row[0])
                        .total((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return ComplaintMetricsDTO.builder()
                .total(total)
                .byStatus(byStatus)
                .byCategory(byCategory)
                .build();
    }

    /**
     * Calcula métricas de accesos usando queries agregadas en BD
     * Optimizado: 2 queries COUNT y 1 GROUP BY HAVING en lugar de traer toda la tabla
     */
    private AccessLogMetricsDTO getAccessLogMetrics() {
        // Intentos exitosos y fallidos (Queries agregadas)
        long successfulAttempts = accessLogRepository.countSuccessfulAttempts();
        long failedAttempts = accessLogRepository.countFailedAttempts();

        // Correos sospechosos con más de 3 intentos fallidos (Query agregada)
        List<SuspiciousEmailDTO> suspiciousEmails = accessLogRepository.findSuspiciousEmailsWithCount()
                .stream()
                .map(row -> SuspiciousEmailDTO.builder()
                        .email((String) row[0])
                        .failedAttempts((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return AccessLogMetricsDTO.builder()
                .successfulAttempts(successfulAttempts)
                .failedAttempts(failedAttempts)
                .suspiciousEmails(suspiciousEmails)
                .build();
    }

    /**
     * Calcula métricas de sugerencias usando queries agregadas en BD
     * Optimizado: 1 query COUNT y 1 GROUP BY en lugar de traer toda la tabla
     */
    private SuggestionMetricsDTO getSuggestionMetrics() {
        // Total sugerencias (Query agregada)
        long total = suggestionRepository.countAllSuggestions();

        // Por categoría (Query agregada)
        List<SuggestionByCategory> byCategory = suggestionRepository.countSuggestionsByCategory().stream()
                .map(row -> SuggestionByCategory.builder()
                        .category((String) row[0])
                        .total((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return SuggestionMetricsDTO.builder()
                .total(total)
                .byCategory(byCategory)
                .build();
    }

    /**
     * Calcula métricas de registros de eventos usando queries agregadas en BD
     * Optimizado: 1 query COUNT y 1 GROUP BY con JOIN en lugar de traer toda la tabla
     */
    private EventRegistrationMetricsDTO getEventRegistrationMetrics() {
        // Total registros (Query agregada)
        long total = eventRegistrationRepository.countAllEventRegistrations();

        // Por categoría (Query agregada con JOIN)
        List<EventRegistrationByCategory> byCategory = eventRegistrationRepository.countEventRegistrationsByCategory().stream()
                .map(row -> EventRegistrationByCategory.builder()
                        .categoryId(((Number) row[0]).intValue())
                        .categoryName((String) row[1])
                        .totalRegistrations((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        return EventRegistrationMetricsDTO.builder()
                .total(total)
                .byCategory(byCategory)
                .build();
    }

    /**
     * Calcula métricas de eventos publicados usando queries agregadas en BD
     * Optimizado: 1 query COUNT y 1 GROUP BY con JOIN en lugar de traer toda la tabla
     */
    private EventMetricsDTO getEventMetrics() {
        // Total eventos (Query agregada)
        long total = eventRepository.countPublishedEvents();

        // Por categoría (Query agregada con JOIN)
        List<EventByCategory> byCategory = eventRepository.countEventsByCategory().stream()
                .map(row -> EventByCategory.builder()
                        .categoryId(((Number) row[0]).intValue())
                        .categoryName((String) row[1])
                        .totalEvents((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        return EventMetricsDTO.builder()
                .total(total)
                .byCategory(byCategory)
                .build();
    }

    /**
     * Calcula métricas de noticias publicadas usando queries agregadas en BD
     * Optimizado: 1 query COUNT y 1 GROUP BY en lugar de traer toda la tabla
     */
    private NewsMetricsDTO getNewsMetrics() {
        // Total noticias (Query agregada)
        long total = newsRepository.countPublishedNews();

        // Por categoría (Query agregada)
        List<NewsByCategory> byCategory = newsRepository.countNewsByCategory().stream()
                .map(row -> NewsByCategory.builder()
                        .category((String) row[0])
                        .totalNews((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return NewsMetricsDTO.builder()
                .total(total)
                .byCategory(byCategory)
                .build();
    }
}
