package com.hospital.consultation.repository;

import com.hospital.consultation.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    // MySQL 8.0+ native query implementing proximity checks via ST_Distance_Sphere and SPATIAL Indexing
    @Query(value = "SELECT d.* FROM doctors d " +
                   "WHERE ST_Distance_Sphere(d.location, ST_GeomFromText(:pointText, 4326)) <= :radiusMeters " +
                   "AND (:specialization IS NULL OR d.specialization = :specialization) " +
                   "AND d.is_available = TRUE " +
                   "ORDER BY ST_Distance_Sphere(d.location, ST_GeomFromText(:pointText, 4326)) ASC", 
           nativeQuery = true)
    List<Doctor> findDoctorsWithinRadius(
        @Param("pointText") String pointText, // Format: 'POINT(lat lng)'
        @Param("radiusMeters") double radiusMeters, 
        @Param("specialization") String specialization
    );
}
