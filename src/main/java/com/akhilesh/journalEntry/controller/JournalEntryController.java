package com.akhilesh.journalEntry.controller;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RestController;

import com.akhilesh.journalEntry.entity.JournalEntry;
import com.akhilesh.journalEntry.entity.User;
import com.akhilesh.journalEntry.service.JournalEntryService;
import com.akhilesh.journalEntry.service.UserService;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    private final UserService userService;

    JournalEntryController(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }

    


    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<JournalEntry> all = user.getJournalEntries();
        if (all != null && !all.isEmpty()) {
            return ResponseEntity.ok(all);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            journalEntryService.saveJournalEntry(userName, myEntry);
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
            return ResponseEntity.ok("Entry deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> updateJournalById(@PathVariable Long id, @RequestBody JournalEntry newEntry) {
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
            journalEntryService.saveEntry(old);
            return ResponseEntity.ok(old);
        }
        return ResponseEntity.notFound().build();
    }
}
