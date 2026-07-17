package com.hospital.consultation.repository;

import com.hospital.consultation.model.ConsultationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultationSessionRepository extends JpaRepository<ConsultationSession, String> {
    List<ConsultationSession> findByDoctorIdAndStatus(String doctorId, String status);
    List<ConsultationSession> findByPatientId(String patientId);
    List<ConsultationSession> findByStatus(String status);
}
