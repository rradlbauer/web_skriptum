package com.clinicaldiary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "record_attachments")
public class RecordAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private HealthRecord record;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public RecordAttachment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HealthRecord getRecord() { return record; }
    public void setRecord(HealthRecord record) { this.record = record; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String f) { this.originalFilename = f; }
    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String f) { this.storedFilename = f; }
    public String getContentType() { return contentType; }
    public void setContentType(String c) { this.contentType = c; }
    public Long getSize() { return size; }
    public void setSize(Long s) { this.size = s; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
