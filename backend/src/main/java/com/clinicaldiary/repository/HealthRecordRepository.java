package com.clinicaldiary.repository;

import com.clinicaldiary.entity.HealthRecord;
import com.clinicaldiary.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPatientOrderByCreatedAtDesc(Patient patient);
}
