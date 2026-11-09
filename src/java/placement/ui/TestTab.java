package placement.ui;

import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.model.*;
import placement.service.DataStore;
import placement.service.TestSession;

class TestTab extends Tab implements Refreshable {
    private final DataStore store;
    private final Student fixed;
    private final ComboBox<Student> studentBox = new ComboBox<>();
    private final ComboBox<String> categoryBox =
            new ComboBox<>(FXCollections.observableArrayList("Aptitude", "Technical", "Mixed"));
    private final Spinner<Integer> countSpin = new Spinner<>(1, 100, 10);
    private final Label timerLabel = new Label("--:--"), progressLabel = new Label(), qLabel = new Label();
    private final ToggleGroup group = new ToggleGroup();
    private final RadioButton[] radios = new RadioButton[4];
    private final Button prev = new Button("< Previous"), next = new Button("Next >"), submit = new Button("Submit Test");
    private final TextArea resultArea = new TextArea();
    private final Label availLabel = new Label();
    private final VBox setupBox = new VBox(8);     // category / count / start + info line
    private final VBox quizBox = new VBox(10);     // the running test
    private final VBox resultBox = new VBox(12);   // the full-page result after submit

    private TestSession session;
    private Student activeStudent;
    private String activeCategory;
    private int index;

    TestTab(DataStore store, Student fixed) {
        super("Practice Test");
        this.store = store;
        this.fixed = fixed;
        setClosable(false);

        categoryBox.getSelectionModel().selectFirst();
        countSpin.setEditable(true);
        countSpin.setPrefWidth(80);
        Ui.commitOnFocusLost(countSpin);
        countSpin.valueProperty().addListener((o, a, b) -> updateAvailable());
        categoryBox.valueProperty().addListener((o, a, b) -> updateAvailable());

        Button start = new Button("Start Test");
        start.setOnAction(e -> start());
        HBox setup = new HBox(10);
        setup.setAlignment(Pos.CENTER_LEFT);
        if (fixed == null) setup.getChildren().addAll(new Label("Student:"), studentBox);
        setup.getChildren().addAll(new Label("Category:"), categoryBox, new Label("Questions:"), countSpin, start);
        setup.setPadding(new Insets(10, 10, 0, 10));

        availLabel.setStyle("-fx-padding: 0 10 0 10; -fx-text-fill: -app-muted;");
        availLabel.setWrapText(true);
        setupBox.getChildren().addAll(setup, availLabel);

        timerLabel.getStyleClass().add("timer");
        qLabel.setWrapText(true);
        qLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-padding: 4 0 6 0;");
        for (int i = 0; i < 4; i++) {
            final int opt = i;
            radios[i] = new RadioButton();
            radios[i].setToggleGroup(group);
            radios[i].setMaxWidth(Double.MAX_VALUE);
            radios[i].setWrapText(true);
            radios[i].setOnAction(e -> { if (session != null) session.answer(index, opt); });
        }
        prev.setOnAction(e -> { index--; show(); });
        next.setOnAction(e -> { index++; show(); });
        submit.setOnAction(e -> finish(false));

        HBox top = new HBox(20, progressLabel, new Label("Time left:"), timerLabel);
        quizBox.getChildren().addAll(top, qLabel, radios[0], radios[1], radios[2], radios[3],
                new HBox(10, prev, next, submit));
        quizBox.setPadding(new Insets(16));
        quizBox.getStyleClass().add("card");

        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultBox.setPadding(new Insets(18));
        resultBox.getStyleClass().add("card");

        showPanel(false, false);
        VBox root = new VBox(10, setupBox, quizBox, resultBox);
        VBox.setVgrow(resultBox, Priority.ALWAYS);
        root.setPadding(new Insets(0, 0, 8, 0));
        setContent(root);
    }

    private void start() {
        Student s = fixed != null ? fixed : studentBox.getValue();
        if (s == null) { Ui.error("Select a student first."); return; }
        String cat = categoryBox.getValue();
        int wanted = Ui.readSpinner(countSpin);       // reads what is typed, even without pressing Enter
        List<Question> pool = store.getQuestions().stream()
                .filter(q -> "Mixed".equals(cat) || q.getCategory().equals(cat))
                .collect(Collectors.toList());
        if (session != null) session.cancel();

        activeStudent = s;
        activeCategory = cat;
        // Each student gets questions they have not answered before, so students do not all see the same set.
        session = new TestSession(pool, wanted, s.getSeenQuestions(), cat);
        int got = session.getQuestions().size();
        if (got == 0) { session = null; Ui.error("Could not build a test. Add more questions to the bank."); return; }
        if (got < wanted)
            Ui.info("Only " + got + " " + cat + " questions are available, so the test has " + got
                    + " questions. Ask the coordinator to add more in the Question Bank tab.");
        index = 0;
        resultArea.clear();
        showPanel(true, false);
        show();
        session.start(
                left -> Platform.runLater(() -> timerLabel.setText(String.format("%02d:%02d", left / 60, left % 60))),
                () -> Platform.runLater(() -> finish(true)));
    }

