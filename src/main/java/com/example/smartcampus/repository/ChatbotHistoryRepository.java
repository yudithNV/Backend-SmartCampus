package com.example.smartcampus.repository;

import com.example.smartcampus.entity.ChatbotHistory;
import com.example.smartcampus.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotHistoryRepository extends JpaRepository<ChatbotHistory, Long> {

    /**
     * Historial de un estudiante ordenado del más reciente al más antiguo.
     * Útil si en el futuro se quiere exponer un endpoint GET /chatbot/history.
     */
    List<ChatbotHistory> findByUserOrderByCreatedAtDesc(User user);
}