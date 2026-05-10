package org.unitime.timetable.util;

/**
 * RoomCapacityValidator - Feature 2: Add Room Capacity Validation
 *
 * Validates whether a room can accommodate the number of enrolled students
 * before a class assignment is committed, preventing over-booking of rooms.
 */
public class RoomCapacityValidator {

    /**
     * Represents the result of a capacity validation check.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final int enrollment;
        private final int roomCapacity;
        private final int effectiveLimit;

        public ValidationResult(boolean valid, String message, int enrollment, int roomCapacity, int effectiveLimit) {
            this.valid = valid;
            this.message = message;
            this.enrollment = enrollment;
            this.roomCapacity = roomCapacity;
            this.effectiveLimit = effectiveLimit;
        }

        public boolean isValid()         { return valid; }
        public String getMessage()       { return message; }
        public int getEnrollment()       { return enrollment; }
        public int getRoomCapacity()     { return roomCapacity; }
        public int getEffectiveLimit()   { return effectiveLimit; }
    }

    /**
     * Computes the effective room limit based on room capacity and room ratio.
     * Mirrors the logic already present in Class_.getClassLimit(Assignment).
     *
     * effectiveLimit = floor(roomCapacity / roomRatio)
     */
    public static int computeEffectiveLimit(int roomCapacity, float roomRatio) {
        if (roomRatio <= 0) roomRatio = 1.0f;
        return (int) Math.floor(roomCapacity / roomRatio);
    }

    /**
     * Validates whether the enrolled students fit in the given room.
     *
     * BEFORE fix: no validation — 200 students could be assigned to a 50-seat room.
     * AFTER fix:  throws clear validation result if enrollment exceeds effectiveLimit.
     *
     * @param enrollment   number of enrolled students
     * @param roomCapacity physical capacity of the room
     * @param roomRatio    room ratio from Class_ (default 1.0)
     * @return ValidationResult indicating pass or fail with details
     */
    public static ValidationResult validate(int enrollment, int roomCapacity, float roomRatio) {
        if (roomCapacity <= 0) {
            return new ValidationResult(false,
                "Invalid room capacity: " + roomCapacity,
                enrollment, roomCapacity, 0);
        }

        int effectiveLimit = computeEffectiveLimit(roomCapacity, roomRatio);

        if (enrollment > effectiveLimit) {
            return new ValidationResult(false,
                "Room capacity exceeded: " + enrollment +
                " students enrolled but room only fits " + effectiveLimit,
                enrollment, roomCapacity, effectiveLimit);
        }

        return new ValidationResult(true,
            "Room capacity is sufficient: " + enrollment +
            " students fit in room with effective limit " + effectiveLimit,
            enrollment, roomCapacity, effectiveLimit);
    }

    /**
     * Convenience method using default roomRatio of 1.0.
     */
    public static ValidationResult validate(int enrollment, int roomCapacity) {
        return validate(enrollment, roomCapacity, 1.0f);
    }
}
