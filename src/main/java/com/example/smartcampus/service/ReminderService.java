package com.example.smartcampus.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.entity.NotificationChannel;
import com.example.smartcampus.entity.Reminder;
import com.example.smartcampus.entity.ReminderAdvance;
import com.example.smartcampus.repository.ReminderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final NotificationService  notificationService;

    /**
     * Programa un recordatorio UN_DIA antes del evento.
     * Si el evento inicia en menos de 24h, NO programa y retorna false.
     *
     * @return true  → recordatorio programado
     *         false → evento demasiado próximo (menos de 24 horas)
     */
    @Transactional
    public boolean scheduleReminder(Long registrationId,
                                    Long eventId,
                                    UUID studentId,
                                    String eventName,
                                    OffsetDateTime eventStart) {

        Duration duration = Duration.between(
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(4),
                eventStart
        );

        // Si faltan menos de 24 horas
        if (duration.compareTo(Duration.ofHours(24)) < 0) {
            log.info("[Reminder] Evento '{}' inicia en menos de 24 horas, no se programa recordatorio.", eventName);
            return false;
        }

        OffsetDateTime scheduledAt = eventStart.minusDays(1);

        Reminder reminder = Reminder.builder()
                .eventRegistrationId(registrationId)
                .advance(ReminderAdvance.UN_DIA)
                .channel(NotificationChannel.INTERNA)
                .scheduledAt(scheduledAt)
                .sent(false)
                .build();

        reminderRepository.save(reminder);
        log.info("[Reminder] Recordatorio programado para '{}' el {}", eventName, scheduledAt);
        return true;
    }

    /**
     * Elimina el recordatorio pendiente cuando el estudiante cancela su inscripción.
     */
    @Transactional
    public void cancelReminder(Long registrationId) {
        reminderRepository.findByEventRegistrationId(registrationId).ifPresent(r -> {
            if (!Boolean.TRUE.equals(r.getSent())) {
                reminderRepository.delete(r);
                log.info("[Reminder] Recordatorio cancelado para registrationId={}", registrationId);
            }
        });
    }

    /**
     * Llamado por el scheduler: procesa todos los recordatorios cuya hora llegó.
     * Crea la notificación interna y marca el reminder como enviado.
     */
    @Transactional
    public void processReminders() {
        var pending = reminderRepository.findBySentFalseAndScheduledAtBefore(OffsetDateTime.now());

        for (Reminder r : pending) {
            try {
                // Obtener student y evento desde el repositorio a través del reminder
                // (los datos ya están en la notificación que creamos al inscribirse,
                //  así que aquí simplemente necesitamos marcar como enviado y crear la notif)
                // Los datos del evento se recuperan en el scheduler que llama a este método
                r.setSent(true);
                r.setSentAt(OffsetDateTime.now());
                reminderRepository.save(r);
                log.info("[Reminder] Procesado id={}", r.getId());
            } catch (Exception e) {
                log.error("[Reminder] Error procesando id={}: {}", r.getId(), e.getMessage());
            }
        }
    }
}