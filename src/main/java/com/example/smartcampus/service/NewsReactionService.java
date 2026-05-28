package com.example.smartcampus.service;

import com.example.smartcampus.dto.ReactionResponseDTO;
import com.example.smartcampus.entity.NewsReaction;
import com.example.smartcampus.entity.ReactionType;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.NewsReactionRepository;
import com.example.smartcampus.repository.NewsRepository;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsReactionService {

    private final NewsReactionRepository reactionRepository;
    private final NewsRepository newsRepository;

  
    @Transactional
    public ReactionResponseDTO toggleReaction(Long newsId, ReactionType reactionType, User user) {
        if (!newsRepository.existsById(newsId)) {
            throw new NotFoundException("Noticia no encontrada con id: " + newsId);
        }

        Optional<NewsReaction> existing = reactionRepository.findByNewsIdAndUserId(newsId, user.getId());

        if (existing.isPresent()) {
            if (existing.get().getReactionType() == reactionType) {
                reactionRepository.deleteByNewsIdAndUserId(newsId, user.getId());
            } else {
                existing.get().setReactionType(reactionType);
                reactionRepository.save(existing.get());
            }
        } else {
            reactionRepository.save(NewsReaction.builder()
                    .newsId(newsId)
                    .userId(user.getId())
                    .reactionType(reactionType)
                    .build());
        }

        return buildReactionResponse(newsId, user);
    }

   
    @Transactional
    public ReactionResponseDTO removeReaction(Long newsId, User user) {
        reactionRepository.deleteByNewsIdAndUserId(newsId, user.getId());
        return buildReactionResponse(newsId, user);
    }

  
    @Transactional(readOnly = true)
    public ReactionResponseDTO getReactions(Long newsId, User user) {
        return buildReactionResponse(newsId, user);
    }

    private ReactionResponseDTO buildReactionResponse(Long newsId, User user) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("LIKE", 0L);
        counts.put("LOVE", 0L);
        counts.put("WOW",  0L);

        long total = 0L;
        for (Object[] row : reactionRepository.countGroupedByType(newsId)) {
            ReactionType type = (ReactionType) row[0];
            Long count = (Long) row[1];
            counts.put(type.name(), count);
            total += count;
        }

        ReactionType myReaction = reactionRepository
                .findByNewsIdAndUserId(newsId, user.getId())
                .map(NewsReaction::getReactionType)
                .orElse(null);

        return ReactionResponseDTO.builder()
                .newsId(newsId)
                .myReaction(myReaction)
                .counts(counts)
                .total(total)
                .build();
    }
}