package org.unitime.timetable.util;

/**
 * NullSafeValidator - Feature 1: Fix Null Pointer Exception
 * 
 * Centralises null-safety logic to eliminate repeated null-check boilerplate
 * across the 969 reliability issues found by SonarQube.
 */
public class NullSafeValidator {

    /**
     * Ensures the given object is not null.
     * Throws IllegalArgumentException if null.
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    /**
     * Returns true if the collection is null or empty.
     */
    public static boolean isNullOrEmpty(java.util.Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * Safely gets room capacity — returns 0 if location or capacity is null.
     */
    public static int safeGetCapacity(Object location, java.util.function.Supplier<Integer> capacitySupplier) {
        if (location == null || capacitySupplier == null) return 0;
        Integer capacity = capacitySupplier.get();
        return capacity == null ? 0 : capacity;
    }

    /**
     * Safely gets enrollment — returns 0 if object is null.
     */
    public static int safeGetEnrollment(Object cls, java.util.function.Supplier<Integer> enrollmentSupplier) {
        if (cls == null || enrollmentSupplier == null) return 0;
        Integer enrollment = enrollmentSupplier.get();
        return enrollment == null ? 0 : enrollment;
    }
}
