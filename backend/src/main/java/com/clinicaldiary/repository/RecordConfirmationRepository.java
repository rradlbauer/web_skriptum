package com.clinicaldiary.repository;

import com.clinicaldiary.entity.RecordConfirmation;
import com.clinicaldiary.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecordConfirmationRepository extends JpaRepository<RecordConfirmation, Long> {
    Optional<RecordConfirmation> findByRecord(HealthRecord record);
}
