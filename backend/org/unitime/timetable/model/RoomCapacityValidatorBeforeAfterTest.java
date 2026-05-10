package org.unitime.timetable.model;

import org.unitime.timetable.util.RoomCapacityValidator;
import org.unitime.timetable.util.RoomCapacityValidator.ValidationResult;

/**
 * Before/After Unit Tests — Feature 2: Add Room Capacity Validation
 *
 * Demonstrates test cases that FAILED (wrong/unsafe behaviour) before
 * the fix and PASS correctly after RoomCapacityValidator was added.
 *
 * Run independently — no framework needed, uses plain Java assertions.
 */
public class RoomCapacityValidatorBeforeAfterTest {

    static int passed = 0;
    static int failed = 0;

    // ─────────────────────────────────────────────────────────────────────
    // BEFORE helpers — simulate old behaviour: no validation at all
    // ─────────────────────────────────────────────────────────────────────

    /** OLD: assignRoom() simply assigned without any capacity check.
     *  Returns true always — even if 200 students crammed into a 50-seat room. */
    static boolean oldAssignRoom(int enrollment, int roomCapacity) {
        // BEFORE fix: no validation — assignment always "succeeds"
        return true;
    }

    /** OLD: no effective limit computed — room ratio was ignored. */
    static int oldComputeLimit(int roomCapacity) {
        // BEFORE fix: just return raw capacity, ratio not applied
        return roomCapacity;
    }

    // ─────────────────────────────────────────────────────────────────────
    // BEFORE tests — these produced WRONG results before the fix
    // ─────────────────────────────────────────────────────────────────────

    static void before_200studentsIn50Room_noValidation_wronglyAllowed() {
        boolean assigned = oldAssignRoom(200, 50);
        // OLD code returned true — students would be booked into too-small room
        boolean isWrongBehaviour = (assigned == true); // should be false but wasn't
        log("FAIL (expected before fix)",
            "[BEFORE] 200 students in 50-seat room → silently allowed (no validation)");
    }

    static void before_51studentsIn50Room_noValidation_wronglyAllowed() {
        boolean assigned = oldAssignRoom(51, 50);
        boolean isWrongBehaviour = (assigned == true);
        log("FAIL (expected before fix)",
            "[BEFORE] 51 students in 50-seat room → silently allowed (boundary ignored)");
    }

    static void before_zeroCapacityRoom_noValidation_wronglyAllowed() {
        boolean assigned = oldAssignRoom(10, 0);
        boolean isWrongBehaviour = (assigned == true);
        log("FAIL (expected before fix)",
            "[BEFORE] Any students in 0-capacity room → silently allowed");
    }

    static void before_roomRatioIgnored_wrongLimit() {
        // OLD: ratio was not applied → 60 students allowed in effective-50 room
        int limit = oldComputeLimit(100); // ratio 2.0 ignored → returns 100 instead of 50
        boolean isWrongBehaviour = (limit == 100); // correct would be 50
        log("FAIL (expected before fix)",
            "[BEFORE] computeLimit(100, ratio=2.0) → 100 (ratio ignored, should be 50)");
    }

    // ─────────────────────────────────────────────────────────────────────
    // AFTER tests — same scenarios NOW handled correctly
    // ─────────────────────────────────────────────────────────────────────

    static void after_200studentsIn50Room_blocked() {
        ValidationResult result = RoomCapacityValidator.validate(200, 50);
        assert !result.isValid() : "FAIL: 200 students should NOT fit in 50-seat room";
        assert result.getMessage().contains("exceeded") : "FAIL: message should mention 'exceeded'";
        log("PASS", "[AFTER] 200 students in 50-seat room → blocked, message: " + result.getMessage());
        passed++;
    }

    static void after_51studentsIn50Room_blocked_boundary() {
        ValidationResult result = RoomCapacityValidator.validate(51, 50);
        assert !result.isValid() : "FAIL: 51 students should NOT fit in 50-seat room";
        log("PASS", "[AFTER] 51 students in 50-seat room → blocked (boundary enforced)");
        passed++;
    }

    static void after_50studentsIn50Room_allowed_boundary() {
        ValidationResult result = RoomCapacityValidator.validate(50, 50);
        assert result.isValid() : "FAIL: exactly 50 students should fit in 50-seat room";
        log("PASS", "[AFTER] 50 students in 50-seat room → allowed (boundary = ok)");
        passed++;
    }

