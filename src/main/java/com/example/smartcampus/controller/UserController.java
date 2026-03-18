package com.example.smartcampus.controller;

import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody UserCreateDTO dto){
        return userService.createUser(dto);
    }
}