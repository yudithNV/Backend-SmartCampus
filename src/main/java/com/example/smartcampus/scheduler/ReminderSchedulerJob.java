package com.example.smartcampus.scheduler;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.entity.Reminder;
import com.example.smartcampus.repository.EventRegistrationRepository;
import com.example.smartcampus.repository.EventRepository;
import com.example.smartcampus.repository.ReminderRepository;
import com.example.smartcampus.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderSchedulerJob {

    private final ReminderRepository          reminderRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository             eventRepository;
    private final NotificationService         notificationService;

    @Scheduled(cron = "0 * * * * *") // cada minuto, igual que NewsSchedulerJob
    @Transactional
    public void processReminders() {
        List<Reminder> pending =
            reminderRepository.findBySentFalseAndScheduledAtBefore(OffsetDateTime.now());

        if (pending.isEmpty()) return;

        log.info("[ReminderScheduler] Procesando {} recordatorio(s)...", pending.size());

        for (Reminder reminder : pending) {
            try {
                // Obtener la inscripción para saber a qué estudiante notificar
                eventRegistrationRepository.findById(reminder.getEventRegistrationId())
                    .ifPresent(registration -> {
                        // Obtener el evento para el título/link
                        eventRepository.findById(registration.getEventId())
                            .ifPresent(event -> {
                                String title = "Recordatorio: " + event.getName();
                                String body  = "El evento \"" + event.getName()
                                             + "\" comienza mañana. ¡No olvides asistir!";
                                String link  = "/student/eventos/" + event.getId();

                                notificationService.create(
                                    registration.getStudentId(),
                                    title,
                                    body,
                                    link
                                );

                                log.info("[ReminderScheduler] Notificación creada para userId={}, evento='{}'",
                                    registration.getStudentId(), event.getName());
                            });
                    });

                // Marcar como enviado
                reminder.setSent(true);
                reminder.setSentAt(OffsetDateTime.now());
                reminderRepository.save(reminder);

            } catch (Exception e) {
                log.error("[ReminderScheduler] Error en reminder id={}: {}", reminder.getId(), e.getMessage());
            }
        }
    }
}