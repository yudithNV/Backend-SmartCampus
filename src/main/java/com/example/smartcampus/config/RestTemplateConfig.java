package com.example.smartcampus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Registra RestTemplate como bean de Spring para que ChatbotService
 * pueda inyectarlo y hacer llamadas HTTP a la API de OpenAI.
 *
 * Se coloca en un archivo propio para no contaminar SecurityConfig.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}