package com.clinicaldiary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "record_icd10")
public class RecordIcd10 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private HealthRecord record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icd10_code_id", nullable = false)
    private Icd10Code icd10Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    public RecordIcd10() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HealthRecord getRecord() { return record; }
    public void setRecord(HealthRecord record) { this.record = record; }
    public Icd10Code getIcd10Code() { return icd10Code; }
    public void setIcd10Code(Icd10Code icd10Code) { this.icd10Code = icd10Code; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
