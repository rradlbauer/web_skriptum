package com.clinicaldiary.controller;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import com.clinicaldiary.security.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepo;
    private final HealthRecordRepository recordRepo;
    private final DoctorPatientRepository doctorPatientRepo;
    private final DoctorRepository doctorRepo;
    private final RecordConfirmationRepository confirmationRepo;
    private final RecordIcd10Repository recordIcd10Repo;
    private final RecordAttachmentRepository attachmentRepo;

    private static final Path UPLOAD_DIR = Path.of("uploads");

    public PatientController(PatientRepository patientRepo, HealthRecordRepository recordRepo,
                             DoctorPatientRepository doctorPatientRepo, DoctorRepository doctorRepo,
                             RecordConfirmationRepository confirmationRepo,
                             RecordIcd10Repository recordIcd10Repo,
                             RecordAttachmentRepository attachmentRepo) {
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.doctorPatientRepo = doctorPatientRepo;
        this.doctorRepo = doctorRepo;
        this.confirmationRepo = confirmationRepo;
        this.recordIcd10Repo = recordIcd10Repo;
        this.attachmentRepo = attachmentRepo;
    }

    private Long getCurrentPatientId(Authentication auth) {
        return ((UserPrincipal) auth.getPrincipal()).getUserId();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication auth) {
        Patient p = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        return ResponseEntity.ok(Map.of("id", p.getId(), "email", p.getEmail(),
                "ssn", p.getSsn(), "firstName", p.getFirstName(),
                "lastName", p.getLastName(), "dateOfBirth", p.getDateOfBirth()));
    }

    @GetMapping("/me/records")
    public ResponseEntity<?> getRecords(Authentication auth) {
        Long patientId = getCurrentPatientId(auth);
        List<Map<String, Object>> result = recordRepo.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::enrichRecord).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/me/records")
    public ResponseEntity<?> createRecord(Authentication auth, @RequestBody Map<String, Object> body) {
        String now = LocalDateTime.now().toString();
        HealthRecord r = new HealthRecord();
        r.setPatientId(getCurrentPatientId(auth));
        r.setCategory((String) body.get("category"));
        r.setTitle((String) body.get("title"));
        r.setDescription((String) body.get("description"));
        r.setDateFrom((String) body.get("dateFrom"));
        r.setDateTo((String) body.get("dateTo"));
        r.setSeverity((String) body.get("severity"));
        r.setNotes((String) body.get("notes"));
        r.setMedicationName((String) body.get("medicationName"));
        r.setDosage((String) body.get("dosage"));
        r.setFrequency((String) body.get("frequency"));
        r.setPrescribingDoctor((String) body.get("prescribingDoctor"));
        r.setVaccineName((String) body.get("vaccineName"));
        r.setDoseNumber(body.get("doseNumber") != null ? Integer.valueOf(body.get("doseNumber").toString()) : null);
        r.setBatchNumber((String) body.get("batchNumber"));
        r.setInstitution((String) body.get("institution"));
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        recordRepo.save(r);
        return ResponseEntity.ok(enrichRecord(r));
    }

    @PutMapping("/me/records/{id}")
    public ResponseEntity<?> updateRecord(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        HealthRecord r = recordRepo.findById(id).orElseThrow();
        if (!r.getPatientId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecordId(id).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot edit confirmed record"));
        }
        r.setTitle((String) body.get("title"));
        r.setDescription((String) body.get("description"));
        r.setDateFrom((String) body.get("dateFrom"));
        r.setDateTo((String) body.get("dateTo"));
        r.setSeverity((String) body.get("severity"));
        r.setNotes((String) body.get("notes"));
        r.setMedicationName((String) body.get("medicationName"));
        r.setDosage((String) body.get("dosage"));
        r.setFrequency((String) body.get("frequency"));
        r.setPrescribingDoctor((String) body.get("prescribingDoctor"));
        r.setVaccineName((String) body.get("vaccineName"));
        r.setDoseNumber(body.get("doseNumber") != null ? Integer.valueOf(body.get("doseNumber").toString()) : null);
        r.setBatchNumber((String) body.get("batchNumber"));
        r.setInstitution((String) body.get("institution"));
        r.setUpdatedAt(LocalDateTime.now().toString());
        recordRepo.save(r);
        return ResponseEntity.ok(enrichRecord(r));
    }

    @DeleteMapping("/me/records/{id}")
    public ResponseEntity<?> deleteRecord(Authentication auth, @PathVariable Long id) {
        HealthRecord r = recordRepo.findById(id).orElseThrow();
        if (!r.getPatientId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecordId(id).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete confirmed record"));
        }
        recordRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Record deleted"));
    }

    @GetMapping("/me/doctors")
    public ResponseEntity<?> getAssignedDoctors(Authentication auth) {
        List<Long> doctorIds = doctorPatientRepo.findDoctorIdsByPatientId(getCurrentPatientId(auth));
        return ResponseEntity.ok(doctorRepo.findAllById(doctorIds).stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId()); m.put("firstName", d.getFirstName());
            m.put("lastName", d.getLastName()); m.put("specialization", d.getSpecialization());
            m.put("medicalLicenseNumber", d.getMedicalLicenseNumber()); m.put("email", d.getEmail());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/me/doctors/{doctorId}")
    public ResponseEntity<?> assignDoctor(Authentication auth, @PathVariable Long doctorId) {
        Long patientId = getCurrentPatientId(auth);
        if (!doctorRepo.findById(doctorId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
        }
        if (doctorPatientRepo.existsByDoctorIdAndPatientId(doctorId, patientId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already assigned"));
        }
        DoctorPatient dp = new DoctorPatient();
        dp.setDoctorId(doctorId);
        dp.setPatientId(patientId);
        dp.setAssignedAt(LocalDateTime.now().toString());
        doctorPatientRepo.save(dp);
        return ResponseEntity.ok(Map.of("message", "Doctor assigned"));
    }

    @DeleteMapping("/me/doctors/{doctorId}")
    public ResponseEntity<?> removeDoctor(Authentication auth, @PathVariable Long doctorId) {
        doctorPatientRepo.findByDoctorIdAndPatientId(doctorId, getCurrentPatientId(auth))
                .ifPresent(doctorPatientRepo::delete);
        return ResponseEntity.ok(Map.of("message", "Doctor removed"));
    }

    @GetMapping("/doctors/search")
    public ResponseEntity<?> searchDoctors(@RequestParam String q) {
        return ResponseEntity.ok(doctorRepo.search(q).stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId()); m.put("firstName", d.getFirstName());
            m.put("lastName", d.getLastName()); m.put("specialization", d.getSpecialization());
            m.put("medicalLicenseNumber", d.getMedicalLicenseNumber());
            return m;
        }).collect(Collectors.toList()));
    }

    // --- File Attachments ---

    @PostMapping("/me/records/{recordId}/attachments")
    public ResponseEntity<?> uploadAttachment(Authentication auth, @PathVariable Long recordId,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        if (!r.getPatientId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }

        String storedFilename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        Files.createDirectories(UPLOAD_DIR);
        file.transferTo(UPLOAD_DIR.resolve(storedFilename));

        RecordAttachment att = new RecordAttachment();
        att.setRecordId(recordId);
        att.setOriginalFilename(file.getOriginalFilename());
        att.setStoredFilename(storedFilename);
        att.setContentType(file.getContentType());
        att.setSize(file.getSize());
        att.setUploadedBy(getCurrentPatientId(auth));
        att.setUploadedAt(LocalDateTime.now().toString());
        attachmentRepo.save(att);

        return ResponseEntity.ok(Map.of(
                "id", att.getId(),
                "originalFilename", att.getOriginalFilename(),
                "contentType", att.getContentType(),
                "size", att.getSize(),
                "uploadedAt", att.getUploadedAt()
        ));
    }

    @GetMapping("/me/records/{recordId}/attachments")
    public ResponseEntity<?> listAttachments(Authentication auth, @PathVariable Long recordId) {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        if (!r.getPatientId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        return ResponseEntity.ok(attachmentRepo.findByRecordId(recordId).stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId()); m.put("originalFilename", a.getOriginalFilename());
            m.put("contentType", a.getContentType()); m.put("size", a.getSize());
            m.put("uploadedAt", a.getUploadedAt());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/me/records/{recordId}/attachments/{attachmentId}")
    public ResponseEntity<?> downloadAttachment(Authentication auth, @PathVariable Long recordId,
                                                @PathVariable Long attachmentId) throws IOException {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        Long patientId = getCurrentPatientId(auth);
        if (!r.getPatientId().equals(patientId) &&
            !doctorPatientRepo.findDoctorIdsByPatientId(patientId).isEmpty()) {
            // patient or assigned doctor can download
        } else if (!r.getPatientId().equals(patientId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }

        RecordAttachment att = attachmentRepo.findById(attachmentId).orElseThrow();
        if (!att.getRecordId().equals(recordId)) {
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

    @DeleteMapping("/me/records/{recordId}/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(Authentication auth, @PathVariable Long recordId,
                                              @PathVariable Long attachmentId) {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        if (!r.getPatientId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecordId(recordId).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete attachments of confirmed record"));
        }
        RecordAttachment att = attachmentRepo.findById(attachmentId).orElseThrow();
        try {
            Files.deleteIfExists(UPLOAD_DIR.resolve(att.getStoredFilename()));
        } catch (IOException ignored) {}
        attachmentRepo.deleteById(attachmentId);
        return ResponseEntity.ok(Map.of("message", "Attachment deleted"));
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private Map<String, Object> enrichRecord(HealthRecord r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId()); m.put("patientId", r.getPatientId());
        m.put("category", r.getCategory()); m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("dateFrom", r.getDateFrom()); m.put("dateTo", r.getDateTo());
        m.put("severity", r.getSeverity()); m.put("notes", r.getNotes());
        m.put("medicationName", r.getMedicationName()); m.put("dosage", r.getDosage());
        m.put("frequency", r.getFrequency()); m.put("prescribingDoctor", r.getPrescribingDoctor());
        m.put("vaccineName", r.getVaccineName()); m.put("doseNumber", r.getDoseNumber());
        m.put("batchNumber", r.getBatchNumber()); m.put("institution", r.getInstitution());
        m.put("createdAt", r.getCreatedAt()); m.put("updatedAt", r.getUpdatedAt());
        Optional<RecordConfirmation> c = confirmationRepo.findByRecordId(r.getId());
        m.put("confirmed", c.isPresent());
        c.ifPresent(conf -> { m.put("confirmedBy", conf.getDoctorName());
            m.put("confirmedAt", conf.getConfirmedAt()); m.put("confirmationComment", conf.getComment()); });
        m.put("icd10Codes", recordIcd10Repo.findByRecordId(r.getId()).stream().map(icd -> {
            Map<String, Object> im = new HashMap<>();
            im.put("id", icd.getId()); im.put("code", icd.getIcd10Code()); im.put("description", icd.getIcd10Description());
            return im;
        }).collect(Collectors.toList()));
        m.put("attachments", attachmentRepo.findByRecordId(r.getId()).stream().map(a -> {
            Map<String, Object> am = new HashMap<>();
            am.put("id", a.getId()); am.put("originalFilename", a.getOriginalFilename());
            am.put("contentType", a.getContentType()); am.put("size", a.getSize());
            am.put("uploadedAt", a.getUploadedAt());
            return am;
        }).collect(Collectors.toList()));
        return m;
    }
}
