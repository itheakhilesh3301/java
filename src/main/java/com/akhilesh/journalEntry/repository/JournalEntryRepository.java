package com.akhilesh.journalEntry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akhilesh.journalEntry.entity.JournalEntry;

public interface JournalEntryRepository extends JpaRepository<JournalEntry,Long>{    
    
}
