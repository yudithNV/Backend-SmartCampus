package com.example.smartcampus.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserListDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private CareerInfo career;
    private String status;
    private String createdAt;

    @Data
    @AllArgsConstructor
    public static class CareerInfo {
        private Integer id;
        private String name;
        private String code;
    }
}
