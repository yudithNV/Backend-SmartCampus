package com.example.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "careers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
