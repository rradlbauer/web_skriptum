package com.clinicaldiary.controller;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import com.clinicaldiary.security.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final HealthRecordRepository recordRepo;
    private final RecordConfirmationRepository confirmationRepo;
    private final RecordIcd10Repository recordIcd10Repo;
    private final Icd10CodeRepository icd10CodeRepo;
    private final RecordAttachmentRepository attachmentRepo;

    private static final Path UPLOAD_DIR = Path.of("uploads");

    public DoctorController(DoctorRepository doctorRepo, PatientRepository patientRepo,
                            HealthRecordRepository recordRepo,
                            RecordConfirmationRepository confirmationRepo,
                            RecordIcd10Repository recordIcd10Repo, Icd10CodeRepository icd10CodeRepo,
                            RecordAttachmentRepository attachmentRepo) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.confirmationRepo = confirmationRepo;
        this.recordIcd10Repo = recordIcd10Repo;
        this.icd10CodeRepo = icd10CodeRepo;
        this.attachmentRepo = attachmentRepo;
    }

    private Long getCurrentDoctorId(Authentication auth) {
        return ((UserPrincipal) auth.getPrincipal()).getUserId();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication auth) {
        Doctor d = doctorRepo.findById(getCurrentDoctorId(auth)).orElseThrow();
        return ResponseEntity.ok(Map.of("id", d.getId(), "email", d.getEmail(),
                "medicalLicenseNumber", d.getMedicalLicenseNumber(),
                "firstName", d.getFirstName(), "lastName", d.getLastName(),
                "specialization", d.getSpecialization()));
    }

    @GetMapping("/me/patients")
    public ResponseEntity<?> getPatients(Authentication auth) {
        Doctor doctor = doctorRepo.findById(getCurrentDoctorId(auth)).orElseThrow();
        return ResponseEntity.ok(doctor.getPatients().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId()); m.put("email", p.getEmail()); m.put("ssn", p.getSsn());
            m.put("firstName", p.getFirstName()); m.put("lastName", p.getLastName());
            m.put("dateOfBirth", p.getDateOfBirth());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/me/patients/{patientId}/records")
    public ResponseEntity<?> getPatientRecords(Authentication auth, @PathVariable Long patientId) {
        Doctor doctor = doctorRepo.findById(getCurrentDoctorId(auth)).orElseThrow();
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        if (!doctor.getPatients().contains(patient)) {
            return ResponseEntity.status(403).body(Map.of("error", "Patient not assigned to you"));
        }
        return ResponseEntity.ok(recordRepo.findByPatientOrderByCreatedAtDesc(patient)
                .stream().map(this::enrichRecord).collect(Collectors.toList()));
    }

    @PostMapping("/records/{recordId}/confirm")
    public ResponseEntity<?> confirmRecord(Authentication auth, @PathVariable Long recordId,
                                           @RequestBody Map<String, String> body) {
        Long doctorId = getCurrentDoctorId(auth);
        HealthRecord record = recordRepo.findById(recordId).orElseThrow();
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        if (!doctor.getPatients().contains(record.getPatient())) {
            return ResponseEntity.status(403).body(Map.of("error", "Patient not assigned to you"));
        }
        if (confirmationRepo.findByRecord(record).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Record already confirmed"));
        }
        RecordConfirmation conf = new RecordConfirmation();
        conf.setRecord(record);
        conf.setDoctor(doctor);
        conf.setComment(body.get("comment"));
        conf.setConfirmedAt(LocalDateTime.now());
        confirmationRepo.save(conf);
        return ResponseEntity.ok(Map.of("message", "Record confirmed",
                "confirmedBy", doctor.getFirstName() + " " + doctor.getLastName(),
                "confirmedAt", conf.getConfirmedAt()));
    }

    @PostMapping("/records/{recordId}/icd10")
    public ResponseEntity<?> assignIcd10(Authentication auth, @PathVariable Long recordId,
                                         @RequestBody Map<String, Long> body) {
        Long doctorId = getCurrentDoctorId(auth);
        HealthRecord record = recordRepo.findById(recordId).orElseThrow();
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();
        if (!doctor.getPatients().contains(record.getPatient())) {
            return ResponseEntity.status(403).body(Map.of("error", "Patient not assigned to you"));
        }
        Icd10Code icd = icd10CodeRepo.findById(body.get("icd10CodeId")).orElseThrow();
        RecordIcd10 ri = new RecordIcd10();
        ri.setRecord(record);
        ri.setIcd10Code(icd);
        ri.setDoctor(doctor);
        ri.setAssignedAt(LocalDateTime.now());
        recordIcd10Repo.save(ri);
        return ResponseEntity.ok(Map.of("id", ri.getId(), "code", icd.getCode(),
                "description", icd.getDescription()));
    }

    @DeleteMapping("/records/{recordId}/icd10/{icd10Id}")
    public ResponseEntity<?> removeIcd10(@PathVariable Long recordId, @PathVariable Long icd10Id) {
        recordIcd10Repo.deleteById(icd10Id);
        return ResponseEntity.ok(Map.of("message", "ICD-10 code removed"));
    }

    @GetMapping("/records/{recordId}/attachments/{attachmentId}")
    public ResponseEntity<?> downloadAttachment(Authentication auth, @PathVariable Long recordId,
                                                @PathVariable Long attachmentId) throws IOException {
        HealthRecord record = recordRepo.findById(recordId).orElseThrow();
        Doctor doctor = doctorRepo.findById(getCurrentDoctorId(auth)).orElseThrow();
        if (!doctor.getPatients().contains(record.getPatient())) {
            return ResponseEntity.status(403).body(Map.of("error", "Patient not assigned to you"));
        }
        RecordAttachment att = attachmentRepo.findById(attachmentId).orElseThrow();
        if (!att.getRecord().getId().equals(recordId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Attachment not found"));
        }
        Path filePath = UPLOAD_DIR.resolve(att.getStoredFilename());
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(404).body(Map.of("error", "File not found on disk"));
        }
        byte[] content = Files.readAllBytes(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(att.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + att.getOriginalFilename() + "\"")
                .body(content);
    }

    @GetMapping("/icd10/search")
    public ResponseEntity<?> searchIcd10(@RequestParam String q) {
        return ResponseEntity.ok(icd10CodeRepo.search(q).stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId()); m.put("code", c.getCode()); m.put("description", c.getDescription());
            return m;
        }).collect(Collectors.toList()));
    }

    private Map<String, Object> enrichRecord(HealthRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("patientId", r.getPatient().getId());
        m.put("category", r.getClass().getAnnotation(jakarta.persistence.DiscriminatorValue.class).value());
        m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("dateFrom", r.getDateFrom());
        m.put("dateTo", r.getDateTo());

        if (r instanceof IllnessRecord ir) {
            m.put("severity", ir.getSeverity());
            m.put("notes", ir.getNotes());
        } else if (r instanceof VaccinationRecord vr) {
            m.put("vaccineName", vr.getVaccineName());
            m.put("doseNumber", vr.getDoseNumber());
            m.put("batchNumber", vr.getBatchNumber());
            m.put("institution", vr.getInstitution());
        } else if (r instanceof MedicationRecord mr) {
            m.put("medicationName", mr.getMedicationName());
            m.put("dosage", mr.getDosage());
            m.put("frequency", mr.getFrequency());
            m.put("prescribingDoctor", mr.getPrescribingDoctor());
        }

        m.put("createdAt", r.getCreatedAt());
        m.put("updatedAt", r.getUpdatedAt());

        Optional<RecordConfirmation> c = confirmationRepo.findByRecord(r);
        m.put("confirmed", c.isPresent());
        c.ifPresent(conf -> {
            m.put("confirmedBy", conf.getDoctor().getFirstName() + " " + conf.getDoctor().getLastName());
            m.put("confirmedAt", conf.getConfirmedAt());
            m.put("confirmationComment", conf.getComment());
        });

        m.put("icd10Codes", recordIcd10Repo.findByRecord(r).stream().map(icd -> {
            Map<String, Object> im = new HashMap<>();
            im.put("id", icd.getId());
            im.put("code", icd.getIcd10Code().getCode());
            im.put("description", icd.getIcd10Code().getDescription());
            return im;
        }).collect(Collectors.toList()));

        m.put("attachments", attachmentRepo.findByRecord(r).stream().map(a -> {
            Map<String, Object> am = new HashMap<>();
            am.put("id", a.getId()); am.put("originalFilename", a.getOriginalFilename());
            am.put("contentType", a.getContentType()); am.put("size", a.getSize());
            am.put("uploadedAt", a.getUploadedAt());
            return am;
        }).collect(Collectors.toList()));
        return m;
    }
}
