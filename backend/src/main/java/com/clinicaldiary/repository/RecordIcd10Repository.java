package com.clinicaldiary.repository;

import com.clinicaldiary.entity.RecordIcd10;
import com.clinicaldiary.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordIcd10Repository extends JpaRepository<RecordIcd10, Long> {
    List<RecordIcd10> findByRecord(HealthRecord record);
}
