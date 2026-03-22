package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String eventDate;
    private String location;
    private String imageUrl;
    private String category;
    private CareerInfo career;
    private String createdBy;
    private String createdAt;
    private String updatedAt;

    @Data
    @AllArgsConstructor
    public static class CareerInfo {
        private Integer id;
        private String name;
        private String code;
    }
}
