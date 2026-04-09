package com.example.smartcampus.service;

import com.example.smartcampus.dto.NewsCreateDTO;
import com.example.smartcampus.dto.NewsResponseDTO;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsCategory;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.UserRepository;

import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    public NewsResponseDTO createNews(NewsCreateDTO dto, User author) {
        News news = News.builder()
                .title(dto.getTitle())
                .body(dto.getBody())
                .category(dto.getCategory() != null ? dto.getCategory() : NewsCategory.OTRO)
                .coverUrl(dto.getCoverUrl())
                .attachmentUrl(dto.getAttachmentUrl())
                .careerId(dto.getCareerId())
                .authorId(author.getId())
                .published(dto.getPublished() != null ? dto.getPublished() : true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        return toDTO(newsRepository.save(news));
    }

    public List<NewsResponseDTO> getAllPublished() {
        return toDTOList(newsRepository.findAllByPublishedTrueOrderByCreatedAtDesc());
    }

    public List<NewsResponseDTO> getNewsByAuthor(User author) {
        return toDTOList(newsRepository.findAllByAuthorIdOrderByCreatedAtDesc(author.getId()));
    }

    public NewsResponseDTO updateNews(Long id, NewsCreateDTO dto, User author) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con id: " + id));

        if (!news.getAuthorId().equals(author.getId())) {
            throw new ForbiddenException("No tienes permiso para editar esta noticia");
        }

        if (dto.getTitle() != null) news.setTitle(dto.getTitle());
        if (dto.getBody() != null) news.setBody(dto.getBody());
        if (dto.getCategory() != null) news.setCategory(dto.getCategory());
        if (dto.getCoverUrl() != null) news.setCoverUrl(dto.getCoverUrl());
        if (dto.getAttachmentUrl() != null) news.setAttachmentUrl(dto.getAttachmentUrl());
        if (dto.getCareerId() != null) news.setCareerId(dto.getCareerId());
        if (dto.getPublished() != null) news.setPublished(dto.getPublished());

        news.setUpdatedAt(OffsetDateTime.now());

        return toDTO(newsRepository.save(news));
    }

    public NewsResponseDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));
        return toDTO(news);
    }

    public void deleteNews(Long id, User requester) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));

        if (!news.getAuthorId().equals(requester.getId())) {
            throw new ForbiddenException("No tienes permiso para eliminar esta noticia");
        }

        newsRepository.deleteById(id);
    }


    public Page<NewsResponseDTO> getRecentNews(
            String search,
            Integer careerId,
            NewsCategory category,
            int page,
            int size,
            String sortBy,
            String sortType) {
                
        Map<String, String> fieldMap = Map.of(
                        "createdAt",  "created_at",
                        "updatedAt",  "updated_at",
                        "title",      "title",
                        "created_at", "created_at",  // por si el frontend ya manda snake_case
                        "updated_at", "updated_at"
                );
                String safeSortBy = fieldMap.getOrDefault(sortBy, "created_at");

        Sort sort = sortType != null && sortType.equalsIgnoreCase("ASC")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Normalizar parámetros
        String searchParam   = (search != null && !search.isBlank()) ? search.trim() : null;
        String categoryParam = (category != null) ? category.name() : null;

        Page<News> result = newsRepository.findFiltered(searchParam, careerId, categoryParam, pageable);

        return result.map(this::toDTO);
    }

    private List<NewsResponseDTO> toDTOList(List<News> newsList) {
        Set<UUID> authorIds = newsList.stream()
                .map(News::getAuthorId)
                .collect(Collectors.toSet());

        Map<UUID, String> authorNames = userRepository.findAllById(authorIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u.getFullName() != null ? u.getFullName() : u.getEmail()
                ));

        return newsList.stream()
                .map(n -> toDTO(n, authorNames.getOrDefault(n.getAuthorId(), "Publicador")))
                .toList();
    }

    private NewsResponseDTO toDTO(News n, String authorName) {
        return new NewsResponseDTO(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getCategory(),
                n.getCoverUrl(),
                n.getAttachmentUrl(),
                n.getCareerId(),
                n.getAuthorId(),
                authorName,
                n.getPublished(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }

    private NewsResponseDTO toDTO(News n) {
        String authorName = "Publicador";
        var userOpt = userRepository.findById(n.getAuthorId());
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            authorName = u.getFullName() != null ? u.getFullName() : u.getEmail();
        }
        return toDTO(n, authorName);
    }
}