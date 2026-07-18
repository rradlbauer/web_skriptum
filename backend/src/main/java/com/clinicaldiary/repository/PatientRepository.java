package com.clinicaldiary.repository;

import com.clinicaldiary.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsBySsn(String ssn);
}
