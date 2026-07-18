package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctor_patient")
public class DoctorPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private Long patientId;

    private String assignedAt;

    public DoctorPatient() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String a) { this.assignedAt = a; }
}
