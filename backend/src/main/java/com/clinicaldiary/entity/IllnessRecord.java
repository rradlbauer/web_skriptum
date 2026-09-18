package com.clinicaldiary.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ILLNESS")
public class IllnessRecord extends HealthRecord {

    private String severity;
    private String notes;

    public IllnessRecord() {}

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
