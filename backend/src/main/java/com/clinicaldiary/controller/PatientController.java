package com.clinicaldiary.controller;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import com.clinicaldiary.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    public PatientController(PatientRepository patientRepo, HealthRecordRepository recordRepo,
                             DoctorPatientRepository doctorPatientRepo, DoctorRepository doctorRepo,
                             RecordConfirmationRepository confirmationRepo,
                             RecordIcd10Repository recordIcd10Repo) {
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.doctorPatientRepo = doctorPatientRepo;
        this.doctorRepo = doctorRepo;
        this.confirmationRepo = confirmationRepo;
        this.recordIcd10Repo = recordIcd10Repo;
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
        return m;
    }
}
