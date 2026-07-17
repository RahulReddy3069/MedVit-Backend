package com.hospital.consultation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "admin_user_id", nullable = false)
    private String adminUserId;

    @Column(name = "action_performed", nullable = false)
    private String actionPerformed;

    @Column(name = "target_session_id")
    private String targetSessionId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
