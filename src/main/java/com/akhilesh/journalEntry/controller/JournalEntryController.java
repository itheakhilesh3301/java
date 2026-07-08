package com.akhilesh.journalEntry.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.akhilesh.journalEntry.entity.JournalEntry;
import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.entity.AuditLog;
import com.akhilesh.journalEntry.service.JournalEntryService;
import com.akhilesh.journalEntry.service.UserService;
import com.akhilesh.journalEntry.repository.AuditLogRepository;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    private final UserService userService;

    private final AuditLogRepository auditLogRepository;

    JournalEntryController(JournalEntryService journalEntryService, UserService userService, AuditLogRepository auditLogRepository) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
    }

    private void logAction(String action, String username, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .adminUsername(username)
                .targetUsername(username)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    


    @GetMapping
    public ResponseEntity<Page<JournalEntry>> getAllJournalEntriesOfUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
        Page<JournalEntry> entriesPage = journalEntryService.getJournalEntriesForUser(userName, keyword, page, size);
        return ResponseEntity.ok(entriesPage);
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@Valid @RequestBody JournalEntry myEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            journalEntryService.saveJournalEntry(userName, myEntry);
            logAction("CREATED_JOURNAL", userName, "Created a new journal entry: " + myEntry.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(myEntry);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        
        Optional<JournalEntry> journalEntry = user.getJournalEntries().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst();

        if (journalEntry.isPresent()) {
            return ResponseEntity.ok(journalEntry.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJournalEntryById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        boolean removed = journalEntryService.deleteById(id, userName);
        if (removed) {
            logAction("DELETED_JOURNAL", userName, "Deleted journal entry ID: " + id);
            return ResponseEntity.ok("Entry deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> updateJournalById(@PathVariable Long id, @Valid @RequestBody JournalEntry newEntry) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        
        Optional<JournalEntry> optionalEntry = user.getJournalEntries().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst();

        if (optionalEntry.isPresent()) {
            JournalEntry old = optionalEntry.get();
            if (newEntry.getTitle() != null && !newEntry.getTitle().equals("")) {
                old.setTitle(newEntry.getTitle());
            }
            if (newEntry.getContent() != null && !newEntry.getContent().equals("")) {
                old.setContent(newEntry.getContent());
            }
            // Tags updating
            if (newEntry.getTags() != null) {
                old.setTags(newEntry.getTags());
            }

            journalEntryService.saveEntry(old);
            logAction("UPDATED_JOURNAL", userName, "Updated journal entry ID: " + id);
            return ResponseEntity.ok(old);
        }
        return ResponseEntity.notFound().build();
    }
}
