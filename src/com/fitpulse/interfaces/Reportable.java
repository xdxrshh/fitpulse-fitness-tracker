package com.fitpulse.interfaces;

import java.util.Map;

/**
 * Interface contract for domain entities capable of emitting formatted report summaries
 * and analytical metric key-value dictionaries.
 * Part of Rubric Item 1: OOP (Interfaces) & Item 2: Collections (Map).
 */
public interface Reportable {
    /**
     * Produces a human-readable text summary of the entity.
     * @return Formatted summary string
     */
    String generateSummary();

    /**
     * Converts the entity's key metrics into a Map for analytics and reporting.
     * @return Map containing metric names as keys and values as Objects.
     */
    Map<String, Object> toMetricMap();
}
