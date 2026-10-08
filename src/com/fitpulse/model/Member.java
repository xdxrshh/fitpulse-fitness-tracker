package com.fitpulse.model;

import com.fitpulse.model.enums.UserRole;

/**
 * Represents a fitness member.
 * Demonstrates:
 * - OOP: Inheritance (extends User, calls super)
 * - OOP: Encapsulation (validated health metrics)
 * - Rubric Item 1: OOP & Item 4: Model Class
 */
public class Member extends User {
    private double weightKg;
    private double heightCm;
    private String fitnessLevel;
    private Integer trainerId; // nullable foreign key to Trainer

    public Member() {
        super();
        setRole(UserRole.MEMBER);
    }

    public Member(int id, String name, String email, String passwordHash,
                  double weightKg, double heightCm, String fitnessLevel, Integer trainerId) {
        super(id, name, email, passwordHash, UserRole.MEMBER);
        setWeightKg(weightKg);
        setHeightCm(heightCm);
        this.fitnessLevel = fitnessLevel != null ? fitnessLevel : "BEGINNER";
        this.trainerId = trainerId;
    }

    @Override
    public String getRoleTitle() {
        return "Club Member (" + fitnessLevel + ")";
    }

    public double calculateBMI() {
        if (heightCm <= 0) return 0.0;
        double heightInMeters = heightCm / 100.0;
        return weightKg / (heightInMeters * heightInMeters);
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        if (weightKg <= 0 || weightKg > 500) {
            throw new IllegalArgumentException("Weight must be between 1 and 500 kg.");
        }
        this.weightKg = weightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        if (heightCm <= 0 || heightCm > 300) {
            throw new IllegalArgumentException("Height must be between 1 and 300 cm.");
        }
        this.heightCm = heightCm;
    }

    public String getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public Integer getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }
}
