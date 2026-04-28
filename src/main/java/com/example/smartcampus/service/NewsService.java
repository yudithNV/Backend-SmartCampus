package com.example.smartcampus.service;

import com.example.smartcampus.dto.NewsCreateDTO;
import com.example.smartcampus.dto.NewsResponseDTO;
import com.example.smartcampus.entity.Career;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsCategory;
import com.example.smartcampus.entity.NewsStatus;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.CareerRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.UserRepository;

import exception.ForbiddenException;
import exception.InvalidScheduledDateException;
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
import java.util.Objects; 

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;    
    private static final long MIN_SCHEDULED_MINUTES = 5;


    public NewsResponseDTO createNews(NewsCreateDTO dto, User author) {
        NewsStatus status = resolveStatus(dto);
        OffsetDateTime scheduledAt = resolveScheduledAt(dto, status);

        News news = News.builder()
                .title(dto.getTitle())
                .body(dto.getBody())
                .category(dto.getCategory() != null ? dto.getCategory() : NewsCategory.OTRO)
                .coverUrl(dto.getCoverUrl())
                .attachmentUrl(dto.getAttachmentUrl())
                .careerId(dto.getCareerId())
                .authorId(author.getId())
                .newsStatus(status)
                .scheduledAt(scheduledAt)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        news.syncPublished();
        return toDTO(newsRepository.save(news));
    }

    public List<NewsResponseDTO> getAllPublished() {
        return toDTOList(newsRepository.findAllByNewsStatusOrderByCreatedAtDesc(NewsStatus.PUBLICADO));
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
        if (dto.getNewsStatus() != null || dto.getPublished() != null) {NewsStatus newStatus = resolveStatus(dto);
                OffsetDateTime newScheduledAt = resolveScheduledAt(dto, newStatus);
        if (news.getNewsStatus() == NewsStatus.PROGRAMADO && newStatus == NewsStatus.BORRADOR) {news.setScheduledAt(null);
                } else {
                        news.setScheduledAt(newScheduledAt);
                }
                        news.setNewsStatus(newStatus);
                        news.syncPublished();
        }
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
            String careerName,       
            NewsCategory category,
            int page,
            int size,
            String sortBy,
            String sortType) {

        Integer careerId = null;
        if (careerName != null && !careerName.isBlank()) {
            careerId = careerRepository.findByNameUnaccented(careerName.trim())
                    .map(Career::getId)
                    .orElse(-1);
        }
                
        Map<String, String> fieldMap = Map.of(
                        "createdAt",  "created_at",
                        "updatedAt",  "updated_at",
                        "title",      "title",
                        "created_at", "created_at",  
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

        return result.map(n -> toDTO(n));  
  }


   private NewsStatus resolveStatus(NewsCreateDTO dto) {
        if (dto.getNewsStatus() != null) {
            return dto.getNewsStatus();
        }
        if (dto.getPublished() != null) {
            return dto.getPublished() ? NewsStatus.PUBLICADO : NewsStatus.BORRADOR;
        }
        return NewsStatus.PUBLICADO; // default
    }
 
    private OffsetDateTime resolveScheduledAt(NewsCreateDTO dto, NewsStatus status) {
        if (status != NewsStatus.PROGRAMADO) {
            return null;
        }
        if (dto.getScheduledAt() == null) {
            throw new InvalidScheduledDateException(
                    "Se requiere 'scheduledAt' cuando el estado es PROGRAMADO.");
        }
        OffsetDateTime minAllowed = OffsetDateTime.now().plusMinutes(MIN_SCHEDULED_MINUTES);
        if (dto.getScheduledAt().isBefore(minAllowed)) {
            throw new InvalidScheduledDateException(
                    "La fecha programada debe ser al menos " + MIN_SCHEDULED_MINUTES
                    + " minutos en el futuro.");
        }
        return dto.getScheduledAt();
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

        Set<Integer> careerIds = newsList.stream()
                .map(News::getCareerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> careerNames = careerRepository.findAllById(careerIds)
                .stream()
                .collect(Collectors.toMap(
                        Career::getId,
                        Career::getName
                ));

        return newsList.stream()
                .map(n -> toDTO(n, authorNames.getOrDefault(n.getAuthorId(), "Publicador"), careerNames.getOrDefault(n.getCareerId(), null)))
                .toList();
    }

    private NewsResponseDTO toDTO(News n, String authorName, String careerName) {
        return new NewsResponseDTO(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getCategory(),
                n.getCoverUrl(),
                n.getAttachmentUrl(),
                n.getCareerId(),
                careerName, 
                n.getAuthorId(),
                authorName,
                n.getPublished(),
                n.getNewsStatus(),       
                n.getScheduledAt(),
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
        String careerName = n.getCareerId() != null
                ? careerRepository.findById(n.getCareerId())
                        .map(Career::getName)
                        .orElse(null)
                : null;
        return toDTO(n, authorName, careerName);
    }
}