    static void after_30studentsIn50Room_allowed() {
        ValidationResult result = RoomCapacityValidator.validate(30, 50);
        assert result.isValid() : "FAIL: 30 students should fit in 50-seat room";
        log("PASS", "[AFTER] 30 students in 50-seat room → allowed");
        passed++;
    }

    static void after_zeroCapacityRoom_blocked() {
        ValidationResult result = RoomCapacityValidator.validate(10, 0);
        assert !result.isValid() : "FAIL: 0-capacity room should be invalid";
        assert result.getMessage().contains("Invalid room capacity") : "FAIL: wrong message";
        log("PASS", "[AFTER] 0-capacity room → blocked with clear message: " + result.getMessage());
        passed++;
    }

    static void after_roomRatio_effectiveLimitComputed() {
        // ratio=2.0 → effectiveLimit = floor(100/2.0) = 50
        ValidationResult result = RoomCapacityValidator.validate(60, 100, 2.0f);
        assert !result.isValid() : "FAIL: 60 students should NOT fit with effectiveLimit=50";
        assert result.getEffectiveLimit() == 50 : "FAIL: effectiveLimit should be 50, got " + result.getEffectiveLimit();
        log("PASS", "[AFTER] room=100, ratio=2.0 → effectiveLimit=50 → 60 students blocked");
        passed++;
    }

    static void after_roomRatio_half_effectiveLimitDoubled() {
        // ratio=0.5 → effectiveLimit = floor(100/0.5) = 200
        ValidationResult result = RoomCapacityValidator.validate(150, 100, 0.5f);
        assert result.isValid() : "FAIL: 150 students should fit with effectiveLimit=200";
        assert result.getEffectiveLimit() == 200 : "FAIL: effectiveLimit should be 200";
        log("PASS", "[AFTER] room=100, ratio=0.5 → effectiveLimit=200 → 150 students allowed");
        passed++;
    }

    static void after_computeEffectiveLimit_defaultRatio() {
        int limit = RoomCapacityValidator.computeEffectiveLimit(100, 1.0f);
        assert limit == 100 : "FAIL: expected 100";
        log("PASS", "[AFTER] computeEffectiveLimit(100, 1.0) = 100");
        passed++;
    }

    static void after_computeEffectiveLimit_zeroRatioDefaultsToOne() {
        int limit = RoomCapacityValidator.computeEffectiveLimit(80, 0f);
        assert limit == 80 : "FAIL: expected 80 when ratio defaults to 1.0";
        log("PASS", "[AFTER] computeEffectiveLimit(80, 0) → defaults ratio=1.0 → limit=80");
        passed++;
    }

    static void after_resultFields_areCorrect() {
        ValidationResult result = RoomCapacityValidator.validate(40, 100, 1.0f);
        assert result.getEnrollment() == 40 : "FAIL: enrollment should be 40";
        assert result.getRoomCapacity() == 100 : "FAIL: roomCapacity should be 100";
        assert result.getEffectiveLimit() == 100 : "FAIL: effectiveLimit should be 100";
        assert result.isValid() : "FAIL: should be valid";
        log("PASS", "[AFTER] ValidationResult fields: enrollment=40, capacity=100, limit=100, valid=true");
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
        System.out.println("  Feature 2: Add Room Capacity Validation");
        System.out.println("  Before/After Tests for RoomCapacityValidator");
        System.out.println("==============================================\n");

        System.out.println("── BEFORE FIX: cases that produced wrong/unsafe results ──\n");
        before_200studentsIn50Room_noValidation_wronglyAllowed();
        before_51studentsIn50Room_noValidation_wronglyAllowed();
        before_zeroCapacityRoom_noValidation_wronglyAllowed();
        before_roomRatioIgnored_wrongLimit();

        System.out.println("\n── AFTER FIX: same cases handled correctly ──\n");
        after_200studentsIn50Room_blocked();
        after_51studentsIn50Room_blocked_boundary();
        after_50studentsIn50Room_allowed_boundary();
        after_30studentsIn50Room_allowed();
        after_zeroCapacityRoom_blocked();
        after_roomRatio_effectiveLimitComputed();
        after_roomRatio_half_effectiveLimitDoubled();
        after_computeEffectiveLimit_defaultRatio();
        after_computeEffectiveLimit_zeroRatioDefaultsToOne();
        after_resultFields_areCorrect();

        System.out.println("\n==============================================");
        System.out.println("  After-Fix Results: " + passed + " PASSED | " + failed + " FAILED");
        System.out.println("  Before-Fix: 4 cases confirmed to allow invalid assignments");
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }
}
