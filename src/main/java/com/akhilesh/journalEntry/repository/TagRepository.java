package com.akhilesh.journalEntry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.akhilesh.journalEntry.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);
}
