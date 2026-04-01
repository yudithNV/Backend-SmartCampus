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
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

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

    // ── Todas las publicadas ──
    public List<NewsResponseDTO> getAllPublished() {
        return newsRepository.findAllByPublishedTrueOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }
 
    // ── Las del autor autenticado (todas, publicadas y borradores) ──
    public List<NewsResponseDTO> getNewsByAuthor(User author) {
        return newsRepository.findAllByAuthorIdOrderByCreatedAtDesc(author.getId())
                .stream().map(this::toDTO).toList();
    }

    public NewsResponseDTO updateNews(Long id, NewsCreateDTO dto, User author) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con id: " + id));

        if (!news.getAuthorId().equals(author.getId())) {
            throw new ForbiddenException("No tienes permiso para editar esta noticia");
        }

        if (dto.getTitle()         != null) news.setTitle(dto.getTitle());
        if (dto.getBody()          != null) news.setBody(dto.getBody());
        if (dto.getCategory()      != null) news.setCategory(dto.getCategory());
        if (dto.getCoverUrl()      != null) news.setCoverUrl(dto.getCoverUrl());
        if (dto.getAttachmentUrl() != null) news.setAttachmentUrl(dto.getAttachmentUrl());
        if (dto.getCareerId()      != null) news.setCareerId(dto.getCareerId());
        if (dto.getPublished()     != null) news.setPublished(dto.getPublished());
        news.setUpdatedAt(OffsetDateTime.now());

        return toDTO(newsRepository.save(news));
    }

    private NewsResponseDTO toDTO(News n) {
        String authorName = "Publicador";
        var userOpt = userRepository.findById(n.getAuthorId());
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            authorName = u.getFullName() != null ? u.getFullName() : u.getEmail();
        }

        return new NewsResponseDTO(
                n.getId(), n.getTitle(), n.getBody(),
                n.getCategory(), n.getCoverUrl(), n.getAttachmentUrl(),
                n.getCareerId(), n.getAuthorId(),
                authorName,  // ← campo nuevo
                n.getPublished(),
                n.getCreatedAt(), n.getUpdatedAt()
        );
    }
    


    public NewsResponseDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada"));
        return toDTO(news);
    }


    public void deleteNews(Long id, User requester) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con id: " + id));

        // SCRUM-323: solo el autor puede eliminar su propia noticia
        if (!news.getAuthorId().equals(requester.getId())) {
            throw new ForbiddenException("No tienes permiso para eliminar esta noticia");
        }

        newsRepository.deleteById(id);
    }
}