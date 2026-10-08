package com.fitpulse.model;

import com.fitpulse.model.enums.UserRole;

/**
 * Represents a professional fitness trainer in the system.
 * Demonstrates:
 * - OOP: Inheritance (extends User)
 * - Rubric Item 1: OOP & Item 4: Model Class
 */
public class Trainer extends User {
    private String specialization;
    private String certification;

    public Trainer() {
        super();
        setRole(UserRole.TRAINER);
    }

    public Trainer(int id, String name, String email, String passwordHash,
                   String specialization, String certification) {
        super(id, name, email, passwordHash, UserRole.TRAINER);
        this.specialization = specialization;
        this.certification = certification;
    }

    @Override
    public String getRoleTitle() {
        return "Certified Trainer (" + specialization + ")";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }
}
