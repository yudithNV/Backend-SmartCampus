package com.example.smartcampus.service;

import com.example.smartcampus.dto.*;
import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.repository.AccessLogRepository;
import com.example.smartcampus.repository.CareerRepository;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final AccessLogRepository accessLogRepository;
    private final CareerRepository careerRepository;

    /**
     * Obtiene todas las métricas para el dashboard del administrador
     * Optimizado: usa queries agregadas en BD en lugar de findAll()
     */
    public AdminDashboardDTO getDashboardMetrics() {
        return AdminDashboardDTO.builder()
                .users(getUserMetrics())
                .complaints(getComplaintMetrics())
                .accessLogs(getAccessLogMetrics())
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
                        .careerId((Integer) row[0])
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
}
