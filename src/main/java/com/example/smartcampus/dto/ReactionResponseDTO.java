package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionResponseDTO {
    private Long newsId;
    private ReactionType myReaction;   
    private Map<String, Long> counts;  
    private Long total;
}