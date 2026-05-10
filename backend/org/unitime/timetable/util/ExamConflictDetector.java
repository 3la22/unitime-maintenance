package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ExamConflictDetector - Feature 3: Add Exam Conflict Detection
 *
 * Detects when a student is enrolled in two exams scheduled at the same time.
 * This prevents students from having overlapping exams before the schedule
 * is finalized, improving fairness and reducing manual conflict resolution.
 *
 * BEFORE fix: No automatic detection — conflicts only discovered after schedule
 *             was finalized, requiring manual resolution.
 * AFTER fix:  Conflicts detected before finalizing, with detailed report.
 */
public class ExamConflictDetector {

    /**
     * Represents a single exam with a time slot.
     */
    public static class Exam {
        private final String examId;
        private final String examName;
        private final String studentId;
        private final int startSlot;
        private final int endSlot;

        public Exam(String examId, String examName, String studentId, int startSlot, int endSlot) {
            this.examId = examId;
            this.examName = examName;
            this.studentId = studentId;
            this.startSlot = startSlot;
            this.endSlot = endSlot;
        }

        public String getExamId()    { return examId; }
        public String getExamName()  { return examName; }
        public String getStudentId() { return studentId; }
        public int getStartSlot()    { return startSlot; }
        public int getEndSlot()      { return endSlot; }
    }

    /**
     * Represents a conflict report between two overlapping exams.
     */
    public static class ConflictReport {
        private final String studentId;
        private final Exam exam1;
        private final Exam exam2;
        private final String severity;

        public ConflictReport(String studentId, Exam exam1, Exam exam2, String severity) {
            this.studentId = studentId;
            this.exam1 = exam1;
            this.exam2 = exam2;
            this.severity = severity;
        }

        public String getStudentId() { return studentId; }
        public Exam getExam1()       { return exam1; }
        public Exam getExam2()       { return exam2; }
        public String getSeverity()  { return severity; }

        public String toAlertMessage() {
            return "[" + severity + "] Student " + studentId +
                   " has a conflict between '" + exam1.getExamName() +
                   "' (slots " + exam1.getStartSlot() + "-" + exam1.getEndSlot() + ")" +
                   " and '" + exam2.getExamName() +
                   "' (slots " + exam2.getStartSlot() + "-" + exam2.getEndSlot() + ")";
        }
    }

    /**
     * Checks whether two exam periods overlap.
     * Two exams overlap if one starts before the other ends.
     */
    public static boolean periodsOverlap(int start1, int end1, int start2, int end2) {
        return start1 < end2 && start2 < end1;
    }

    /**
     * Detects all direct conflicts for a given student's exams.
     * A direct conflict = two exams with overlapping time slots.
     *
     * @param exams list of exams for a single student
     * @return list of ConflictReports (empty if no conflicts)
     */
    public static List<ConflictReport> detectConflictsForStudent(String studentId, List<Exam> exams) {
        List<ConflictReport> conflicts = new ArrayList<>();
        if (exams == null || exams.size() < 2) return conflicts;

        for (int i = 0; i < exams.size(); i++) {
            for (int j = i + 1; j < exams.size(); j++) {
                Exam e1 = exams.get(i);
                Exam e2 = exams.get(j);
                if (periodsOverlap(e1.getStartSlot(), e1.getEndSlot(),
                                   e2.getStartSlot(), e2.getEndSlot())) {
                    conflicts.add(new ConflictReport(studentId, e1, e2, "CRITICAL"));
                }
            }
        }
        return conflicts;
    }

    /**
     * Detects conflicts across all students' exams.
     *
     * @param allExams list of all exams (each with studentId)
     * @return list of all ConflictReports found
     */
    public static List<ConflictReport> detectAllConflicts(List<Exam> allExams) {
        List<ConflictReport> allConflicts = new ArrayList<>();
        if (allExams == null || allExams.isEmpty()) return allConflicts;

        // Group exams by student
        java.util.Map<String, List<Exam>> byStudent = new java.util.HashMap<>();
        for (Exam exam : allExams) {
            byStudent.computeIfAbsent(exam.getStudentId(), k -> new ArrayList<>()).add(exam);
        }

        // Check each student's exams for conflicts
        for (java.util.Map.Entry<String, List<Exam>> entry : byStudent.entrySet()) {
            allConflicts.addAll(detectConflictsForStudent(entry.getKey(), entry.getValue()));
        }

        return allConflicts;
    }

    /**
     * Returns true if any conflicts exist in the given exam list.
     */
    public static boolean hasConflicts(List<Exam> allExams) {
        return !detectAllConflicts(allExams).isEmpty();
    }
}
