package com.example.smartcampus.scheduler;

import com.example.smartcampus.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class NewsSchedulerJob {

    private final NewsRepository newsRepository;

   
    @Scheduled(cron = "0 * * * * *")   // cada minuto, al segundo 0
    @Transactional
    public void publishDueNews() {
        OffsetDateTime now = OffsetDateTime.now();
        int published = newsRepository.publishDueNews(now);

        if (published > 0) {
            log.info("[NewsScheduler] {} noticia(s) publicada(s) automáticamente a las {}",
                    published, now);
        }
    }
}