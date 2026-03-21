package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreateResponseDTO {

    private String message;
    private UserRegisterDTO user;

    @Data
    @AllArgsConstructor
    public static class UserRegisterDTO {
        private String id;
        private String fullName;
        private String email;
        private String role;
        private String status;
        private String createdAt;
    }
}
