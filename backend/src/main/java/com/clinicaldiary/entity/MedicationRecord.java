package com.clinicaldiary.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MEDICATION")
public class MedicationRecord extends HealthRecord {

    private String medicationName;
    private String dosage;
    private String frequency;
    private String prescribingDoctor;

    public MedicationRecord() {}

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getPrescribingDoctor() { return prescribingDoctor; }
    public void setPrescribingDoctor(String prescribingDoctor) { this.prescribingDoctor = prescribingDoctor; }
}
