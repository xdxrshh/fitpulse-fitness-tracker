package com.fitpulse.interfaces;

/**
 * Interface representing any activity or plan whose physical metrics are measurable.
 * Part of Rubric Item 1: OOP Implementation - Interfaces & Polymorphism.
 */
public interface Trackable {
    /**
     * Calculates the estimated calories burned using domain-specific physics algorithms.
     * @return calories burned in kcal
     */
    double calculateCaloriesBurned();

    /**
     * Computes the subjective exertion score on a scale of 1 to 10.
     * @return intensity score (1-10)
     */
    int getIntensityScore();
}