    private void show() {
        List<Question> qs = session.getQuestions();
        Question q = qs.get(index);
        progressLabel.setText("Question " + (index + 1) + " of " + qs.size());
        qLabel.setText(q.getText());
        String[] o = q.getOptions();
        for (int i = 0; i < 4; i++) radios[i].setText((char) ('A' + i) + ")  " + o[i]);
        int a = session.getAnswer(index);
        if (a >= 0) group.selectToggle(radios[a]); else group.selectToggle(null);
        prev.setDisable(index == 0);
        next.setDisable(index == qs.size() - 1);
    }

    private void finish(boolean timeUp) {
        if (session == null) return;
        TestResult r = session.finish(activeStudent.getRollNo(), activeCategory);
        String review = session.review();
        int unanswered = session.getUnanswered();
        List<String> seenNow = session.getQuestions().stream().map(Question::getText).collect(Collectors.toList());
        session = null;
        if (r == null) { showPanel(false, false); return; }
        activeStudent.addResult(r);
        activeStudent.markSeen(seenNow);
        Ui.save(store);
        buildResult(r, unanswered, timeUp, review);
        showPanel(false, true);     // the result replaces the whole test screen
    }

    /** Builds the result page: big percentage, verdict, progress bar, stat tiles, review and buttons. */
    private void buildResult(TestResult r, int unanswered, boolean timeUp, String review) {
        double pct = r.getPercent();
        int wrong = r.getTotal() - r.getCorrect() - unanswered;
        String color = pct >= 75 ? "#2b8a3e" : pct >= 50 ? "#e67700" : "#c92a2a";
        String verdictText = pct >= 75 ? "Excellent - placement ready"
                : pct >= 50 ? "Good - keep practising" : "Needs more practice";

        Label title = new Label(activeCategory + " test result" + (timeUp ? "   (time is up, auto-submitted)" : ""));

        Label percent = new Label(String.format("%.1f%%", pct));
        percent.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label verdict = new Label(verdictText);
        verdict.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label scoreLine = new Label(r.getCorrect() + " out of " + r.getTotal() + " questions correct");
        scoreLine.setStyle("-fx-text-fill: -app-muted;");
        VBox verdictBox = new VBox(3, verdict, scoreLine);
        verdictBox.setAlignment(Pos.CENTER_LEFT);
        HBox head = new HBox(20, percent, verdictBox);
        head.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(14);

        HBox tiles = new HBox(12,
                tile(String.valueOf(r.getCorrect()), "Correct", "#2b8a3e"),
                tile(String.valueOf(wrong), "Wrong", "#c92a2a"),
                tile(String.valueOf(unanswered), "Not answered", "#7d6b74"),
                tile(String.format("%02d:%02d", r.getSeconds() / 60, r.getSeconds() % 60), "Time taken", "#4a3f45"));

        Label reviewTitle = new Label("Question review");
        reviewTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        resultArea.setText(review.startsWith("REVIEW") ? review.substring("REVIEW".length()).stripLeading() : review);

        Button again = new Button("Take another test");
        again.setOnAction(e -> { resultArea.clear(); showPanel(false, false); });
        HBox buttons = new HBox(10, again);
        if (fixed != null) {
            Button report = new Button("View my report");
            report.setOnAction(e -> openReportTab());
            buttons.getChildren().add(report);
        }

        resultBox.getChildren().setAll(title, head, bar, tiles, reviewTitle, resultArea, buttons);
        VBox.setVgrow(resultArea, Priority.ALWAYS);
        Platform.runLater(() -> { resultArea.positionCaret(0); resultArea.setScrollTop(0); });
    }

    private VBox tile(String value, String caption, String color) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label c = new Label(caption);
        c.setStyle("-fx-text-fill: -app-muted;");
        VBox t = new VBox(2, v, c);
        t.setAlignment(Pos.CENTER);
        t.setPadding(new Insets(10, 20, 10, 20));
        t.setStyle("-fx-background-color: rgba(0,0,0,0.045); -fx-background-radius: 10;");
        t.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(t, Priority.ALWAYS);
        return t;
    }

    private void openReportTab() {
        TabPane tp = getTabPane();
        if (tp == null) return;
        for (Tab t : tp.getTabs())
            if ("My Report".equals(t.getText())) { tp.getSelectionModel().select(t); return; }
    }

    /** Shows the running quiz, or the full result page, or just the setup bar (never the faded old quiz). */
    private void showPanel(boolean quiz, boolean result) {
        setOn(quizBox, quiz);
        setOn(resultBox, result);
        setOn(setupBox, !result);
    }

    private static void setOn(Region n, boolean on) {
        n.setVisible(on);
        n.setManaged(on);
    }

    private void updateAvailable() {
        String cat = categoryBox.getValue();
        if (cat == null) return;
        int n = countSpin.getValue() == null ? 10 : countSpin.getValue();
        availLabel.setText("Time is set automatically (1 min per aptitude question, 45 sec per technical question): about "
                + TestSession.formatDuration(TestSession.estimateSeconds(cat, n)) + " for " + n + " questions. "
                + "Each student gets random questions they have not seen before; when the bank runs out, new "
                + cat.toLowerCase() + " questions are generated automatically.");
    }

    @Override public void refresh() {
        updateAvailable();
        Student sel = studentBox.getValue();
        studentBox.setItems(FXCollections.observableArrayList(store.getStudents()));
        if (sel != null && store.getStudents().contains(sel)) studentBox.setValue(sel);
    }
}