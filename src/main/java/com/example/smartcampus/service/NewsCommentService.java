package com.example.smartcampus.service;

import com.example.smartcampus.dto.CommentCreateDTO;
import com.example.smartcampus.dto.CommentResponseDTO;
import com.example.smartcampus.entity.NewsComment;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.NewsCommentRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.UserRepository;
import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsCommentService {

    private final NewsCommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;


    @Transactional
    public CommentResponseDTO createComment(Long newsId, CommentCreateDTO dto, User author) {
        if (!newsRepository.existsById(newsId)) {
            throw new NotFoundException("Noticia no encontrada");
        }

        String trimmed = dto.getBody().trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío o contener solo espacios");
        }

        NewsComment comment = NewsComment.builder()
                .newsId(newsId)
                .userId(author.getId())
                .body(trimmed)
                .hidden(false)
                .build();

        return toDTO(commentRepository.save(comment), author);
    }


    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getComments(Long newsId, User viewer) {
        boolean canSeeHidden = viewer != null &&
                (viewer.getRole() == Role.PUBLICADOR || viewer.getRole() == Role.ADMINISTRADOR);

        List<NewsComment> comments = canSeeHidden
                ? commentRepository.findByNewsIdOrderByCreatedAtDesc(newsId)
                : commentRepository.findByNewsIdAndHiddenFalseOrderByCreatedAtDesc(newsId);

        Set<UUID> authorIds = comments.stream().map(NewsComment::getUserId).collect(Collectors.toSet());
        Map<UUID, User> usersMap = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return comments.stream()
                .map(c -> toDTO(c, usersMap.get(c.getUserId()), viewer))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteComment(Long newsId, Long commentId, User user) {
        NewsComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        if (!comment.getNewsId().equals(newsId)) {
            throw new NotFoundException("Comentario no pertenece a esta noticia");
        }

        if (!comment.getUserId().equals(user.getId())) {
            throw new ForbiddenException("No puedes eliminar comentarios de otros usuarios");
        }

        commentRepository.deleteById(commentId);
    }


    @Transactional
    public CommentResponseDTO toggleHideComment(Long newsId, Long commentId, User user) {
        if (user.getRole() != Role.PUBLICADOR && user.getRole() != Role.ADMINISTRADOR) {
            throw new ForbiddenException("Solo publicadores o administradores pueden ocultar comentarios");
        }

        NewsComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        if (!comment.getNewsId().equals(newsId)) {
            throw new NotFoundException("Comentario no pertenece a esta noticia");
        }

        comment.setHidden(!comment.getHidden());
        return toDTO(commentRepository.save(comment), user);
    }

    private CommentResponseDTO toDTO(NewsComment c, User author) {
        return toDTO(c, author, null);
    }

    private CommentResponseDTO toDTO(NewsComment c, User author, User viewer) {
        boolean isOwn = viewer != null && viewer.getId().equals(c.getUserId());
        boolean canHide = viewer != null &&
                (viewer.getRole() == Role.PUBLICADOR || viewer.getRole() == Role.ADMINISTRADOR);

        return CommentResponseDTO.builder()
                .id(c.getId())
                .newsId(c.getNewsId())
                .userId(c.getUserId())
                .userFullName(author != null ? author.getFullName() : "Usuario")
                .userAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .body(c.getBody())
                .hidden(c.getHidden())
                .isOwn(isOwn)
                .canHide(canHide)
                .createdAt(c.getCreatedAt())
                .build();
    }
}