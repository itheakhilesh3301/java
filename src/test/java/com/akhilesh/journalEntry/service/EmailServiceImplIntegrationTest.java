package com.akhilesh.journalEntry.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceImplIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testSendRealEmail() {
        // Replace with the email address you want to receive the test email
        String to = "chekareakhilesh3301@gmail.com"; 
        String subject = "Live Test from Spring Boot!";
        String body = "Hello Akhilesh,\n\nIf you are reading this, your Spring Boot email integration is working perfectly with your real credentials!\n\nAwesome job!";

        // This will attempt to use your real SMTP credentials from application.yml
        emailService.sendEmail(to, subject, body);
        
        System.out.println("Check your inbox at " + to + "!");
    }
}
