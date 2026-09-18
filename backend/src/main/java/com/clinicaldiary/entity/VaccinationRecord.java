package com.clinicaldiary.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VACCINATION")
public class VaccinationRecord extends HealthRecord {

    private String vaccineName;
    private Integer doseNumber;
    private String batchNumber;
    private String institution;

    public VaccinationRecord() {}

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
}
