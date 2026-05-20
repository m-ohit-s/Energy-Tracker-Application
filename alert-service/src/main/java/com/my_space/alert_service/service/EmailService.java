package com.my_space.alert_service.service;

import com.my_space.alert_service.entity.Alert;
import com.my_space.alert_service.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

   private final JavaMailSender mailSender;
   private final AlertRepository alertRepository;

   public void sendEmail(String to, String subject, String body, Long userId) {
        log.info("Sending email to {}, subject {}, body {}", to, subject, body);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setFrom("noreply@myspace.com");
        mailMessage.setSubject(subject);
        mailMessage.setText(body);

        try {
            mailSender.send(mailMessage);
            final Alert alert = Alert.builder()
                    .sent(true)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            alertRepository.saveAndFlush(alert);
        } catch (MailException e) {
            log.error("Error sending email to {}, subject {}", to, subject, e);
            final Alert alert = Alert.builder()
                    .sent(false)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
            alertRepository.saveAndFlush(alert);
            return;
        }
        log.info("Email sent to {}, subject {}, body {}", to, subject, body);
   }
}
