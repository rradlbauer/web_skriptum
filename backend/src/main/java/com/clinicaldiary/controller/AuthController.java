package com.clinicaldiary.controller;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import com.clinicaldiary.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(PatientRepository patientRepo, DoctorRepository doctorRepo,
                          PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (patientRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        if (patientRepo.existsBySsn(body.get("ssn"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "SSN already registered"));
        }

        Patient patient = new Patient();
        patient.setEmail(email);
        patient.setPassword(passwordEncoder.encode(body.get("password")));
        patient.setSsn(body.get("ssn"));
        patient.setFirstName(body.get("firstName"));
        patient.setLastName(body.get("lastName"));
        patient.setDateOfBirth(body.get("dateOfBirth"));
        patientRepo.save(patient);

        String token = jwtTokenProvider.generateToken(patient.getId(), patient.getEmail(), "PATIENT");
        return ResponseEntity.ok(Map.of(
                "token", token, "role", "PATIENT",
                "userId", patient.getId(),
                "name", patient.getFirstName() + " " + patient.getLastName()
        ));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (doctorRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        if (doctorRepo.existsByMedicalLicenseNumber(body.get("medicalLicenseNumber"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "License number already registered"));
        }

        Doctor doctor = new Doctor();
        doctor.setEmail(email);
        doctor.setPassword(passwordEncoder.encode(body.get("password")));
        doctor.setMedicalLicenseNumber(body.get("medicalLicenseNumber"));
        doctor.setFirstName(body.get("firstName"));
        doctor.setLastName(body.get("lastName"));
        doctor.setSpecialization(body.get("specialization"));
        doctor.setActive(true);
        doctorRepo.save(doctor);

        String token = jwtTokenProvider.generateToken(doctor.getId(), doctor.getEmail(), "DOCTOR");
        return ResponseEntity.ok(Map.of(
                "token", token, "role", "DOCTOR",
                "userId", doctor.getId(),
                "name", doctor.getFirstName() + " " + doctor.getLastName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        var patientOpt = patientRepo.findByEmail(email);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            if (passwordEncoder.matches(password, patient.getPassword())) {
                String token = jwtTokenProvider.generateToken(patient.getId(), patient.getEmail(), "PATIENT");
                return ResponseEntity.ok(Map.of(
                        "token", token, "role", "PATIENT",
                        "userId", patient.getId(),
                        "name", patient.getFirstName() + " " + patient.getLastName()
                ));
            }
        }

        var doctorOpt = doctorRepo.findByEmail(email);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            if (passwordEncoder.matches(password, doctor.getPassword())) {
                String token = jwtTokenProvider.generateToken(doctor.getId(), doctor.getEmail(), "DOCTOR");
                return ResponseEntity.ok(Map.of(
                        "token", token, "role", "DOCTOR",
                        "userId", doctor.getId(),
                        "name", doctor.getFirstName() + " " + doctor.getLastName()
                ));
            }
        }

        if ("admin@clinicaldiary.at".equals(email) && "admin".equals(password)) {
            String token = jwtTokenProvider.generateToken(0L, email, "SUPERUSER");
            return ResponseEntity.ok(Map.of(
                    "token", token, "role", "SUPERUSER",
                    "userId", 0L, "name", "Administrator"
            ));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(Map.of(
                "userId", jwtTokenProvider.getUserIdFromToken(token),
                "email", jwtTokenProvider.getEmailFromToken(token),
                "role", jwtTokenProvider.getRoleFromToken(token)
        ));
    }
}
