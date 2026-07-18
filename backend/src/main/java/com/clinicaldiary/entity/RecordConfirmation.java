package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "record_confirmations")
public class RecordConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    private Long doctorId;

    private String doctorName;
    private String doctorLicenseNumber;
    private String comment;
    private String confirmedAt;

    public RecordConfirmation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long r) { this.recordId = r; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long d) { this.doctorId = d; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String n) { this.doctorName = n; }
    public String getDoctorLicenseNumber() { return doctorLicenseNumber; }
    public void setDoctorLicenseNumber(String l) { this.doctorLicenseNumber = l; }
    public String getComment() { return comment; }
    public void setComment(String c) { this.comment = c; }
    public String getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(String c) { this.confirmedAt = c; }
}
