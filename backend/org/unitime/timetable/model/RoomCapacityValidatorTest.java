package org.unitime.timetable.model;

import org.unitime.timetable.util.RoomCapacityValidator;
import org.unitime.timetable.util.RoomCapacityValidator.ValidationResult;

/**
 * Unit Tests for Feature 2: Add Room Capacity Validation
 *
 * Tests the RoomCapacityValidator utility class that prevents over-booking
 * of rooms during class assignment in UniTime.
 *
 * Run independently — no framework needed, uses plain Java assertions.
 */
public class RoomCapacityValidatorTest {

    static int passed = 0;
    static int failed = 0;

    // ─────────────────────────────────────────────
    // Test 1: enrollment fits in room → valid
    // BEFORE fix: no check, 30 students assigned to 50-seat room silently
    // AFTER fix:  returns valid=true with confirmation message
    // ─────────────────────────────────────────────
    static void test_validate_enrollmentFitsRoom_returnsValid() {
        ValidationResult result = RoomCapacityValidator.validate(30, 50);
        assert result.isValid() : "FAIL: 30 students should fit in 50-seat room";
        assert result.getEffectiveLimit() == 50 : "FAIL: effectiveLimit should be 50";
        log("PASS", "30 students fit in 50-seat room → valid=true");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 2: enrollment equals room capacity → valid (boundary)
    // ─────────────────────────────────────────────
    static void test_validate_enrollmentEqualsCapacity_returnsValid() {
        ValidationResult result = RoomCapacityValidator.validate(50, 50);
        assert result.isValid() : "FAIL: 50 students should fit in exactly 50-seat room";
        log("PASS", "50 students in 50-seat room → valid=true (boundary)");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 3: enrollment exceeds room capacity → invalid
    // BEFORE fix: 200 students assigned to 50-seat room with no warning
    // AFTER fix:  returns valid=false with clear message
    // ─────────────────────────────────────────────
    static void test_validate_enrollmentExceedsCapacity_returnsInvalid() {
        ValidationResult result = RoomCapacityValidator.validate(200, 50);
        assert !result.isValid() : "FAIL: 200 students should NOT fit in 50-seat room";
        assert result.getMessage().contains("exceeded") : "FAIL: message should mention 'exceeded'";
        log("PASS", "200 students in 50-seat room → valid=false, message: " + result.getMessage());
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 4: enrollment exceeds capacity by 1 → invalid (boundary)
    // ─────────────────────────────────────────────
    static void test_validate_enrollmentExceedsByOne_returnsInvalid() {
        ValidationResult result = RoomCapacityValidator.validate(51, 50);
        assert !result.isValid() : "FAIL: 51 students should NOT fit in 50-seat room";
        log("PASS", "51 students in 50-seat room → valid=false (boundary)");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 5: invalid room capacity (0) → invalid
    // ─────────────────────────────────────────────
    static void test_validate_zeroRoomCapacity_returnsInvalid() {
        ValidationResult result = RoomCapacityValidator.validate(10, 0);
        assert !result.isValid() : "FAIL: room with 0 capacity should be invalid";
        assert result.getMessage().contains("Invalid room capacity") : "FAIL: wrong message";
        log("PASS", "Room with 0 capacity → valid=false, message: " + result.getMessage());
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 6: roomRatio = 0.5 → effectiveLimit = floor(100/0.5) = 200
    // ─────────────────────────────────────────────
    static void test_validate_withRoomRatio_computesEffectiveLimit() {
        ValidationResult result = RoomCapacityValidator.validate(150, 100, 0.5f);
        assert result.isValid() : "FAIL: 150 students should fit with ratio 0.5 (effectiveLimit=200)";
        assert result.getEffectiveLimit() == 200 : "FAIL: effectiveLimit should be 200, got " + result.getEffectiveLimit();
        log("PASS", "150 students, room=100, ratio=0.5 → effectiveLimit=200 → valid=true");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 7: roomRatio = 2.0 → effectiveLimit = floor(100/2.0) = 50
    // ─────────────────────────────────────────────
    static void test_validate_withHighRoomRatio_reducesEffectiveLimit() {
        ValidationResult result = RoomCapacityValidator.validate(60, 100, 2.0f);
        assert !result.isValid() : "FAIL: 60 students should NOT fit with ratio 2.0 (effectiveLimit=50)";
        assert result.getEffectiveLimit() == 50 : "FAIL: effectiveLimit should be 50, got " + result.getEffectiveLimit();
        log("PASS", "60 students, room=100, ratio=2.0 → effectiveLimit=50 → valid=false");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 8: computeEffectiveLimit directly
    // ─────────────────────────────────────────────
    static void test_computeEffectiveLimit_normalValues() {
        int limit = RoomCapacityValidator.computeEffectiveLimit(100, 1.0f);
        assert limit == 100 : "FAIL: expected 100, got " + limit;
        log("PASS", "computeEffectiveLimit(100, 1.0) = 100");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 9: computeEffectiveLimit with ratio 0 → defaults to 1.0
    // ─────────────────────────────────────────────
    static void test_computeEffectiveLimit_zeroRatio_defaultsToOne() {
        int limit = RoomCapacityValidator.computeEffectiveLimit(80, 0f);
        assert limit == 80 : "FAIL: expected 80 when ratio defaults to 1.0, got " + limit;
        log("PASS", "computeEffectiveLimit(80, 0) defaults ratio to 1.0 → limit=80");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 10: result fields are correctly populated
    // ─────────────────────────────────────────────
    static void test_validate_resultFieldsAreCorrect() {
        ValidationResult result = RoomCapacityValidator.validate(40, 100, 1.0f);
        assert result.getEnrollment() == 40 : "FAIL: enrollment should be 40";
        assert result.getRoomCapacity() == 100 : "FAIL: roomCapacity should be 100";
        assert result.getEffectiveLimit() == 100 : "FAIL: effectiveLimit should be 100";
        assert result.isValid() : "FAIL: should be valid";
        log("PASS", "Result fields: enrollment=40, roomCapacity=100, effectiveLimit=100, valid=true");
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
        System.out.println("  Feature 2: Add Room Capacity Validation");
        System.out.println("  Unit Tests for RoomCapacityValidator");
        System.out.println("==============================================\n");

        test_validate_enrollmentFitsRoom_returnsValid();
        test_validate_enrollmentEqualsCapacity_returnsValid();
        test_validate_enrollmentExceedsCapacity_returnsInvalid();
        test_validate_enrollmentExceedsByOne_returnsInvalid();
        test_validate_zeroRoomCapacity_returnsInvalid();
        test_validate_withRoomRatio_computesEffectiveLimit();
        test_validate_withHighRoomRatio_reducesEffectiveLimit();
        test_computeEffectiveLimit_normalValues();
        test_computeEffectiveLimit_zeroRatio_defaultsToOne();
        test_validate_resultFieldsAreCorrect();

        System.out.println("\n==============================================");
        System.out.println("  Results: " + passed + " PASSED | " + failed + " FAILED");
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }
}
