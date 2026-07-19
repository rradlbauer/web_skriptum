package com.clinicaldiary.repository;

import com.clinicaldiary.entity.RecordAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordAttachmentRepository extends JpaRepository<RecordAttachment, Long> {
    List<RecordAttachment> findByRecordId(Long recordId);
    Optional<RecordAttachment> findByIdAndRecordId(Long id, Long recordId);
    void deleteByRecordIdAndId(Long recordId, Long id);
}
