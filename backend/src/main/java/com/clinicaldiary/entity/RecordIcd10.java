package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "record_icd10")
public class RecordIcd10 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    private Long icd10CodeId;

    private String icd10Code;
    private String icd10Description;

    @Column(nullable = false)
    private Long doctorId;

    private String assignedAt;

    public RecordIcd10() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long r) { this.recordId = r; }
    public Long getIcd10CodeId() { return icd10CodeId; }
    public void setIcd10CodeId(Long c) { this.icd10CodeId = c; }
    public String getIcd10Code() { return icd10Code; }
    public void setIcd10Code(String c) { this.icd10Code = c; }
    public String getIcd10Description() { return icd10Description; }
    public void setIcd10Description(String d) { this.icd10Description = d; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long d) { this.doctorId = d; }
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String a) { this.assignedAt = a; }
}
