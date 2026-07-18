package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String medicalLicenseNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private boolean active = true;

    public Doctor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public void setMedicalLicenseNumber(String n) { this.medicalLicenseNumber = n; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) { this.specialization = s; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
