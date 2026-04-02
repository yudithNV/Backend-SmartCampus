package com.example.smartcampus.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.dto.UserListDTO;
import com.example.smartcampus.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserWithResponse(dto));
    }

    @GetMapping
    // ✅ CAMBIO: Agregamos parámetros de búsqueda + filtros (todos opcionales)
    public ResponseEntity<Page<UserListDTO>> listAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(userService.listAllUsers(search, role, status, pageable));
    }
}