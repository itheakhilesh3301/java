package com.akhilesh.journalEntry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.akhilesh.journalEntry.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
}
