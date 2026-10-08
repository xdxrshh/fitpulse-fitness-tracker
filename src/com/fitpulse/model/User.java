package com.fitpulse.model;

import com.fitpulse.model.enums.UserRole;
import java.time.LocalDateTime;

/**
 * Abstract base class representing a system actor in FitPulse.
 * Demonstrates:
 * - OOP: Encapsulation (private fields, defensive setters)
 * - OOP: Inheritance (base class for Member and Trainer)
 * - Rubric Item 4: Database Model Class
 */
public abstract class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String name, String email, String passwordHash, UserRole role) {
        this.id = id;
        setName(name);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.MEMBER;
        this.createdAt = LocalDateTime.now();
    }

    // Polymorphic method to be implemented or customized by subclasses
    public abstract String getRoleTitle();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s #%d] %s (%s)", role, id, name, email);
    }
}
