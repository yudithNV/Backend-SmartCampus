package com.example.smartcampus.service;

import com.example.smartcampus.dto.CareerDTO;
import com.example.smartcampus.entity.Career;
import com.example.smartcampus.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerRepository careerRepository;

    public List<CareerDTO> getAllCareers() {
        return careerRepository.findAll().stream()
            .map(career -> new CareerDTO(
                career.getId(),
                career.getName(),
                career.getCode()
            ))
            .toList();
    }

    public Optional<String> getCareerNameById(Integer careerId) {
        return careerRepository.findById(careerId)
            .map(Career::getName);
    }

    public Optional<Career> getCareerById(Integer careerId) {
        return careerRepository.findById(careerId);
    }
}
