package com.example.smartcampus.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String fullName;
    private String email;
    private String role;
    private String status;
    private Integer careerId;
}