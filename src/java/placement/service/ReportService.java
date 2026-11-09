package placement.service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import placement.model.*;

public final class ReportService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final String LINE = "=".repeat(70);

    private ReportService() {}

    public static String studentReport(Student s, List<Company> companies) {
        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT PERFORMANCE & ELIGIBILITY REPORT\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(FMT)).append('\n').append(LINE).append('\n');
        sb.append(String.format("Name     : %s (%s)%n", s.getName(), s.getRollNo()));
        sb.append(String.format("Branch   : %s    CGPA: %.2f    Backlogs: %d%n", s.getBranch(), s.getCgpa(), s.getBacklogs()));
        sb.append(String.format("10th/12th: %.1f%% / %.1f%%%n", s.getTenthPercent(), s.getTwelfthPercent()));
        sb.append("Skills   : ").append(s.getSkills().isEmpty() ? "-" : String.join(", ", s.getSkills())).append("\n\n");

        sb.append("PRACTICE TEST PERFORMANCE\n");
        if (s.testCount(null) == 0) {
            sb.append("No tests taken yet.\n");
        } else {
            sb.append(String.format("Tests taken: %d   Average: %.1f%%   Best: %.1f%%%n",
                    s.testCount(null), s.averageScore(null), s.bestScore()));
            for (String cat : new String[]{"Aptitude", "Technical", "Mixed"})
                if (s.testCount(cat) > 0)
                    sb.append(String.format("  %-10s %d test(s), average %.1f%%%n", cat, s.testCount(cat), s.averageScore(cat)));
            sb.append("\nHistory:\n");
            for (TestResult r : s.getResults())
                sb.append(String.format("  %s | %-9s | %d/%d | %5.1f%% | %02d:%02d%n", r.getTakenAt().format(FMT),
                        r.getCategory(), r.getCorrect(), r.getTotal(), r.getPercent(), r.getSeconds() / 60, r.getSeconds() % 60));
            sb.append("\nReadiness: ").append(readiness(s)).append('\n');
        }

        sb.append("\nPLACEMENT ELIGIBILITY\n");
        for (Company c : companies) {
            List<String> why = EligibilityService.check(s, c);
            sb.append(String.format("  %-32s %s%n", c, why.isEmpty() ? "ELIGIBLE" : "Not eligible: " + String.join("; ", why)));
        }
        return sb.toString();
    }

    public static String placementReport(List<Student> students, List<Company> companies) {
        StringBuilder sb = new StringBuilder();
        sb.append("PLACEMENT ELIGIBILITY SUMMARY\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(FMT)).append('\n').append(LINE).append('\n');
        for (Company c : companies) {
            List<Student> ok = EligibilityService.eligibleStudents(c, students);
            sb.append(String.format("%n%s  [%s]  min CGPA %.1f, max backlogs %d%n", c, c.getCtc(), c.getMinCgpa(), c.getMaxBacklogs()));
            sb.append("  Eligible: ").append(ok.size()).append(" of ").append(students.size()).append('\n');
            if (!ok.isEmpty())
                sb.append("  ").append(ok.stream().map(Student::toString).collect(Collectors.joining(", "))).append('\n');
        }
        sb.append('\n').append(LINE).append("\nSTUDENT PERFORMANCE RANKING\n");
        sb.append(String.format("%-4s %-28s %-6s %-6s %-8s %-8s%n", "#", "Student", "CGPA", "Tests", "Avg %", "Eligible"));
        List<Student> ranked = students.stream()
                .sorted(Comparator.comparingDouble((Student s) -> s.averageScore(null)).reversed())
                .collect(Collectors.toList());
        int rank = 1;
        for (Student s : ranked)
            sb.append(String.format("%-4d %-28s %-6.2f %-6d %-8.1f %-8d%n", rank++, s, s.getCgpa(), s.testCount(null),
                    s.averageScore(null), EligibilityService.eligibleCompanies(s, companies).size()));
        return sb.toString();
    }

    private static String readiness(Student s) {
        double avg = s.averageScore(null);
        if (avg >= 75) return "Placement ready";
        if (avg >= 50) return "Improving - keep practising";
        return "Needs more practice";
    }

    public static void saveToFile(String text, Path path) throws IOException {
        Files.writeString(path, text);
    }
}
