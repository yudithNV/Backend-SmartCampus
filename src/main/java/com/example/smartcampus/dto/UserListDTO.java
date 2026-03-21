package com.example.smartcampus.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserListDTO {

    private UUID id;
    private String fullName;
    private String role;
    private String careerName;
    private String status;
    private String createdAt;
}
