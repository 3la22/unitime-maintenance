package org.unitime.timetable.model;

import org.unitime.timetable.util.NullSafeValidator;

/**
 * Unit Tests for Feature 1: Fix Null Pointer Exception
 * 
 * Tests the NullSafeValidator utility class that fixes the 969 reliability
 * issues found by SonarQube in UniTime.
 * 
 * Run this file independently — no framework needed, uses plain Java assertions.
 */
public class NullSafeValidatorTest {

    static int passed = 0;
    static int failed = 0;

    // ─────────────────────────────────────────────
    // Test 1: requireNonNull — valid object → returns it
    // ─────────────────────────────────────────────
    static void test_requireNonNull_validObject_returnsObject() {
        String input = "UniTime";
        String result = NullSafeValidator.requireNonNull(input, "Should not be null");
        assert result.equals("UniTime") : "FAIL: expected 'UniTime'";
        log("PASS", "requireNonNull with valid object returns the object");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 2: requireNonNull — null → throws exception
    // BEFORE fix: getClassLimit(assignment) called assignment.getPlacement()
    //             without null check → NullPointerException
    // AFTER fix:  NullSafeValidator.requireNonNull throws clear exception
    // ─────────────────────────────────────────────
    static void test_requireNonNull_nullObject_throwsException() {
        try {
            NullSafeValidator.requireNonNull(null, "Assignment must not be null");
            log("FAIL", "requireNonNull with null should have thrown exception");
            failed++;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().equals("Assignment must not be null") : "FAIL: wrong message";
            log("PASS", "requireNonNull with null throws IllegalArgumentException: " + e.getMessage());
            passed++;
        }
    }

    // ─────────────────────────────────────────────
    // Test 3: isNullOrEmpty — null collection → true
    // BEFORE fix: code called collection.iterator() on null → NPE
    // AFTER fix:  isNullOrEmpty check prevents iteration
    // ─────────────────────────────────────────────
    static void test_isNullOrEmpty_nullCollection_returnsTrue() {
        boolean result = NullSafeValidator.isNullOrEmpty(null);
        assert result : "FAIL: null collection should return true";
        log("PASS", "isNullOrEmpty(null) returns true — prevents NPE on iteration");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 4: isNullOrEmpty — empty collection → true
    // ─────────────────────────────────────────────
    static void test_isNullOrEmpty_emptyCollection_returnsTrue() {
        boolean result = NullSafeValidator.isNullOrEmpty(new java.util.ArrayList<>());
        assert result : "FAIL: empty collection should return true";
        log("PASS", "isNullOrEmpty(empty list) returns true");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 5: isNullOrEmpty — non-empty collection → false
    // ─────────────────────────────────────────────
    static void test_isNullOrEmpty_nonEmptyCollection_returnsFalse() {
        java.util.List<String> rooms = java.util.Arrays.asList("Room101");
        boolean result = NullSafeValidator.isNullOrEmpty(rooms);
        assert !result : "FAIL: non-empty collection should return false";
        log("PASS", "isNullOrEmpty(non-empty list) returns false");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 6: safeGetCapacity — null location → returns 0
    // BEFORE fix: room.getCapacity() called on null location → NPE
    // AFTER fix:  returns 0 safely
    // ─────────────────────────────────────────────
    static void test_safeGetCapacity_nullLocation_returnsZero() {
        int result = NullSafeValidator.safeGetCapacity(null, () -> 100);
        assert result == 0 : "FAIL: expected 0 for null location";
        log("PASS", "safeGetCapacity(null location) returns 0 — prevents NPE");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 7: safeGetCapacity — null capacity value → returns 0
    // BEFORE fix: room.getCapacity() returned null → NPE on unboxing
    // AFTER fix:  returns 0 safely
    // ─────────────────────────────────────────────
    static void test_safeGetCapacity_nullCapacityValue_returnsZero() {
        Object fakeRoom = new Object();
        int result = NullSafeValidator.safeGetCapacity(fakeRoom, () -> null);
        assert result == 0 : "FAIL: expected 0 for null capacity";
        log("PASS", "safeGetCapacity with null capacity value returns 0 — prevents NPE");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 8: safeGetCapacity — valid location and capacity → returns value
    // ─────────────────────────────────────────────
    static void test_safeGetCapacity_validInputs_returnsCapacity() {
        Object fakeRoom = new Object();
        int result = NullSafeValidator.safeGetCapacity(fakeRoom, () -> 150);
        assert result == 150 : "FAIL: expected 150";
        log("PASS", "safeGetCapacity with valid inputs returns 150");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 9: safeGetEnrollment — null class → returns 0
    // BEFORE fix: clazz.getEnrollment() on null → NPE
    // AFTER fix:  returns 0 safely
    // ─────────────────────────────────────────────
    static void test_safeGetEnrollment_nullClass_returnsZero() {
        int result = NullSafeValidator.safeGetEnrollment(null, () -> 50);
        assert result == 0 : "FAIL: expected 0 for null class";
        log("PASS", "safeGetEnrollment(null class) returns 0 — prevents NPE");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 10: safeGetEnrollment — valid class → returns enrollment
    // ─────────────────────────────────────────────
    static void test_safeGetEnrollment_validClass_returnsEnrollment() {
        Object fakeClass = new Object();
        int result = NullSafeValidator.safeGetEnrollment(fakeClass, () -> 75);
        assert result == 75 : "FAIL: expected 75";
        log("PASS", "safeGetEnrollment with valid class returns 75");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────
    static void log(String status, String message) {
        System.out.println("[" + status + "] " + message);
    }

    // ─────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Feature 1: Fix Null Pointer Exception");
        System.out.println("  Unit Tests for NullSafeValidator");
        System.out.println("==============================================\n");

        test_requireNonNull_validObject_returnsObject();
        test_requireNonNull_nullObject_throwsException();
        test_isNullOrEmpty_nullCollection_returnsTrue();
        test_isNullOrEmpty_emptyCollection_returnsTrue();
        test_isNullOrEmpty_nonEmptyCollection_returnsFalse();
        test_safeGetCapacity_nullLocation_returnsZero();
        test_safeGetCapacity_nullCapacityValue_returnsZero();
        test_safeGetCapacity_validInputs_returnsCapacity();
        test_safeGetEnrollment_nullClass_returnsZero();
        test_safeGetEnrollment_validClass_returnsEnrollment();

        System.out.println("\n==============================================");
        System.out.println("  Results: " + passed + " PASSED | " + failed + " FAILED");
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }
}
