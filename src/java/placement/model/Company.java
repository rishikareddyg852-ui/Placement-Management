package placement.model;

import java.io.Serializable;
import java.util.*;
import placement.exception.ValidationException;

public class Company implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name, role, ctc;
    private final double minCgpa, minTenth, minTwelfth;
    private final int maxBacklogs;
    private final Set<String> branches = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> requiredSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public Company(String name, String role, String ctc, double minCgpa, double minTenth, double minTwelfth,
                   int maxBacklogs, Collection<String> branches, Collection<String> skills) throws ValidationException {
        if (name == null || name.isBlank()) throw new ValidationException("Company name is required.");
        if (minCgpa < 0 || minCgpa > 10) throw new ValidationException("Min CGPA must be between 0 and 10.");
        if (minTenth < 0 || minTenth > 100 || minTwelfth < 0 || minTwelfth > 100)
            throw new ValidationException("Min percentages must be between 0 and 100.");
        if (maxBacklogs < 0) throw new ValidationException("Max backlogs cannot be negative.");
        this.name = name.trim();
        this.role = role == null ? "" : role.trim();
        this.ctc = ctc == null ? "" : ctc.trim();
        this.minCgpa = minCgpa;
        this.minTenth = minTenth;
        this.minTwelfth = minTwelfth;
        this.maxBacklogs = maxBacklogs;
        this.branches.addAll(branches);
        this.requiredSkills.addAll(skills);
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public String getCtc() { return ctc; }
    public double getMinCgpa() { return minCgpa; }
    public double getMinTenth() { return minTenth; }
    public double getMinTwelfth() { return minTwelfth; }
    public int getMaxBacklogs() { return maxBacklogs; }
    public Set<String> getBranches() { return Collections.unmodifiableSet(branches); }
    public Set<String> getRequiredSkills() { return Collections.unmodifiableSet(requiredSkills); }

    /** An empty branch list means all branches are allowed. */
    public boolean allowsBranch(String branch) { return branches.isEmpty() || branches.contains(branch); }

    @Override public String toString() { return role.isEmpty() ? name : name + " - " + role; }
}
