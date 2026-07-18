package com.clinicaldiary.repository;

import com.clinicaldiary.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
