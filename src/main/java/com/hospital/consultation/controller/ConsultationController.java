package com.hospital.consultation.controller;

import com.hospital.consultation.model.ConsultationSession;
import com.hospital.consultation.model.Doctor;
import com.hospital.consultation.model.User;
import com.hospital.consultation.repository.ConsultationSessionRepository;
import com.hospital.consultation.repository.DoctorRepository;
import com.hospital.consultation.repository.UserRepository;
import com.hospital.consultation.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ConsultationController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ConsultationSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Proximity-based Doctor Search (with distance calculations via MySQL spatial triggers)
    @GetMapping("/doctors/search")
    public ResponseEntity<List<Map<String, Object>>> searchDoctors(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusKm,
            @RequestParam(required = false) String specialty,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Format point text for MySQL SPATIAL input: POINT(lat lng)
        String pointText = String.format(Locale.US, "POINT(%.6f %.6f)", lat, lng);
        double radiusMeters = radiusKm * 1000.0;

        List<Doctor> nearbyDocs = doctorRepository.findDoctorsWithinRadius(pointText, radiusMeters, specialty);
        List<Map<String, Object>> response = new ArrayList<>();

        // standard mock user check for subscription tier checks
        String subscription = "Standard";
        if (authHeader != null && authHeader.contains("Premium")) {
            subscription = "Premium";
        }

        for (Doctor doc : nearbyDocs) {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", doc.getId());
            docMap.put("fullName", doc.getFullName());
            docMap.put("specialization", doc.getSpecialization());
            docMap.put("latitude", doc.getLatitude());
            docMap.put("longitude", doc.getLongitude());
            
            // Mask phone numbers if user is Standard tier
            if ("Premium".equalsIgnoreCase(subscription)) {
                docMap.put("phoneNumber", doc.getPhoneNumber());
                docMap.put("premiumUnlocked", true);
            } else {
                docMap.put("phoneNumber", "★-★★★-★★★-" + doc.getPhoneNumber().substring(doc.getPhoneNumber().length() - 4));
                docMap.put("premiumUnlocked", false);
            }
            response.add(docMap);
        }

        return ResponseEntity.ok(response);
    }

    // 2. Submit Intake Request with Media Attachments
    @PostMapping("/consultations/request")
    public ResponseEntity<?> submitRequest(
            @RequestParam String patientId,
            @RequestParam String doctorId,
            @RequestParam String symptoms,
            @RequestParam(value = "attachments", required = false) MultipartFile[] files) {

        Optional<User> patient = userRepository.findById(patientId);
        if (patient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Patient user details not found.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5); // Strict 5-minute guardrail

        ConsultationSession session = ConsultationSession.builder()
                .id(UUID.randomUUID().toString())
                .patientId(patientId)
                .doctorId(doctorId)
                .symptomDescription(symptoms)
                .status("Pending")
                .requestTimestamp(now)
                .timerExpiresAt(expiresAt)
                .build();

        sessionRepository.save(session);

        // Process attachments (Zero-Knowledge placeholder, URLs encrypted)
        List<String> mockAttachmentUrls = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                // Simulate saving pre-encrypted S3 keys
                mockAttachmentUrls.add("https://s3.amazonaws.com/secured-clinical-bucket/" + UUID.randomUUID() + "_enc");
            }
        }

        // Dispatch SSE/Websocket event to the specific doctor's session handler
        String eventPayload = String.format(
                "{\"sessionId\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"symptoms\":\"%s\",\"expiresAt\":\"%s\",\"attachmentsCount\":%d}",
                session.getId(), patientId, patient.get().getEmail().split("@")[0], symptoms, expiresAt.toString(), mockAttachmentUrls.size()
        );
        
        WebSocketConfig.notifyDoctorOfNewRequest(doctorId, eventPayload);

        return ResponseEntity.ok(session);
    }

    // 3. Matchmaking Accept State Transition
    @PostMapping("/consultations/{sessionId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable String sessionId) {
        Optional<ConsultationSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ConsultationSession session = sessionOpt.get();
        if (LocalDateTime.now().isAfter(session.getTimerExpiresAt())) {
            session.setStatus("Expired");
            sessionRepository.save(session);
            return ResponseEntity.status(HttpStatus.GONE).body("Request timer expired. Matchmaking failed.");
        }

        session.setStatus("Active");
        session.setAcceptedTimestamp(LocalDateTime.now());
        sessionRepository.save(session);

        return ResponseEntity.ok(session);
    }

    // 4. Matchmaking Reject/Cancel Transition
    @PostMapping("/consultations/{sessionId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable String sessionId) {
        Optional<ConsultationSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ConsultationSession session = sessionOpt.get();
        session.setStatus("Rejected");
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return ResponseEntity.ok(session);
    }
}
