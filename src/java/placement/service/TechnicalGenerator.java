package placement.service;

import java.util.*;
import placement.exception.ValidationException;
import placement.model.Question;
import placement.model.TechnicalQuestion;

/** Creates fresh technical MCQs on the fly (number systems, Java output, data structures, loops, ...). */
public final class TechnicalGenerator {
    private static final String[] WORDS = {"program", "computer", "network", "database", "compiler", "software",
            "keyboard", "internet", "variable", "function", "algorithm", "interface", "inherit", "package",
            "thread", "memory", "binary", "student", "placement", "company"};

    private TechnicalGenerator() {}

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
        switch (r.nextInt(26)) {
            case 0: {
                int n = 5 + r.nextInt(251);
                return num("Number Systems", "What is the decimal value of the binary number "
                        + Integer.toBinaryString(n) + "?", n, r);
            }
            case 1: {
                int n = 5 + r.nextInt(250);
                String ans = Integer.toBinaryString(n);
                return str("Number Systems", "What is the binary representation of decimal " + n + "?", ans,
                        Arrays.asList(Integer.toBinaryString(n + 1), Integer.toBinaryString(n - 1),
                                Integer.toBinaryString(n + 2), Integer.toBinaryString(n - 2),
                                Integer.toBinaryString(n ^ 1), Integer.toBinaryString(n ^ 4)), r);
            }
            case 2: {
                int n = 16 + r.nextInt(240);
                return num("Number Systems", "What is the decimal value of the hexadecimal number 0x"
                        + Integer.toHexString(n).toUpperCase() + "?", n, r);
            }
            case 3: {
                int a = 2 + r.nextInt(20), b = 2 + r.nextInt(12), c = 2 + r.nextInt(9);
                return num("Java Basics", "What is the output of: int a = " + a + ", b = " + b + ", c = " + c
                        + "; System.out.println(a + b * c);", a + b * c, r);
            }
            case 4: {
                int a = 20 + r.nextInt(180), b = 3 + r.nextInt(9);
                return num("Java Basics", "What is the output of: System.out.println(" + a + " / " + b
                        + ");  (integer division)", a / b, r);
            }
            case 5: {
                int a = 20 + r.nextInt(180), b = 3 + r.nextInt(9);
                return num("Java Basics", "What is the output of: System.out.println(" + a + " % " + b + ");",
                        a % b, r);
            }
            case 6: {
                int a = 1 + r.nextInt(63), b = 1 + r.nextInt(63);
                int k = r.nextInt(3);
                String op = k == 0 ? "&" : k == 1 ? "|" : "^";
                long ans = k == 0 ? (a & b) : k == 1 ? (a | b) : (a ^ b);
                return num("Bit Manipulation", "What is the value of " + a + " " + op + " " + b + " in Java?", ans, r);
            }
            case 7: {
                int n = 1 + r.nextInt(20), k = 1 + r.nextInt(4);
                return num("Bit Manipulation", "What is the value of " + n + " << " + k + " in Java?",
                        (long) n << k, r);
            }
            case 8: {
                int n = 20 + r.nextInt(180), s = 2 + r.nextInt(6);
                return num("Loops", "for (int i = 0; i < " + n + "; i += " + s
                        + ") count++;  If count starts at 0, what is its final value?", (n + s - 1) / s, r);
            }
            case 9: {
                int n = 5 + r.nextInt(40);
                return num("Loops", "for (int i = 1; i <= " + n + "; i++) sum += i;  If sum starts at 0, "
                        + "what is its final value?", (long) n * (n + 1) / 2, r);
            }
            case 10: {
                int[] a = new int[5];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 5; i++) { a[i] = 1 + r.nextInt(50); sb.append(a[i]).append(i < 4 ? ", " : ""); }
                int k = r.nextInt(5);
                return num("Arrays", "int[] a = {" + sb + "}; What is the output of System.out.println(a[" + k
                        + "] + a.length);", a[k] + 5, r);
            }
            case 11: {
                String w = WORDS[r.nextInt(WORDS.length)];
                return num("Strings", "String s = \"" + w + "\"; What is the output of System.out.println(s.length());",
                        w.length(), r);
            }
            case 12: {
                String w = WORDS[r.nextInt(WORDS.length)];
                int k = r.nextInt(w.length());
                String ans = String.valueOf(w.charAt(k));
                List<String> wrong = new ArrayList<>();
                for (int i = 0; i < w.length(); i++) if (w.charAt(i) != w.charAt(k)) wrong.add(String.valueOf(w.charAt(i)));
                return str("Strings", "String s = \"" + w + "\"; What is the output of System.out.println(s.charAt("
                        + k + "));", ans, wrong, r);
            }
            case 13: {
                String w = WORDS[r.nextInt(WORDS.length)];
                int i = r.nextInt(3), j = i + 2 + r.nextInt(3);
                String ans = w.substring(i, j);
                return str("Strings", "String s = \"" + w + "\"; What is the output of System.out.println(s.substring("
                        + i + ", " + j + "));", ans,
                        Arrays.asList(w.substring(i, j - 1), w.substring(i + 1, j), w.substring(i, j + 1),
                                w.substring(i + 1, j + 1), w.substring(i, j - 1) + w.charAt(i)), r);
            }
            case 14: {
                int h = 2 + r.nextInt(9);
                return num("Trees", "What is the maximum number of nodes in a binary tree of height " + h
                        + "? (a tree with only a root has height 0)", (1L << (h + 1)) - 1, r);
            }
            case 15: {
                int k = 3 + r.nextInt(8);
                return num("Algorithms", "What is the maximum number of comparisons binary search needs on a sorted "
                        + "array of " + ((1 << k) - 1) + " elements?", k, r);
            }
            case 16: {
                int n = 4 + r.nextInt(30);
                return num("Graphs", "What is the maximum number of edges in a simple undirected graph with " + n
                        + " vertices?", (long) n * (n - 1) / 2, r);
            }
            case 17: {
                int n = 3 + r.nextInt(60);
                return num("Trees", "A tree has " + n + " nodes. How many edges does it have?", n - 1, r);
            }
            case 18: {
                int k = 4 + r.nextInt(3), p = 1 + r.nextInt(k - 2);
                int[] v = distinct(k, 90, r);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < k; i++) sb.append(v[i]).append(i < k - 1 ? ", " : "");
                return num("Data Structures", "Elements " + sb + " are pushed onto an empty stack in this order. "
                        + "After " + p + " pop operation(s), what is the top element?", v[k - 1 - p], r);
            }
            case 19: {
                int k = 4 + r.nextInt(3), d = 1 + r.nextInt(k - 2);
                int[] v = distinct(k, 90, r);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < k; i++) sb.append(v[i]).append(i < k - 1 ? ", " : "");
                return num("Data Structures", "Elements " + sb + " are inserted into an empty queue in this order. "
                        + "After " + d + " dequeue operation(s), what is the front element?", v[d], r);
            }
            case 20: {
                int n = 5 + r.nextInt(96);
                String[] type = {"int", "double", "long", "char"};
                int[] size = {4, 8, 8, 2};
                int t = r.nextInt(4);
                return num("Java Basics", "How many bytes do the elements of a Java " + type[t] + "[" + n
                        + "] array occupy? (ignore object header)", (long) n * size[t], r);
            }
            case 21: {
                int a = 2 + r.nextInt(15), b = 2 + r.nextInt(15);
                return num("Loops", "for (int i = 0; i < " + a + "; i++) for (int j = 0; j < " + b
                        + "; j++) count++;  How many times does count++ execute?", (long) a * b, r);
            }
            case 22: {
                int n = 3 + r.nextInt(6);
                long f = 1;
                for (int i = 2; i <= n; i++) f *= i;
                return num("Recursion", "int fact(int n) { return n <= 1 ? 1 : n * fact(n - 1); }  What does fact("
                        + n + ") return?", f, r);
            }
            case 23: {
                int n = 5 + r.nextInt(9);
                long a = 0, b = 1;
                for (int i = 0; i < n; i++) { long t = a + b; a = b; b = t; }
                return num("Recursion", "int fib(int n) { return n < 2 ? n : fib(n - 1) + fib(n - 2); }  "
                        + "What does fib(" + n + ") return?", a, r);
            }
            case 24: {
                int k = 1 + r.nextInt(20);
                char c = (char) ('A' + k);
                List<String> wrong = new ArrayList<>();
                for (int d = -3; d <= 3; d++) if (d != 0) wrong.add(String.valueOf((char) ('A' + k + d)));
                return str("Java Basics", "What is the output of: System.out.println((char) ('A' + " + k + "));",
                        String.valueOf(c), wrong, r);
            }
            default: {
                int a = 2 + r.nextInt(30);
                return num("Java Basics", "int x = " + a + "; int y = x++ + ++x;  What is the value of y?",
                        2L * a + 2, r);
            }
        }
    }

    /** k distinct values in 1..max. */
    private static int[] distinct(int k, int max, Random r) {
        LinkedHashSet<Integer> s = new LinkedHashSet<>();
        while (s.size() < k) s.add(1 + r.nextInt(max));
        int[] v = new int[k];
        int i = 0;
        for (int x : s) v[i++] = x;
        return v;
    }

    /** MCQ with a numeric answer and three distinct, plausible wrong options. */
    private static Question num(String topic, String text, long correct, Random r) throws ValidationException {
        long step = Math.max(1, Math.abs(correct) / 10);
        LinkedHashSet<Long> opts = new LinkedHashSet<>();
        opts.add(correct);
        int guard = 0;
        while (opts.size() < 4 && guard++ < 200) {
            long off = (r.nextInt(7) - 3) * step;
            if (off == 0) continue;
            long v = correct + off;
            if (v >= 0) opts.add(v);
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
        return new TechnicalQuestion(topic, text, o, idx);
    }

    /** MCQ with a text answer; wrong options are taken from the candidate list (distinct, not equal to the answer). */
    private static Question str(String topic, String text, String correct, List<String> candidates, Random r)
            throws ValidationException {
        LinkedHashSet<String> opts = new LinkedHashSet<>();
        opts.add(correct);
        List<String> pool = new ArrayList<>(candidates);
        Collections.shuffle(pool, r);
        for (String c : pool) {
            if (opts.size() >= 4) break;
            if (c != null && !c.isEmpty()) opts.add(c);
        }
        int pad = 0;
        while (opts.size() < 4) opts.add(correct + (pad++ == 0 ? "0" : "1"));
        List<String> list = new ArrayList<>(opts);
        Collections.shuffle(list, r);
        String[] o = list.toArray(new String[0]);
        return new TechnicalQuestion(topic, text, o, list.indexOf(correct));
    }
}