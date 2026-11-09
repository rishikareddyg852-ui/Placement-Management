package placement.service;

import java.util.*;
import placement.exception.ValidationException;
import placement.model.AptitudeQuestion;
import placement.model.Question;

/** Creates fresh aptitude MCQs on the fly so a test can have any number of questions. */
public final class AptitudeGenerator {
    private static final int[] PERCENTS = {5, 10, 12, 15, 20, 25, 30, 35, 40, 45, 60, 75};
    private static final int[][] WORK = {{12, 24, 8}, {10, 15, 6}, {20, 30, 12}, {15, 30, 10}, {6, 12, 4},
            {30, 60, 20}, {18, 36, 12}, {20, 20, 10}, {24, 24, 12}, {40, 60, 24}, {45, 90, 30}, {12, 12, 6}};
    private static final int[] SPEEDS = {30, 40, 45, 50, 60, 72, 80};
    private static final int[][] COPRIME = {{2, 3}, {3, 4}, {3, 5}, {4, 5}, {2, 5}, {5, 7}, {3, 7}, {4, 9}};

    private AptitudeGenerator() {}

    /** Returns up to n new questions whose text is not in {@code avoid}. */
    public static List<Question> generate(int n, Set<String> avoid) {
        Random r = new Random();
        List<Question> out = new ArrayList<>();
        Set<String> used = new HashSet<>();
        int attempts = 0;
        while (out.size() < n && attempts++ < n * 30 + 100) {
            try {
                Question q = make(r);
                String t = q.getText();
                if ((avoid != null && avoid.contains(t)) || !used.add(t)) continue;
                out.add(q);
            } catch (ValidationException e) {
                // skip and try another
            }
        }
        return out;
    }

    private static Question make(Random r) throws ValidationException {
        switch (r.nextInt(12)) {
            case 0: {
                int p = PERCENTS[r.nextInt(PERCENTS.length)], n = 20 * (2 + r.nextInt(49));
                return mc("Percentages", "What is " + p + "% of " + n + "?", (long) p * n / 100, r);
            }
            case 1: {
                int cp = 20 * (5 + r.nextInt(96)), pr = new int[]{5, 10, 15, 20, 25, 30}[r.nextInt(6)];
                return mc("Profit & Loss", "An article bought for " + cp + " is sold at " + pr
                        + "% profit. What is the selling price?", (long) cp * (100 + pr) / 100, r);
            }
            case 2: {
                int cp = 20 * (5 + r.nextInt(96)), ls = new int[]{5, 10, 20, 25}[r.nextInt(4)];
                return mc("Profit & Loss", "An article bought for " + cp + " is sold at a " + ls
                        + "% loss. What is the selling price?", (long) cp * (100 - ls) / 100, r);
            }
            case 3: {
                int pr = 100 * (5 + r.nextInt(96)), rate = 2 + r.nextInt(11), t = 1 + r.nextInt(5);
                return mc("Simple Interest", "Find the simple interest on " + pr + " at " + rate
                        + "% per annum for " + t + " year(s).", (long) pr * rate * t / 100, r);
            }
            case 4: {
                int s = SPEEDS[r.nextInt(SPEEDS.length)], t = 2 + r.nextInt(5);
                return mc("Time & Distance", "A car travels at " + s + " km/h for " + t
                        + " hours. How many km does it cover?", (long) s * t, r);
            }
            case 5: {
                int avg = 50 + r.nextInt(40);
                int[] d = new int[5];
                int sum = 0;
                for (int i = 0; i < 4; i++) { d[i] = r.nextInt(21) - 10; sum += d[i]; }
                d[4] = -sum;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 5; i++) sb.append(avg + d[i]).append(i < 4 ? ", " : "");
                return mc("Averages", "Find the average of " + sb + ".", avg, r);
            }
            case 6: {
                int[] w = WORK[r.nextInt(WORK.length)];
                return mc("Time & Work", "A can finish a job in " + w[0] + " days and B in " + w[1]
                        + " days. Working together, how many days will they take?", w[2], r);
            }
            case 7: {
                int a = 2 + r.nextInt(30), d = 2 + r.nextInt(9);
                return mc("Series", "Next number in the series " + a + ", " + (a + d) + ", " + (a + 2 * d) + ", "
                        + (a + 3 * d) + ", ?", a + 4L * d, r);
            }
            case 8: {
                int[] rt = COPRIME[r.nextInt(COPRIME.length)];
                int k = 2 + r.nextInt(15), total = (rt[0] + rt[1]) * k;
                return mc("Ratio", "Divide " + total + " in the ratio " + rt[0] + ":" + rt[1]
                        + ". What is the larger share?", (long) Math.max(rt[0], rt[1]) * k, r);
            }
            case 9: {
                int n = 11 + r.nextInt(25);
                return mc("Number System", "What is the square of " + n + "?", (long) n * n, r);
            }
            case 10: {
                int l = 5 + r.nextInt(30), b = 3 + r.nextInt(20);
                return mc("Geometry", "Find the area of a rectangle with length " + l + " cm and breadth " + b
                        + " cm (in sq cm).", (long) l * b, r);
            }
            default: {
                int[] c = COPRIME[r.nextInt(COPRIME.length)];
                int g = 2 + r.nextInt(12);
                return mc("HCF & LCM", "Find the HCF of " + (c[0] * g) + " and " + (c[1] * g) + ".", g, r);
            }
        }
    }

    /** Builds an MCQ from a numeric answer with three distinct, plausible wrong options. */
    private static Question mc(String topic, String text, long correct, Random r) throws ValidationException {
        long step = Math.max(1, Math.abs(correct) / 10);
        LinkedHashSet<Long> opts = new LinkedHashSet<>();
        opts.add(correct);
        int guard = 0;
        while (opts.size() < 4 && guard++ < 200) {
            long off = (r.nextInt(7) - 3) * step;
            if (off == 0) continue;
            long v = correct + off;
            if (v > 0) opts.add(v);
        }
        long fill = correct + step * 4;
        while (opts.size() < 4) opts.add(fill++);
        List<Long> list = new ArrayList<>(opts);
        Collections.shuffle(list, r);
        String[] o = new String[4];
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            o[i] = String.valueOf(list.get(i));
            if (list.get(i) == correct) idx = i;
        }
        return new AptitudeQuestion(topic, text, o, idx);
    }
}
