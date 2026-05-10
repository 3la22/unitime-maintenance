package org.unitime.timetable.model;

import org.unitime.timetable.util.ExamConflictDetector;
import org.unitime.timetable.util.ExamConflictDetector.Exam;
import org.unitime.timetable.util.ExamConflictDetector.ConflictReport;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit Tests for Feature 3: Add Exam Conflict Detection
 *
 * Tests the ExamConflictDetector utility class that prevents students
 * from having overlapping exams in UniTime.
 *
 * Run independently — no framework needed, uses plain Java assertions.
 */
public class ExamConflictDetectorTest {

    static int passed = 0;
    static int failed = 0;

    // ─────────────────────────────────────────────
    // Test 1: Two non-overlapping exams → no conflict
    // BEFORE fix: no detection at all
    // AFTER fix:  correctly returns empty conflict list
    // ─────────────────────────────────────────────
    static void test_noConflict_nonOverlappingExams() {
        Exam e1 = new Exam("E1", "Math Final", "S1", 0, 10);
        Exam e2 = new Exam("E2", "Physics Final", "S1", 10, 20);
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1",
                Arrays.asList(e1, e2));
        assert conflicts.isEmpty() : "FAIL: non-overlapping exams should have no conflict";
        log("PASS", "Non-overlapping exams (0-10, 10-20) → no conflict");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 2: Two fully overlapping exams → conflict detected
    // BEFORE fix: student assigned to both exams at same time silently
    // AFTER fix:  conflict detected with CRITICAL severity
    // ─────────────────────────────────────────────
    static void test_conflict_fullyOverlappingExams() {
        Exam e1 = new Exam("E1", "Math Final", "S1", 0, 10);
        Exam e2 = new Exam("E2", "Chemistry Final", "S1", 0, 10);
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1",
                Arrays.asList(e1, e2));
        assert conflicts.size() == 1 : "FAIL: expected 1 conflict, got " + conflicts.size();
        assert conflicts.get(0).getSeverity().equals("CRITICAL") : "FAIL: severity should be CRITICAL";
        log("PASS", "Fully overlapping exams (0-10, 0-10) → 1 CRITICAL conflict");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 3: Partially overlapping exams → conflict detected
    // ─────────────────────────────────────────────
    static void test_conflict_partiallyOverlappingExams() {
        Exam e1 = new Exam("E1", "Math Final", "S1", 0, 15);
        Exam e2 = new Exam("E2", "Physics Final", "S1", 10, 20);
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1",
                Arrays.asList(e1, e2));
        assert conflicts.size() == 1 : "FAIL: expected 1 conflict for partial overlap";
        log("PASS", "Partially overlapping exams (0-15, 10-20) → 1 conflict detected");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 4: periodsOverlap — adjacent slots → no overlap
    // ─────────────────────────────────────────────
    static void test_periodsOverlap_adjacentSlots_returnsFalse() {
        boolean result = ExamConflictDetector.periodsOverlap(0, 10, 10, 20);
        assert !result : "FAIL: adjacent slots should NOT overlap";
        log("PASS", "periodsOverlap(0-10, 10-20) → false (adjacent, not overlapping)");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 5: periodsOverlap — same slot → overlap
    // ─────────────────────────────────────────────
    static void test_periodsOverlap_sameSlot_returnsTrue() {
        boolean result = ExamConflictDetector.periodsOverlap(5, 15, 5, 15);
        assert result : "FAIL: same slots should overlap";
        log("PASS", "periodsOverlap(5-15, 5-15) → true (same slot)");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 6: single exam → no conflict possible
    // ─────────────────────────────────────────────
    static void test_singleExam_noConflict() {
        Exam e1 = new Exam("E1", "Math Final", "S1", 0, 10);
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1",
                Arrays.asList(e1));
        assert conflicts.isEmpty() : "FAIL: single exam cannot have conflict";
        log("PASS", "Single exam → no conflict possible");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 7: null exam list → no conflict
    // ─────────────────────────────────────────────
    static void test_nullExamList_noConflict() {
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1", null);
        assert conflicts.isEmpty() : "FAIL: null list should return empty conflicts";
        log("PASS", "Null exam list → no conflict (safe handling)");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 8: 3 exams, 2 conflict → detects both
    // ─────────────────────────────────────────────
    static void test_threeExams_twoConflicts() {
        Exam e1 = new Exam("E1", "Math", "S1", 0, 10);
        Exam e2 = new Exam("E2", "Physics", "S1", 5, 15);   // overlaps e1
        Exam e3 = new Exam("E3", "Chemistry", "S1", 7, 12); // overlaps e1 and e2
        List<ConflictReport> conflicts = ExamConflictDetector.detectConflictsForStudent("S1",
                Arrays.asList(e1, e2, e3));
        assert conflicts.size() == 3 : "FAIL: expected 3 conflicts, got " + conflicts.size();
        log("PASS", "3 overlapping exams → 3 conflicts detected");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 9: detectAllConflicts — two students, only one has conflict
    // ─────────────────────────────────────────────
    static void test_detectAllConflicts_onlyOneStudentHasConflict() {
        List<Exam> allExams = new ArrayList<>();
        // Student 1: conflict
        allExams.add(new Exam("E1", "Math", "S1", 0, 10));
        allExams.add(new Exam("E2", "Physics", "S1", 5, 15));
        // Student 2: no conflict
        allExams.add(new Exam("E3", "Biology", "S2", 0, 10));
        allExams.add(new Exam("E4", "History", "S2", 10, 20));

        List<ConflictReport> conflicts = ExamConflictDetector.detectAllConflicts(allExams);
        assert conflicts.size() == 1 : "FAIL: expected 1 conflict, got " + conflicts.size();
        assert conflicts.get(0).getStudentId().equals("S1") : "FAIL: conflict should be for S1";
        log("PASS", "2 students, only S1 has conflict → 1 conflict detected for S1");
        passed++;
    }

    // ─────────────────────────────────────────────
    // Test 10: hasConflicts — returns true when conflict exists
    // ─────────────────────────────────────────────
    static void test_hasConflicts_returnsTrue() {
        List<Exam> allExams = new ArrayList<>();
        allExams.add(new Exam("E1", "Math", "S1", 0, 10));
        allExams.add(new Exam("E2", "Physics", "S1", 5, 15));
        assert ExamConflictDetector.hasConflicts(allExams) : "FAIL: should detect conflict";
        log("PASS", "hasConflicts returns true when overlapping exams exist");
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
        System.out.println("  Feature 3: Add Exam Conflict Detection");
        System.out.println("  Unit Tests for ExamConflictDetector");
        System.out.println("==============================================\n");

        test_noConflict_nonOverlappingExams();
        test_conflict_fullyOverlappingExams();
        test_conflict_partiallyOverlappingExams();
        test_periodsOverlap_adjacentSlots_returnsFalse();
        test_periodsOverlap_sameSlot_returnsTrue();
        test_singleExam_noConflict();
        test_nullExamList_noConflict();
        test_threeExams_twoConflicts();
        test_detectAllConflicts_onlyOneStudentHasConflict();
        test_hasConflicts_returnsTrue();

        System.out.println("\n==============================================");
        System.out.println("  Results: " + passed + " PASSED | " + failed + " FAILED");
        System.out.println("==============================================");

        if (failed > 0) System.exit(1);
    }
}
