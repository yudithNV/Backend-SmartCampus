package com.example.smartcampus.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String fullName;
    private String phone;
    private String bio;
    private String avatarUrl;
}