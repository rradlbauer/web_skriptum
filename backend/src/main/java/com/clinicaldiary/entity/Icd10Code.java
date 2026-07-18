package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "icd10_codes")
public class Icd10Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String description;

    public Icd10Code() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
}
