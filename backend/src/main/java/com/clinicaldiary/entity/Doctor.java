package com.clinicaldiary.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("DOCTOR")
public class Doctor extends User {

    @Column(unique = true, nullable = true)
    private String medicalLicenseNumber;

    @Column(nullable = true)
    private String specialization;

    @Column(nullable = true)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
        name = "doctor_patient",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private List<Patient> patients = new ArrayList<>();

    public Doctor() {}

    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public void setMedicalLicenseNumber(String n) { this.medicalLicenseNumber = n; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) { this.specialization = s; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Patient> getPatients() { return patients; }
    public void setPatients(List<Patient> patients) { this.patients = patients; }
}
