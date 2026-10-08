package com.fitpulse.model;

import java.util.Objects;

/**
 * Domain entity representing a fitness challenge in FitPulse.
 * Designed specifically for Set collections (implements equals and hashCode).
 * Demonstrates:
 * - OOP: Encapsulation
 * - Rubric Item 2: Collections & Generics (HashSet<Challenge> duplicate elimination)
 */
public class Challenge {
    private int id;
    private String name;
    private String category; // e.g. "CARDIO", "STRENGTH", "WEIGHT_LOSS"
    private int durationDays;
    private int rewardPoints;

    public Challenge() {}

    public Challenge(int id, String name, String category, int durationDays, int rewardPoints) {
        this.id = id;
        setName(name);
        this.category = category != null ? category : "GENERAL";
        setDurationDays(durationDays);
        setRewardPoints(rewardPoints);
    }

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
            throw new IllegalArgumentException("Challenge name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        if (durationDays <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.durationDays = durationDays;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        if (rewardPoints < 0) {
            throw new IllegalArgumentException("Reward points cannot be negative.");
        }
        this.rewardPoints = rewardPoints;
    }

    /**
     * Proper equals implementation based on challenge ID and Name for HashSet uniqueness.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Challenge challenge = (Challenge) o;
        return id == challenge.id && Objects.equals(name, challenge.name);
    }

    /**
     * HashCode consistent with equals for proper O(1) hash bucket distribution.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return String.format("Challenge[#%d: %s (%s, %d days, %d pts)]",
                id, name, category, durationDays, rewardPoints);
    }
}
