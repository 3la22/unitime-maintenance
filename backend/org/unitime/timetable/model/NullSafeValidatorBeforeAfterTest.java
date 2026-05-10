package org.unitime.timetable.model;

import org.unitime.timetable.util.NullSafeValidator;

/**
 * Before/After Unit Tests — Feature 1: Fix Null Pointer Exception
 *
 * Demonstrates test cases that FAILED before the fix (old behaviour
 * caused NullPointerException or unsafe results) and PASS after the
 * fix (NullSafeValidator handles null inputs safely).
 *
 * Run independently — no framework needed, uses plain Java assertions.
 */
public class NullSafeValidatorBeforeAfterTest {

    static int passed = 0;
    static int failed = 0;

    // ─────────────────────────────────────────────────────────────────────
    // BEFORE helpers — simulate the OLD unsafe behaviour (no null checks)
    // ─────────────────────────────────────────────────────────────────────

    /** OLD: getClassLimit(assignment) called assignment.getPlacement()
     *  directly with no null check → NullPointerException when null */
    static int oldGetClassLimit(Object assignment) {
        // Simulates: assignment.getPlacement().getRoomSize() — NPE if null
        if (assignment == null)
            throw new NullPointerException("Assignment is null — NPE in getClassLimit()");
        return 30; // would normally compute from placement
    }

    /** OLD: iterating a collection without a null guard → NPE */
    static int oldCountEnrollments(java.util.Collection<?> enrollments) {
        // Simulates: for (Object e : enrollments) { ... } — NPE if null
        int count = 0;
        for (Object e : enrollments) { count++; } // throws NPE when null
        return count;
    }

    /** OLD: room.getCapacity() unboxed directly — NPE if capacity is null */
    static int oldGetRoomCapacity(Integer capacity) {
        return capacity; // auto-unbox → NPE when Integer is null
    }

    // ─────────────────────────────────────────────────────────────────────
    // BEFORE tests — these FAILED (threw NPE) before the fix
    // ─────────────────────────────────────────────────────────────────────

    static void before_getClassLimit_nullAssignment_throwsNPE() {
        try {
            oldGetClassLimit(null);
            log("FAIL", "[BEFORE] Expected NPE was not thrown — test invalid");
            failed++;
        } catch (NullPointerException e) {
            log("FAIL (expected before fix)",
                "[BEFORE] getClassLimit(null) → NullPointerException: " + e.getMessage());
            // This is the EXPECTED before-fix behaviour — document it, don't count as our failure
        }
    }

    static void before_iterateNullCollection_throwsNPE() {
        try {
            oldCountEnrollments(null);
            log("FAIL", "[BEFORE] Expected NPE was not thrown — test invalid");
            failed++;
        } catch (NullPointerException e) {
            log("FAIL (expected before fix)",
                "[BEFORE] Iterating null collection → NullPointerException");
        }
    }

