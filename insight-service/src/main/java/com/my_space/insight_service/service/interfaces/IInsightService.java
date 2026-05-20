package com.my_space.insight_service.service.interfaces;

import com.my_space.insight_service.dto.InsightDto;

public interface IInsightService {
    InsightDto getSavingsTip(Long userId);

    InsightDto getOverview(Long userId);
}
