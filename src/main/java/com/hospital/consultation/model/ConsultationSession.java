package com.hospital.consultation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSession {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "doctor_id", nullable = false)
    private String doctorId;

    @Column(nullable = false)
    private String status; // 'Pending', 'Active', 'Rejected', 'Expired', 'Completed'

    @Column(name = "symptom_description", nullable = false)
    private String symptomDescription;

    @Column(name = "request_timestamp")
    private LocalDateTime requestTimestamp = LocalDateTime.now();

    @Column(name = "accepted_timestamp")
    private LocalDateTime acceptedTimestamp;

    @Column(name = "timer_expires_at", nullable = false)
    private LocalDateTime timerExpiresAt; // Calculated as requestTimestamp + 5 mins

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        this.requestTimestamp = LocalDateTime.now();
        if (this.timerExpiresAt == null) {
            this.timerExpiresAt = this.requestTimestamp.plusMinutes(5);
        }
    }
}
