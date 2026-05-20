package com.my_space.alert_service.service;

import com.my_space.kafka.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final EmailService emailService;

    @KafkaListener(topics = "energy-alert", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Received alerting event: {}", alertingEvent);
        final String subject = "Energy usage alert...";
        final String body = "Alert: " +
                "\nYou have reached your set threshold" +
                "\nCurrent values " +
                "\nThreshold: " + alertingEvent.threshold() + "" +
                "\nConsumption: "  + alertingEvent.energyConsumed();
        emailService.sendEmail(alertingEvent.email(), subject, body, alertingEvent.userId());
    }
}
