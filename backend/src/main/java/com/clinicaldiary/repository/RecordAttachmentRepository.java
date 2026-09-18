package com.clinicaldiary.repository;

import com.clinicaldiary.entity.RecordAttachment;
import com.clinicaldiary.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecordAttachmentRepository extends JpaRepository<RecordAttachment, Long> {
    List<RecordAttachment> findByRecord(HealthRecord record);
    Optional<RecordAttachment> findByIdAndRecord(Long id, HealthRecord record);
}
