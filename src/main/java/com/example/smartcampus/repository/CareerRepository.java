package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Career;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerRepository extends JpaRepository<Career, Integer> {
    Optional<Career> findByNameIgnoreCase(String name);

    @Query(value = """
        SELECT * FROM careers
        WHERE unaccent(LOWER(name)) = unaccent(LOWER(:name))
        LIMIT 1
        """, nativeQuery = true)
    Optional<Career> findByNameUnaccented(@Param("name") String name);
}
