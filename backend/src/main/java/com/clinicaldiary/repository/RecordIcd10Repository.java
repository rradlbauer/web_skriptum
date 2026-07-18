package com.clinicaldiary.repository;

import com.clinicaldiary.entity.RecordIcd10;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordIcd10Repository extends JpaRepository<RecordIcd10, Long> {
    List<RecordIcd10> findByRecordId(Long recordId);
    void deleteByRecordIdAndId(Long recordId, Long id);
}
