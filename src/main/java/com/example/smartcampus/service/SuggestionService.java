package com.example.smartcampus.service;

import com.example.smartcampus.dto.AdminSuggestionResponseDTO;
import com.example.smartcampus.dto.SuggestionReplyDTO;
import com.example.smartcampus.dto.SuggestionRequestDTO;
import com.example.smartcampus.dto.SuggestionResponseDTO;
import com.example.smartcampus.entity.Suggestion;
import com.example.smartcampus.entity.SuggestionCategory;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.SuggestionRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository repository;
    private final UserRepository userRepository;

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

    // ─── Eliminar sugerencia — solo el dueño puede eliminarla ─────────────────
    public void delete(Long id, User user) {
        Suggestion suggestion = repository.findByIdAndStudentId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Sugerencia no encontrada o no tienes permiso para eliminarla"));
        repository.delete(suggestion);
    }

    // ─── Admin: listado de todas las sugerencias con datos del estudiante (con filtro opcional) ─────
    public List<AdminSuggestionResponseDTO> getAllForAdmin(SuggestionCategory category) {
        List<Object[]> rows = (category == null)
                ? repository.findAllWithStudent()
                : repository.findAllWithStudentByCategory(category);

        return rows.stream()
                .map(row -> {
                    Suggestion s = (Suggestion) row[0];
                    User u       = (User) row[1];
                    return new AdminSuggestionResponseDTO(
                            s.getId(),
                            u.getFullName(),
                            s.getCategory(),
                            s.getBody(),
                            s.getCreatedAt(),
                            s.getAdminResponse(),
                            s.getRespondedBy() != null
                                    ? userRepository.findById(s.getRespondedBy())
                                            .map(User::getFullName).orElse(null)
                                    : null,
                            s.getRespondedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // ─── Admin: responder sugerencia ──────────────────────────────────────────
    public AdminSuggestionResponseDTO reply(Long id, SuggestionReplyDTO dto, User admin) {
        Suggestion suggestion = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sugerencia no encontrada"));

        suggestion.setAdminResponse(dto.getAdminResponse().trim());
        suggestion.setRespondedBy(admin.getId());
        suggestion.setRespondedAt(OffsetDateTime.now());

        repository.save(suggestion);

        return new AdminSuggestionResponseDTO(
                suggestion.getId(),
                userRepository.findById(suggestion.getStudentId())
                        .map(User::getFullName)
                        .orElse("Estudiante"),
                suggestion.getCategory(),
                suggestion.getBody(),
                suggestion.getCreatedAt(),
                suggestion.getAdminResponse(),
                admin.getFullName(),
                suggestion.getRespondedAt()
        );
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────
    private SuggestionResponseDTO mapToDTO(Suggestion s) {
        return new SuggestionResponseDTO(
                s.getId(),
                s.getStudentId(),
                s.getCategory() != null ? s.getCategory().name() : "OTRO",
                s.getBody(),
                s.getCreatedAt(),
                s.getAdminResponse(),
                s.getRespondedBy() != null
                        ? userRepository.findById(s.getRespondedBy())
                                .map(User::getFullName).orElse(null)
                        : null,
                s.getRespondedAt()
        );
    }
}