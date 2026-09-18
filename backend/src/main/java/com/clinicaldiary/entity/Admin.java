package com.clinicaldiary.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    private String role;

    public Admin() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
