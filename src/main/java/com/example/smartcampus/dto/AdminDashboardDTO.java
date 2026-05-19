package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {

    private UserMetricsDTO users;
    private ComplaintMetricsDTO complaints;
    private AccessLogMetricsDTO accessLogs;
    private SuggestionMetricsDTO suggestions;
    private EventRegistrationMetricsDTO eventRegistrations;
    private EventMetricsDTO events;
    private NewsMetricsDTO news;
    private Long totalSuggestions;
    private Long publishedEvents;
    private Long totalEventRegistrations;
    private Long publishedNews;
}