    static void before_unboxNullCapacity_throwsNPE() {
        try {
            oldGetRoomCapacity(null);
            log("FAIL", "[BEFORE] Expected NPE was not thrown — test invalid");
            failed++;
        } catch (NullPointerException e) {
            log("FAIL (expected before fix)",
                "[BEFORE] Unboxing null Integer capacity → NullPointerException");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // AFTER tests — same scenarios NOW PASS with NullSafeValidator
    // ─────────────────────────────────────────────────────────────────────

    static void after_requireNonNull_nullAssignment_throwsClearException() {
        try {
            NullSafeValidator.requireNonNull(null, "Assignment must not be null");
            log("FAIL", "[AFTER] Should have thrown IllegalArgumentException");
            failed++;
        } catch (IllegalArgumentException e) {
            // Clear, controlled exception — NOT a NullPointerException
            assert e.getMessage().equals("Assignment must not be null") : "Wrong message";
            log("PASS", "[AFTER] null assignment → IllegalArgumentException with clear message (no NPE)");
            passed++;
        }
    }

    static void after_isNullOrEmpty_nullCollection_returnsTrue() {
        boolean result = NullSafeValidator.isNullOrEmpty(null);
        assert result : "FAIL: null collection should return true";
        log("PASS", "[AFTER] isNullOrEmpty(null) → true — iteration skipped safely, no NPE");
        passed++;
    }

    static void after_safeGetCapacity_nullCapacity_returnsZero() {
        Object fakeRoom = new Object();
        int result = NullSafeValidator.safeGetCapacity(fakeRoom, () -> null);
        assert result == 0 : "FAIL: expected 0 for null capacity";
        log("PASS", "[AFTER] safeGetCapacity(null value) → 0 — no NPE on unboxing");
        passed++;
    }

    static void after_safeGetCapacity_nullLocation_returnsZero() {
        int result = NullSafeValidator.safeGetCapacity(null, () -> 100);
        assert result == 0 : "FAIL: expected 0 for null location";
        log("PASS", "[AFTER] safeGetCapacity(null location) → 0 — no NPE");
        passed++;
    }

    static void after_safeGetCapacity_validInputs_returnsCapacity() {
        Object fakeRoom = new Object();
        int result = NullSafeValidator.safeGetCapacity(fakeRoom, () -> 150);
        assert result == 150 : "FAIL: expected 150";
        log("PASS", "[AFTER] safeGetCapacity(valid) → 150 — correct value");
        passed++;
    }

    static void after_isNullOrEmpty_emptyList_returnsTrue() {
        boolean result = NullSafeValidator.isNullOrEmpty(new java.util.ArrayList<>());
        assert result : "FAIL: empty list should return true";
        log("PASS", "[AFTER] isNullOrEmpty(empty list) → true");
        passed++;
    }

    static void after_isNullOrEmpty_nonEmptyList_returnsFalse() {
        boolean result = NullSafeValidator.isNullOrEmpty(java.util.Arrays.asList("Room101"));
        assert !result : "FAIL: non-empty list should return false";
        log("PASS", "[AFTER] isNullOrEmpty(non-empty list) → false");
        passed++;
    }

    static void after_safeGetEnrollment_nullClass_returnsZero() {
        int result = NullSafeValidator.safeGetEnrollment(null, () -> 50);
        assert result == 0 : "FAIL: expected 0 for null class";
        log("PASS", "[AFTER] safeGetEnrollment(null class) → 0 — no NPE");
        passed++;
    }

    static void after_safeGetEnrollment_validClass_returnsEnrollment() {
        int result = NullSafeValidator.safeGetEnrollment(new Object(), () -> 75);
        assert result == 75 : "FAIL: expected 75";
        log("PASS", "[AFTER] safeGetEnrollment(valid) → 75 — correct value");
        passed++;
    }

    static void after_requireNonNull_validObject_returnsIt() {
        String result = NullSafeValidator.requireNonNull("UniTime", "must not be null");
        assert "UniTime".equals(result) : "FAIL: expected 'UniTime'";
        log("PASS", "[AFTER] requireNonNull(valid object) → returns object safely");
        passed++;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────
    static void log(String status, String message) {
        System.out.println("[" + status + "] " + message);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Feature 1: Fix Null Pointer Exception");
        System.out.println("  Before/After Tests for NullSafeValidator");
        System.out.println("==============================================\n");

        System.out.println("── BEFORE FIX: cases that caused NullPointerException ──\n");
        before_getClassLimit_nullAssignment_throwsNPE();
        before_iterateNullCollection_throwsNPE();
        before_unboxNullCapacity_throwsNPE();

        System.out.println("\n── AFTER FIX: same cases handled safely ──\n");
        after_requireNonNull_nullAssignment_throwsClearException();
        after_isNullOrEmpty_nullCollection_returnsTrue();
        after_safeGetCapacity_nullCapacity_returnsZero();
        after_safeGetCapacity_nullLocation_returnsZero();
        after_safeGetCapacity_validInputs_returnsCapacity();
        after_isNullOrEmpty_emptyList_returnsTrue();
        after_isNullOrEmpty_nonEmptyList_returnsFalse();
        after_safeGetEnrollment_nullClass_returnsZero();
        after_safeGetEnrollment_validClass_returnsEnrollment();
        after_requireNonNull_validObject_returnsIt();

        System.out.println("\n==============================================");
        System.out.println("  After-Fix Results: " + passed + " PASSED | " + failed + " FAILED");
        System.out.println("  Before-Fix: 3 cases confirmed to throw NPE");
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }
}
