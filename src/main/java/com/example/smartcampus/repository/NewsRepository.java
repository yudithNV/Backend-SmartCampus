package com.example.smartcampus.repository;

import com.example.smartcampus.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, Long> { 
}