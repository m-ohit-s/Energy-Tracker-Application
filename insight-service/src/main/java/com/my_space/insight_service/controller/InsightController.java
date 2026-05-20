package com.my_space.insight_service.controller;

import com.my_space.insight_service.dto.InsightDto;
import com.my_space.insight_service.service.interfaces.IInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insight")
@RequiredArgsConstructor
public class InsightController {
    private final IInsightService insightService;

    @GetMapping("/saving-tips/{userId}")
    ResponseEntity<InsightDto> savingTips(@PathVariable Long userId) {
        final InsightDto insightDto = insightService.getSavingsTip(userId);
        return ResponseEntity.ok(insightDto);
    }

    @GetMapping("/overview/{userId}")
     ResponseEntity<InsightDto> getOverview(@PathVariable Long userId) {
        final InsightDto insightDto = insightService.getOverview(userId);
        return ResponseEntity.ok(insightDto);
    }
}
