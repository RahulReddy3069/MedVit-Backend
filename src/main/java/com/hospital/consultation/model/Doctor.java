package com.hospital.consultation.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

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

    @Column(columnDefinition = "POINT", nullable = false)
    private Point location; // JTS Spatial Coordinate representing latitude & longitude

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // Helper method to retrieve standard latitude coordinate
    public double getLatitude() {
        return location != null ? location.getX() : 0.0;
    }

    // Helper method to retrieve standard longitude coordinate
    public double getLongitude() {
        return location != null ? location.getY() : 0.0;
    }
}
