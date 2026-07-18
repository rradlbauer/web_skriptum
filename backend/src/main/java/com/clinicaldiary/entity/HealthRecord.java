package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String category;

    private String title;
    private String description;
    private String dateFrom;
    private String dateTo;
    private String severity;
    private String notes;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String prescribingDoctor;
    private String vaccineName;
    private Integer doseNumber;
    private String batchNumber;
    private String institution;

    @Column(nullable = false)
    private String createdAt;

    private String updatedAt;

    public HealthRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String d) { this.dateFrom = d; }
    public String getDateTo() { return dateTo; }
    public void setDateTo(String d) { this.dateTo = d; }
    public String getSeverity() { return severity; }
    public void setSeverity(String s) { this.severity = s; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String m) { this.medicationName = m; }
    public String getDosage() { return dosage; }
    public void setDosage(String d) { this.dosage = d; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { this.frequency = f; }
    public String getPrescribingDoctor() { return prescribingDoctor; }
    public void setPrescribingDoctor(String d) { this.prescribingDoctor = d; }
    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String v) { this.vaccineName = v; }
    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer d) { this.doseNumber = d; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String b) { this.batchNumber = b; }
    public String getInstitution() { return institution; }
    public void setInstitution(String i) { this.institution = i; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String c) { this.createdAt = c; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String u) { this.updatedAt = u; }
}
