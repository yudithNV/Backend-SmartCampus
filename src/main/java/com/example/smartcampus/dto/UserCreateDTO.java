package com.example.smartcampus.dto;

import com.example.smartcampus.entity.Role;
import lombok.Data;

@Data
public class UserCreateDTO {

    private String fullName;
    private String email;
    private String password;
    private Role role;
    private Integer careerId;
}