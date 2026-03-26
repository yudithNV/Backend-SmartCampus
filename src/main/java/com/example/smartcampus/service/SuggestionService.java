package com.example.smartcampus.service;

import com.example.smartcampus.dto.SuggestionRequestDTO;
import com.example.smartcampus.dto.SuggestionResponseDTO;
import com.example.smartcampus.entity.Suggestion;
import com.example.smartcampus.entity.SuggestionCategory;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository repository;

    // ─── Crear sugerencia ─────────────────────────────────────────────────────
    public SuggestionResponseDTO create(SuggestionRequestDTO dto, User user) {
        SuggestionCategory category;
        try {
            category = SuggestionCategory.valueOf(dto.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Categoría inválida: " + dto.getCategory());
        }

        Suggestion suggestion = Suggestion.builder()
                .studentId(user.getId())
                .category(category)
                .body(dto.getBody().trim())
                .build();

        return mapToDTO(repository.save(suggestion));
    }

    // ─── Historial de sugerencias del estudiante ──────────────────────────────
    public List<SuggestionResponseDTO> getMy(User user) {
        return repository.findByStudentIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────
    private SuggestionResponseDTO mapToDTO(Suggestion s) {
        return new SuggestionResponseDTO(
                s.getId(),
                s.getStudentId(),
                s.getCategory().name(),
                s.getBody(),
                s.getCreatedAt()
        );
    }
}