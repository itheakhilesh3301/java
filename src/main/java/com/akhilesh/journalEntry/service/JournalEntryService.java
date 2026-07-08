package com.akhilesh.journalEntry.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.akhilesh.journalEntry.entity.JournalEntry;
import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.entity.Tag;
import com.akhilesh.journalEntry.repository.JournalEntryRepository;
import com.akhilesh.journalEntry.repository.TagRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    private final UserService userService;
    private final TagRepository tagRepository;


    JournalEntryService(JournalEntryRepository journalEntryRepository, UserService userService, TagRepository tagRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.userService = userService;
        this.tagRepository = tagRepository;
    }


    @Transactional
    public void saveJournalEntry(String username, JournalEntry journalEntry) {
        try {
            User user = userService.findByUserName(username);
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUser(user);

            if (journalEntry.getTags() != null) {
                List<Tag> processedTags = new ArrayList<>();
                for (Tag tag : journalEntry.getTags()) {
                    Tag existingTag = tagRepository.findByNameIgnoreCase(tag.getName()).orElse(null);
                    if (existingTag == null) {
                        existingTag = tagRepository.save(tag);
                    }
                    processedTags.add(existingTag);
                }
                journalEntry.setTags(processedTags);
            }

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
            if (entry.getTags() != null) {
                List<Tag> processedTags = new ArrayList<>();
                for (Tag tag : entry.getTags()) {
                    Tag existingTag = tagRepository.findByNameIgnoreCase(tag.getName()).orElse(null);
                    if (existingTag == null) {
                        existingTag = tagRepository.save(tag);
                    }
                    processedTags.add(existingTag);
                }
                entry.setTags(processedTags);
            }
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

    public Page<JournalEntry> getJournalEntriesForUser(String username, String keyword, int page, int size) {
        User user = userService.findByUserName(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            return journalEntryRepository.findByUserAndKeyword(user, keyword.trim(), pageable);
        } else {
            return journalEntryRepository.findByUser(user, pageable);
        }
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
