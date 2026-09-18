package com.clinicaldiary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    @Column(unique = true, nullable = true)
    private String ssn;

    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<HealthRecord> healthRecords = new ArrayList<>();

    @ManyToMany(mappedBy = "patients")
    private List<Doctor> doctors = new ArrayList<>();

    public Patient() {}

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public List<HealthRecord> getHealthRecords() { return healthRecords; }
    public void setHealthRecords(List<HealthRecord> healthRecords) { this.healthRecords = healthRecords; }
    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> doctors) { this.doctors = doctors; }
}
