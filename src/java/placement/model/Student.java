package placement.model;

import java.util.*;
import placement.exception.ValidationException;

public class Student extends Person {
    private static final long serialVersionUID = 1L;

    private final String branch;
    private final double cgpa, tenthPercent, twelfthPercent;
    private final int backlogs;
    private final Set<String> skills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final List<TestResult> results = new ArrayList<>();
    // Added later: old saved files do not have these, so they are null until first use.
    private String themeColor;            // hex like #3b5bdb, null = default colour for this student
    private Set<String> seenQuestions;    // texts of questions this student has already answered

    public Student(String rollNo, String name, String email, String branch,
                   double cgpa, double tenth, double twelfth, int backlogs) throws ValidationException {
        super(rollNo, name, email);
        if (branch == null || branch.isBlank()) throw new ValidationException("Branch is required.");
        if (cgpa < 0 || cgpa > 10) throw new ValidationException("CGPA must be between 0 and 10.");
        if (tenth < 0 || tenth > 100 || twelfth < 0 || twelfth > 100)
            throw new ValidationException("10th / 12th percentage must be between 0 and 100.");
        if (backlogs < 0) throw new ValidationException("Backlogs cannot be negative.");
        this.branch = branch.trim();
        this.cgpa = cgpa;
        this.tenthPercent = tenth;
        this.twelfthPercent = twelfth;
        this.backlogs = backlogs;
    }

    @Override public String getRole() { return "Student"; }
    public String getRollNo() { return getId(); }
    public String getBranch() { return branch; }
    public double getCgpa() { return cgpa; }
    public double getTenthPercent() { return tenthPercent; }
    public double getTwelfthPercent() { return twelfthPercent; }
    public int getBacklogs() { return backlogs; }

    public Set<String> getSkills() { return Collections.unmodifiableSet(skills); }
    public List<TestResult> getResults() { return Collections.unmodifiableList(results); }

    /** Replaces the skill list from a comma separated string. */
    public void setSkillsFromCsv(String csv) {
        skills.clear();
        if (csv == null) return;
        for (String s : csv.split(",")) if (!s.isBlank()) skills.add(s.trim());
    }

    public boolean hasAllSkills(Collection<String> required) { return skills.containsAll(required); }

    public void addResult(TestResult r) { results.add(r); }

    /** Colour theme chosen by this student (null = use the default for their roll number). */
    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String hex) { this.themeColor = hex; }

    /** Questions this student has already seen, so new tests prefer fresh ones. */
    public Set<String> getSeenQuestions() {
        if (seenQuestions == null) seenQuestions = new HashSet<>();
        return seenQuestions;
    }
    public void markSeen(Collection<String> questionTexts) { getSeenQuestions().addAll(questionTexts); }

    /** Number of tests taken in a category (null = all). */
    public int testCount(String category) {
        return (int) results.stream().filter(r -> category == null || r.getCategory().equals(category)).count();
    }

    public double averageScore(String category) {
        return results.stream().filter(r -> category == null || r.getCategory().equals(category))
                .mapToDouble(TestResult::getPercent).average().orElse(0);
    }

    public double bestScore() {
        return results.stream().mapToDouble(TestResult::getPercent).max().orElse(0);
    }

    @Override public String toString() { return getRollNo() + " - " + getName(); }
}
