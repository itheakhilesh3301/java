package com.akhilesh.journalEntry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g. "CREATED_ADMIN", "DELETED_USER", "UPDATED_USER"

    @Column(nullable = false)
    private String adminUsername; // The admin who did it

    @Column(nullable = false)
    private String targetUsername; // The user affected

    private String details; // Any extra info

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
