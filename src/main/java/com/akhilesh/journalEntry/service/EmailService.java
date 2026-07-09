package com.akhilesh.journalEntry.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
