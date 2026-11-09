package placement.service;

import java.util.*;
import java.util.stream.Collectors;
import placement.model.*;

public final class EligibilityService {
    private EligibilityService() {}

    /** Returns the list of unmet criteria. Empty list = eligible. */
    public static List<String> check(Student s, Company c) {
        List<String> r = new ArrayList<>();
        if (s.getCgpa() < c.getMinCgpa())
            r.add(String.format("CGPA %.2f < %.2f", s.getCgpa(), c.getMinCgpa()));
        if (s.getTenthPercent() < c.getMinTenth())
            r.add(String.format("10th %.1f%% < %.1f%%", s.getTenthPercent(), c.getMinTenth()));
        if (s.getTwelfthPercent() < c.getMinTwelfth())
            r.add(String.format("12th %.1f%% < %.1f%%", s.getTwelfthPercent(), c.getMinTwelfth()));
        if (s.getBacklogs() > c.getMaxBacklogs())
            r.add("Backlogs " + s.getBacklogs() + " > " + c.getMaxBacklogs());
        if (!c.allowsBranch(s.getBranch()))
            r.add("Branch " + s.getBranch() + " not allowed");
        Set<String> missing = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        missing.addAll(c.getRequiredSkills());
        missing.removeAll(s.getSkills());
        if (!missing.isEmpty()) r.add("Missing skills: " + String.join(", ", missing));
        return r;
    }

    public static boolean isEligible(Student s, Company c) { return check(s, c).isEmpty(); }

    public static List<Student> eligibleStudents(Company c, Collection<Student> all) {
        return all.stream().filter(s -> isEligible(s, c)).collect(Collectors.toList());
    }

    public static List<Company> eligibleCompanies(Student s, Collection<Company> all) {
        return all.stream().filter(c -> isEligible(s, c)).collect(Collectors.toList());
    }
}
