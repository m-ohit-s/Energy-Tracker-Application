package com.my_space.insight_service.client;

import com.my_space.insight_service.dto.UsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class UsageClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsageClient(@Value("${endpoint.usage-service-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        final String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();
        ResponseEntity<UsageDto> response = restTemplate.getForEntity(url, UsageDto.class);
        return response.getBody();
    }
}
