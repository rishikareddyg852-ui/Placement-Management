package placement.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TestResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String studentId, category;
    private final int total, correct, seconds;
    private final LocalDateTime takenAt = LocalDateTime.now();

    public TestResult(String studentId, String category, int total, int correct, int seconds) {
        this.studentId = studentId;
        this.category = category;
        this.total = total;
        this.correct = correct;
        this.seconds = seconds;
    }

    public String getStudentId() { return studentId; }
    public String getCategory() { return category; }
    public int getTotal() { return total; }
    public int getCorrect() { return correct; }
    public int getSeconds() { return seconds; }
    public LocalDateTime getTakenAt() { return takenAt; }
    public double getPercent() { return total == 0 ? 0 : correct * 100.0 / total; }

    public String summary() {
        return String.format("Category: %s%nScore: %d / %d  (%.1f%%)%nTime taken: %02d:%02d",
                category, correct, total, getPercent(), seconds / 60, seconds % 60);
    }
}
