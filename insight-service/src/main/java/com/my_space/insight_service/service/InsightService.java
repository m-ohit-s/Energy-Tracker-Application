package com.my_space.insight_service.service;

import com.my_space.insight_service.client.UsageClient;
import com.my_space.insight_service.dto.DeviceDto;
import com.my_space.insight_service.dto.InsightDto;
import com.my_space.insight_service.dto.UsageDto;
import com.my_space.insight_service.service.interfaces.IInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService implements IInsightService {

    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;

    @Override
    public InsightDto getSavingsTip(Long userId) {
        final UsageDto usageDto = usageClient.getXDaysUsageForUser(userId, 3);
        double totalUsage = usageDto.devices().stream().mapToDouble(DeviceDto::energyConsumed).sum();

        log.info("Calling Ollama for userId {} and totalUsage {}", userId, totalUsage);

        String prompt = new StringBuilder()
                .append("This is my total consumption over the past 3 days.")
                .append("How can I reduce energy consumption? How does it compare to average households?")
                .append("Total Energy Used: \n")
                .append(totalUsage)
                .toString();

        ChatResponse chatResponse = ollamaChatModel.call(
                Prompt.builder().content(prompt).build()
        );

        return InsightDto.builder()
                .userId(userId)
                .message(chatResponse.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }

    @Override
    public InsightDto getOverview(Long userId) {
        final UsageDto usageDto = usageClient.getXDaysUsageForUser(userId, 3);
        double totalUsage = usageDto.devices().stream().mapToDouble(DeviceDto::energyConsumed).sum();

        log.info("Calling Ollama for userId {} and totalUsage {}", userId, totalUsage);

        String prompt = new StringBuilder()
                .append("Analyse the following energy usage data and provide a concise" +
                        "overview with actionable insights.")
                .append("This data is aggregate data for the past 3 days")
                .append("Usage Data: \n")
                .append(usageDto.devices())
                .toString();

        ChatResponse chatResponse = ollamaChatModel.call(
                Prompt.builder().content(prompt).build()
        );

        return InsightDto.builder()
                .userId(userId)
                .message(chatResponse.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }
}
