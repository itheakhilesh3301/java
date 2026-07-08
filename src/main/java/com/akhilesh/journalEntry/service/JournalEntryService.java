package com.akhilesh.journalEntry.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.akhilesh.journalEntry.entity.JournalEntry;
import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.repository.JournalEntryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    private final UserService userService;


    JournalEntryService(JournalEntryRepository journalEntryRepository, UserService userService) {
        this.journalEntryRepository = journalEntryRepository;
        this.userService = userService;
    }


    @Transactional
    public void saveJournalEntry(String username, JournalEntry journalEntry) {
        try {
            User user = userService.findByUserName(username);
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUser(user);
            JournalEntry saved = journalEntryRepository.save(journalEntry);
            user.getJournalEntries().add(saved);
            userService.saveUser(user);
            log.info("Journal entry saved successfully");
        } catch (Exception e) {
            log.error("Failed to save journal entry: " + e.getMessage());
            throw new RuntimeException("An error occurred while saving the entry.", e);
        }

    }

    public void saveEntry(JournalEntry entry) {
        try {
            journalEntryRepository.save(entry);
            log.info("Journal entry saved successfully");
        } catch (Exception e) {
            log.error("Failed to save journal entry: " + e.getMessage());
            throw new RuntimeException("An error occurred while saving the entry.", e);
        }
    }

    public List<JournalEntry> getAllJournalEntries() {
        return journalEntryRepository.findAll();
    }

    @Transactional
    public boolean deleteById(Long id, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getJournalEntries().removeIf(x -> x.getId().equals(id));
            if (removed) {
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
            }
        } catch (Exception e) {
            log.error("Error ", e);
            throw new RuntimeException("An error occurred while deleting the entry.", e);
        }
        return removed;

    }

}
