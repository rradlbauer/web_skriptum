package com.clinicaldiary.controller;

import com.clinicaldiary.entity.Doctor;
import com.clinicaldiary.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/superuser")
public class SuperuserController {

    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final HealthRecordRepository recordRepo;
    private final RecordConfirmationRepository confirmationRepo;
    private final PasswordEncoder passwordEncoder;

    public SuperuserController(DoctorRepository doctorRepo, PatientRepository patientRepo,
                               HealthRecordRepository recordRepo,
                               RecordConfirmationRepository confirmationRepo,
                               PasswordEncoder passwordEncoder) {
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.confirmationRepo = confirmationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        List<Doctor> doctors = doctorRepo.findAll();
        List<Map<String, Object>> result = doctors.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("email", d.getEmail());
            map.put("medicalLicenseNumber", d.getMedicalLicenseNumber());
            map.put("firstName", d.getFirstName());
            map.put("lastName", d.getLastName());
            map.put("specialization", d.getSpecialization());
            map.put("active", d.isActive());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String medicalLicenseNumber = body.get("medicalLicenseNumber");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String specialization = body.get("specialization");

        if (doctorRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        if (doctorRepo.existsByMedicalLicenseNumber(medicalLicenseNumber)) {
            return ResponseEntity.badRequest().body(Map.of("error", "License number already registered"));
        }

        Doctor doctor = new Doctor();
        doctor.setEmail(email);
        doctor.setPassword(passwordEncoder.encode(password));
        doctor.setMedicalLicenseNumber(medicalLicenseNumber);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialization(specialization);
        doctor.setActive(true);
        doctorRepo.save(doctor);

        Map<String, Object> result = new HashMap<>();
        result.put("id", doctor.getId());
        result.put("email", doctor.getEmail());
        result.put("medicalLicenseNumber", doctor.getMedicalLicenseNumber());
        result.put("firstName", doctor.getFirstName());
        result.put("lastName", doctor.getLastName());
        result.put("specialization", doctor.getSpecialization());
        result.put("active", doctor.isActive());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/doctors/{id}/deactivate")
    public ResponseEntity<?> deactivateDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow();
        doctor.setActive(false);
        doctorRepo.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor deactivated"));
    }

    @PutMapping("/doctors/{id}/activate")
    public ResponseEntity<?> activateDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepo.findById(id).orElseThrow();
        doctor.setActive(true);
        doctorRepo.save(doctor);
        return ResponseEntity.ok(Map.of("message", "Doctor activated"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", patientRepo.count());
        stats.put("totalDoctors", doctorRepo.count());
        stats.put("activeDoctors", doctorRepo.findByActiveTrue().size());
        stats.put("totalRecords", recordRepo.count());
        return ResponseEntity.ok(stats);
    }
}
