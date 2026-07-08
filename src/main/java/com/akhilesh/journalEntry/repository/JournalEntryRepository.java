package com.akhilesh.journalEntry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.akhilesh.journalEntry.entity.JournalEntry;
import com.akhilesh.journalEntry.entity.User;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {    
    
    Page<JournalEntry> findByUser(User user, Pageable pageable);
    
    @Query("SELECT j FROM JournalEntry j WHERE j.user = :user AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JournalEntry> findByUserAndKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
}
