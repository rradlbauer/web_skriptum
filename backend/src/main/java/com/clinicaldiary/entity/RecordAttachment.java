package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "record_attachments")
public class RecordAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String uploadedAt;

    public RecordAttachment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String f) { this.originalFilename = f; }
    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String f) { this.storedFilename = f; }
    public String getContentType() { return contentType; }
    public void setContentType(String c) { this.contentType = c; }
    public Long getSize() { return size; }
    public void setSize(Long s) { this.size = s; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long u) { this.uploadedBy = u; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String a) { this.uploadedAt = a; }
}
