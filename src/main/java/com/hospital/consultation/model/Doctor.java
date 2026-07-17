package com.hospital.consultation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String specialization;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    // Replaced MySQL POINT type with standard double columns for PostgreSQL compatibility
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}
