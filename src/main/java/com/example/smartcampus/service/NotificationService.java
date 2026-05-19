package com.example.smartcampus.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartcampus.entity.Notification;
import com.example.smartcampus.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void create(UUID userId, String title, String body, String link) {
        Notification n = Notification.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .link(link)
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}