package placement.ui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import placement.exception.ValidationException;
import placement.model.Student;
import placement.model.TestResult;
import placement.service.DataStore;

/** A student's own profile, skills, password and test history. */
class MyProfileTab extends Tab implements Refreshable {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final Student me;
    private final Label stats = new Label();
    private final TableView<TestResult> table = new TableView<>();

    MyProfileTab(DataStore store, Student me) {
        super("My Profile");
        this.me = me;
        setClosable(false);

        Label details = new Label(String.format(
                "Name: %s%nRoll No: %s%nEmail: %s%nBranch: %s%nCGPA: %.2f%n10th / 12th: %.1f%% / %.1f%%%nBacklogs: %d",
                me.getName(), me.getRollNo(), me.getEmail(), me.getBranch(), me.getCgpa(),
                me.getTenthPercent(), me.getTwelfthPercent(), me.getBacklogs()));

        TextField skills = new TextField(String.join(", ", me.getSkills()));
        skills.setPromptText("Java, Python, SQL");
        Button saveSkills = new Button("Update skills");
        saveSkills.setOnAction(e -> {
            me.setSkillsFromCsv(skills.getText());
            Ui.save(store);
            Ui.info("Skills updated.");
        });

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");
        Button changePass = new Button("Change password");
        changePass.setOnAction(e -> {
            try {
                me.setPassword(newPass.getText());
                Ui.save(store);
                newPass.clear();
                Ui.info("Password changed.");
            } catch (ValidationException ex) {
                Ui.error(ex.getMessage());
            }
        });

        // Avatar with the student's initials, and a colour chooser (each student has their own look).
        Label avatar = new Label(initials(me.getName()));
        avatar.getStyleClass().add("avatar");
        HBox swatches = new HBox(6);
        for (String hex : Theme.PALETTE) {
            Button sw = new Button();
            sw.setMinSize(26, 26);
            sw.setMaxSize(26, 26);
            sw.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 13; -fx-padding: 0;");
            sw.setOnAction(e -> setTheme(store, hex));
            swatches.getChildren().add(sw);
        }
        ColorPicker picker = new ColorPicker(Color.web(Theme.colorOf(me)));
        picker.setOnAction(e -> setTheme(store, Theme.toHex(picker.getValue())));

        VBox left = new VBox(10, new Label("My profile"), avatar, details, new Label("My colour"), swatches, picker,
                new Label("Skills (comma separated)"), skills, saveSkills,
                new Label("Change password"), newPass, changePass);
        left.getStyleClass().add("card");
        left.setPrefWidth(340);
        left.setMinWidth(340);

        table.getColumns().add(Ui.<TestResult>col("Date", r -> r.getTakenAt().format(FMT)));
        table.getColumns().add(Ui.<TestResult>col("Category", TestResult::getCategory));
        table.getColumns().add(Ui.<TestResult>col("Score", r -> r.getCorrect() + " / " + r.getTotal()));
        table.getColumns().add(Ui.<TestResult>col("Percent", r -> String.format("%.1f%%", r.getPercent())));
        table.getColumns().add(Ui.<TestResult>col("Time", r -> String.format("%02d:%02d", r.getSeconds() / 60, r.getSeconds() % 60)));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox statsCard = new VBox(8, new Label("My performance"), stats);
        statsCard.getStyleClass().add("card");
        VBox right = new VBox(10, statsCard, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox root = new HBox(10, left, right);
        root.setPadding(new Insets(6));
        HBox.setHgrow(right, Priority.ALWAYS);
        setContent(root);
    }

    private void setTheme(DataStore store, String hex) {
        me.setThemeColor(hex);
        Ui.save(store);
        if (getTabPane() != null && getTabPane().getScene() != null)
            Theme.apply(getTabPane().getScene().getRoot(), hex);
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+"))
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        return sb.length() == 0 ? "?" : sb.toString();
    }

    @Override public void refresh() {
        List<TestResult> list = new ArrayList<>(me.getResults());
        Collections.reverse(list);
        table.setItems(FXCollections.observableArrayList(list));
        if (me.testCount(null) == 0) {
            stats.setText("No tests taken yet. Open the Practice Test tab to start.");
        } else {
            StringBuilder sb = new StringBuilder(String.format("Tests taken: %d     Average: %.1f%%     Best: %.1f%%",
                    me.testCount(null), me.averageScore(null), me.bestScore()));
            for (String cat : new String[]{"Aptitude", "Technical", "Mixed"})
                if (me.testCount(cat) > 0)
                    sb.append(String.format("%n%s average: %.1f%% (%d tests)", cat, me.averageScore(cat), me.testCount(cat)));
            stats.setText(sb.toString());
        }
    }
}
