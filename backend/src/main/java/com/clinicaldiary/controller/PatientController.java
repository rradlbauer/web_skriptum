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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepo;
    private final HealthRecordRepository recordRepo;
    private final DoctorRepository doctorRepo;
    private final RecordConfirmationRepository confirmationRepo;
    private final RecordIcd10Repository recordIcd10Repo;
    private final RecordAttachmentRepository attachmentRepo;

    private static final Path UPLOAD_DIR = Path.of("uploads");

    public PatientController(PatientRepository patientRepo, HealthRecordRepository recordRepo,
                             DoctorRepository doctorRepo,
                             RecordConfirmationRepository confirmationRepo,
                             RecordIcd10Repository recordIcd10Repo,
                             RecordAttachmentRepository attachmentRepo) {
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
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
        Patient patient = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        List<Map<String, Object>> result = recordRepo.findByPatientOrderByCreatedAtDesc(patient)
                .stream().map(this::enrichRecord).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/me/records")
    public ResponseEntity<?> createRecord(Authentication auth, @RequestBody Map<String, Object> body) {
        Patient patient = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        String category = (String) body.get("category");
        HealthRecord r;
        switch (category) {
            case "ILLNESS":
                IllnessRecord ir = new IllnessRecord();
                ir.setSeverity((String) body.get("severity"));
                ir.setNotes((String) body.get("notes"));
                r = ir;
                break;
            case "VACCINATION":
                VaccinationRecord vr = new VaccinationRecord();
                vr.setVaccineName((String) body.get("vaccineName"));
                vr.setDoseNumber(body.get("doseNumber") != null ? Integer.valueOf(body.get("doseNumber").toString()) : null);
                vr.setBatchNumber((String) body.get("batchNumber"));
                vr.setInstitution((String) body.get("institution"));
                r = vr;
                break;
            case "MEDICATION":
                MedicationRecord mr = new MedicationRecord();
                mr.setMedicationName((String) body.get("medicationName"));
                mr.setDosage((String) body.get("dosage"));
                mr.setFrequency((String) body.get("frequency"));
                mr.setPrescribingDoctor((String) body.get("prescribingDoctor"));
                r = mr;
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown category: " + category));
        }
        r.setPatient(patient);
        r.setTitle((String) body.get("title"));
        r.setDescription((String) body.get("description"));
        r.setDateFrom(body.get("dateFrom") != null ? LocalDate.parse((String) body.get("dateFrom")) : null);
        r.setDateTo(body.get("dateTo") != null ? LocalDate.parse((String) body.get("dateTo")) : null);
        LocalDateTime now = LocalDateTime.now();
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        recordRepo.save(r);
        return ResponseEntity.ok(enrichRecord(r));
    }

    @PutMapping("/me/records/{id}")
    public ResponseEntity<?> updateRecord(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        HealthRecord r = recordRepo.findById(id).orElseThrow();
        if (!r.getPatient().getId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecord(r).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot edit confirmed record"));
        }
        r.setTitle((String) body.get("title"));
        r.setDescription((String) body.get("description"));
        r.setDateFrom(body.get("dateFrom") != null ? LocalDate.parse((String) body.get("dateFrom")) : null);
        r.setDateTo(body.get("dateTo") != null ? LocalDate.parse((String) body.get("dateTo")) : null);
        if (r instanceof IllnessRecord ir) {
            ir.setSeverity((String) body.get("severity"));
            ir.setNotes((String) body.get("notes"));
        } else if (r instanceof VaccinationRecord vr) {
            vr.setVaccineName((String) body.get("vaccineName"));
            vr.setDoseNumber(body.get("doseNumber") != null ? Integer.valueOf(body.get("doseNumber").toString()) : null);
            vr.setBatchNumber((String) body.get("batchNumber"));
            vr.setInstitution((String) body.get("institution"));
        } else if (r instanceof MedicationRecord mr) {
            mr.setMedicationName((String) body.get("medicationName"));
            mr.setDosage((String) body.get("dosage"));
            mr.setFrequency((String) body.get("frequency"));
            mr.setPrescribingDoctor((String) body.get("prescribingDoctor"));
        }
        r.setUpdatedAt(LocalDateTime.now());
        recordRepo.save(r);
        return ResponseEntity.ok(enrichRecord(r));
    }

    @DeleteMapping("/me/records/{id}")
    public ResponseEntity<?> deleteRecord(Authentication auth, @PathVariable Long id) {
        HealthRecord r = recordRepo.findById(id).orElseThrow();
        if (!r.getPatient().getId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecord(r).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete confirmed record"));
        }
        recordRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Record deleted"));
    }

    @GetMapping("/me/doctors")
    public ResponseEntity<?> getAssignedDoctors(Authentication auth) {
        Patient patient = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        return ResponseEntity.ok(patient.getDoctors().stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId()); m.put("firstName", d.getFirstName());
            m.put("lastName", d.getLastName()); m.put("specialization", d.getSpecialization());
            m.put("medicalLicenseNumber", d.getMedicalLicenseNumber()); m.put("email", d.getEmail());
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/me/doctors/{doctorId}")
    public ResponseEntity<?> assignDoctor(Authentication auth, @PathVariable Long doctorId) {
        Patient patient = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
        }
        if (doctor.getPatients().contains(patient)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already assigned"));
        }
        doctor.getPatients().add(patient);
        patient.getDoctors().add(doctor);
        doctorRepo.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor assigned"));
    }

    @DeleteMapping("/me/doctors/{doctorId}")
    public ResponseEntity<?> removeDoctor(Authentication auth, @PathVariable Long doctorId) {
        Patient patient = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor != null) {
            doctor.getPatients().remove(patient);
            patient.getDoctors().remove(doctor);
            doctorRepo.save(doctor);
        }
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

    @PostMapping("/me/records/{recordId}/attachments")
    public ResponseEntity<?> uploadAttachment(Authentication auth, @PathVariable Long recordId,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        if (!r.getPatient().getId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }

        String storedFilename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        Files.createDirectories(UPLOAD_DIR);
        file.transferTo(UPLOAD_DIR.resolve(storedFilename));

        User uploader = patientRepo.findById(getCurrentPatientId(auth)).orElseThrow();
        RecordAttachment att = new RecordAttachment();
        att.setRecord(r);
        att.setOriginalFilename(file.getOriginalFilename());
        att.setStoredFilename(storedFilename);
        att.setContentType(file.getContentType());
        att.setSize(file.getSize());
        att.setUploadedBy(uploader);
        att.setUploadedAt(LocalDateTime.now());
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
        if (!r.getPatient().getId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        return ResponseEntity.ok(attachmentRepo.findByRecord(r).stream().map(a -> {
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
        if (!r.getPatient().getId().equals(patientId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
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

    @DeleteMapping("/me/records/{recordId}/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(Authentication auth, @PathVariable Long recordId,
                                              @PathVariable Long attachmentId) {
        HealthRecord r = recordRepo.findById(recordId).orElseThrow();
        if (!r.getPatient().getId().equals(getCurrentPatientId(auth))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your record"));
        }
        if (confirmationRepo.findByRecord(r).isPresent()) {
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
