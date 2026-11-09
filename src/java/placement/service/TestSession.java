package placement.service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.IntConsumer;
import placement.model.*;

/** One timed practice test. The countdown runs on a background thread. */
public class TestSession {
    /** Time allowed per question; the total test time is worked out from the questions chosen. */
    public static final int APTITUDE_SECONDS = 60;
    public static final int TECHNICAL_SECONDS = 45;

    private final List<Question> questions;
    private final int[] answers;
    private final int durationSec;
    private final AtomicInteger remaining = new AtomicInteger();
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private long startMillis;

    /**
     * @param seen             texts of questions this student already answered (fresh ones are picked first)
     * @param category         Aptitude / Technical / Mixed: new questions of that kind are generated automatically
     *                         when the bank has no fresh questions left for this student
     */
    public TestSession(List<Question> pool, int count, Set<String> seen, String category) {
        List<Question> fresh = new ArrayList<>(), old = new ArrayList<>();
        for (Question q : pool) (seen != null && seen.contains(q.getText()) ? old : fresh).add(q);
        Collections.shuffle(fresh);
        Collections.shuffle(old);

        List<Question> chosen = new ArrayList<>(fresh.subList(0, Math.min(count, fresh.size())));
        if (chosen.size() < count && category != null) {
            int need = count - chosen.size();
            Set<String> avoid = new HashSet<>();
            if (seen != null) avoid.addAll(seen);
            for (Question q : chosen) avoid.add(q.getText());
            if ("Technical".equals(category)) {
                chosen.addAll(TechnicalGenerator.generate(need, avoid));
            } else if ("Aptitude".equals(category)) {
                chosen.addAll(AptitudeGenerator.generate(need, avoid));
            } else {
                int t = need / 2;
                chosen.addAll(TechnicalGenerator.generate(t, avoid));
                chosen.addAll(AptitudeGenerator.generate(need - t, avoid));
            }
        }
        if (chosen.size() < count)                      // last resort: repeat questions already seen
            chosen.addAll(old.subList(0, Math.min(count - chosen.size(), old.size())));
        Collections.shuffle(chosen);

        this.questions = chosen;
        this.answers = new int[questions.size()];
        Arrays.fill(answers, -1);
        int total = 0;
        for (Question q : questions) total += secondsFor(q);
        this.durationSec = Math.max(60, total);   // time is automatic: depends on the questions in this test
        this.remaining.set(durationSec);
    }

    /** Seconds allowed for one question (technical questions are quicker than aptitude ones). */
    public static int secondsFor(Question q) {
        return "Technical".equals(q.getCategory()) ? TECHNICAL_SECONDS : APTITUDE_SECONDS;
    }

    /** Estimated total time for a test of this category and size (Mixed is split about half and half). */
    public static int estimateSeconds(String category, int count) {
        if ("Technical".equals(category)) return count * TECHNICAL_SECONDS;
        if ("Aptitude".equals(category)) return count * APTITUDE_SECONDS;
        int t = count / 2;
        return t * TECHNICAL_SECONDS + (count - t) * APTITUDE_SECONDS;
    }

    public static String formatDuration(int seconds) {
        int m = seconds / 60, s = seconds % 60;
        return s == 0 ? m + " min" : m + " min " + s + " sec";
    }

    public int getDurationSec() { return durationSec; }

    public int getUnanswered() {
        int n = 0;
        for (int a : answers) if (a < 0) n++;
        return n;
    }

    public List<Question> getQuestions() { return questions; }
    public int getAnswer(int i) { return answers[i]; }
    public void answer(int i, int option) { answers[i] = option; }

    /** Starts the countdown thread. Callbacks run on the timer thread (use Platform.runLater in the UI). */
    public void start(IntConsumer onTick, Runnable onTimeUp) {
        startMillis = System.currentTimeMillis();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "test-timer");
            t.setDaemon(true);
            return t;
        });
        onTick.accept(durationSec);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int left = remaining.decrementAndGet();
                onTick.accept(Math.max(left, 0));
                if (left <= 0 && !finished.get()) {
                    scheduler.shutdown();
                    onTimeUp.run();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /** Stops the timer without producing a result (used when abandoning a test). */
    public void cancel() {
        finished.set(true);
        if (scheduler != null) scheduler.shutdownNow();
    }

    /** Scores the test. Returns null if it was already finished. */
    public TestResult finish(String studentId, String category) {
        if (!finished.compareAndSet(false, true)) return null;
        if (scheduler != null) scheduler.shutdownNow();
        int correct = 0;
        for (int i = 0; i < questions.size(); i++)
            if (answers[i] >= 0 && questions.get(i).isCorrect(answers[i])) correct++;
        int secs = (int) Math.min((System.currentTimeMillis() - startMillis) / 1000, durationSec);
        return new TestResult(studentId, category, questions.size(), correct, secs);
    }

    public String review() {
        StringBuilder sb = new StringBuilder("REVIEW\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int a = answers[i];
            boolean ok = a >= 0 && q.isCorrect(a);
            sb.append(i + 1).append(". ").append(ok ? "[Correct] " : "[Wrong] ").append(q.getText()).append('\n');
            if (!ok) {
                sb.append("   Your answer: ").append(a < 0 ? "(not answered)" : (char) ('A' + a) + ") " + q.getOptions()[a]).append('\n');
                sb.append("   Correct answer: ").append((char) ('A' + q.getCorrectIndex())).append(") ").append(q.getCorrectOption()).append('\n');
            }
        }
        return sb.toString();
    }
}