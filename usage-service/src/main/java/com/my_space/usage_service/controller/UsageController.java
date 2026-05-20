package com.my_space.usage_service.controller;

import com.my_space.usage_service.dto.UsageDto;
import com.my_space.usage_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/{userId}")
    ResponseEntity<UsageDto> getUserDeviceUsage(@PathVariable Long userId, @RequestParam(defaultValue = "3") int days) {
        final UsageDto usageDto = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usageDto);
    }
}
