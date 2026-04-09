package com.example.smartcampus.service;

import com.example.smartcampus.dto.AccessLogResponseDTO;
import com.example.smartcampus.entity.AccessLog;
import com.example.smartcampus.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository repository;

    /** Umbral de intentos fallidos para marcar un correo como sospechoso */
    private static final int SUSPICIOUS_THRESHOLD = 3;

    // ─── Registrar un intento (exitoso o fallido) ─────────────────────────────
    public void record(String email, String ipAddress, String userAgent, boolean success) {
        AccessLog log = AccessLog.builder()
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .build();
        repository.save(log);
    }

    // ─── Obtener todos los logs con flag de sospechoso ────────────────────────
    public List<AccessLogResponseDTO> getAllLogs() {
        // Precalcula qué correos son sospechosos para no hacer N+1 queries
        List<Object[]> suspiciousRaw = repository.findSuspiciousEmails(SUSPICIOUS_THRESHOLD);
        List<String> suspiciousEmails = suspiciousRaw.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());

        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new AccessLogResponseDTO(
                        log.getId(),
                        log.getEmail(),
                        log.getSuccess(),
                        log.getUserAgent(),
                        log.getCreatedAt(),
                        suspiciousEmails.contains(log.getEmail())
                ))
                .collect(Collectors.toList());
    }
}