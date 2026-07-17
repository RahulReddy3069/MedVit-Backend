package com.hospital.consultation.controller;

import com.hospital.consultation.model.AdminAuditLog;
import com.hospital.consultation.model.User;
import com.hospital.consultation.repository.AdminAuditLogRepository;
import com.hospital.consultation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    // 1. Fetch metadata-only registration grid (No Chat content, no symptom details)
    @GetMapping("/users-metadata")
    public ResponseEntity<List<Map<String, Object>>> getUsersMetadata(
            @RequestHeader("Authorization") String token,
            @RequestParam String adminId) {

        // Log audit event for checking records
        AdminAuditLog log = AdminAuditLog.builder()
                .id(UUID.randomUUID().toString())
                .adminUserId(adminId)
                .actionPerformed("Query Registration Metadata Grid")
                .ipAddress("127.0.0.1")
                .build();
        auditLogRepository.save(log);

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getId());
            map.put("email", u.getEmail());
            map.put("role", u.getRole());
            map.put("subscription", u.getSubscription());
            map.put("registeredAt", u.getCreatedAt());
            map.put("lastLoginTimestamp", u.getUpdatedAt()); // Metadata logs only
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    // 2. Legal / Dispute Export Flow (CSV Exporting metadata only - strictly isolates chats)
    @GetMapping("/audit-logs/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam String adminId) {

        // Log export trigger
        AdminAuditLog log = AdminAuditLog.builder()
                .id(UUID.randomUUID().toString())
                .adminUserId(adminId)
                .actionPerformed("Triggered Legal CSV Export Flow")
                .ipAddress("127.0.0.1")
                .build();
        auditLogRepository.save(log);

        List<AdminAuditLog> logs = auditLogRepository.findAll();
        
        // Assemble metadata CSV buffer (Strictly zero chats/symptom data)
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("LogID,AdminUserID,ActionPerformed,TargetSessionID,IPAddress,Timestamp\n");

        for (AdminAuditLog audit : logs) {
            csvBuilder.append(String.format("%s,%s,%s,%s,%s,%s\n",
                    audit.getId(),
                    audit.getAdminUserId(),
                    audit.getActionPerformed(),
                    audit.getTargetSessionId() != null ? audit.getTargetSessionId() : "NULL",
                    audit.getIpAddress(),
                    audit.getTimestamp().toString()
            ));
        }

        byte[] csvBytes = csvBuilder.toString().getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hms_legal_audit_metadata.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
