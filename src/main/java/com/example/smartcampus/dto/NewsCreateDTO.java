package com.example.smartcampus.dto;

import com.example.smartcampus.entity.NewsCategory;
import lombok.Data;

@Data
public class NewsCreateDTO {
    private String title;
    private String body;
    private NewsCategory category;
    private String coverUrl;
    private String attachmentUrl;
    private Integer careerId;
    private Boolean published;
}