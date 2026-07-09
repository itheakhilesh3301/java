package com.akhilesh.journalEntry.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            // Default "from" can be set in application properties, but we can also set it if needed.
            // mail.setFrom("your-email@gmail.com");
            
            javaMailSender.send(mail);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Error while sending email to {}: {}", to, e.getMessage());
        }
    }
}
