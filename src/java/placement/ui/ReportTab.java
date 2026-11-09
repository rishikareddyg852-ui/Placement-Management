package placement.ui;

import java.io.File;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.model.Student;
import placement.service.DataStore;
import placement.service.ReportService;

/** fixed == null: coordinator view (any student + summary). fixed != null: that student's own report only. */
class ReportTab extends Tab implements Refreshable {
    private final DataStore store;
    private final Student fixed;
    private final ComboBox<Student> studentBox = new ComboBox<>();
    private final TextArea output = new TextArea();

    ReportTab(DataStore store, Student fixed) {
        super(fixed == null ? "Reports" : "My Report");
        this.store = store;
        this.fixed = fixed;
        setClosable(false);

        HBox top = new HBox(10);
        top.setPadding(new Insets(10));
        if (fixed == null) {
            Button studentReport = new Button("Student Report");
            Button placementReport = new Button("Placement Summary (all students)");
            studentReport.setOnAction(e -> {
                Student s = studentBox.getValue();
                if (s == null) { Ui.error("Select a student."); return; }
                output.setText(ReportService.studentReport(s, store.getCompanies()));
            });
            placementReport.setOnAction(e ->
                    output.setText(ReportService.placementReport(store.getStudents(), store.getCompanies())));
            top.getChildren().addAll(new Label("Student:"), studentBox, studentReport, placementReport);
        } else {
            Button regenerate = new Button("Refresh report");
            regenerate.setOnAction(e -> showMine());
            top.getChildren().add(regenerate);
        }
        Button saveBtn = new Button("Save to File...");
        saveBtn.setOnAction(e -> save());
        top.getChildren().add(saveBtn);

        output.setEditable(false);
        output.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");
        VBox root = new VBox(10, top, output);
        VBox.setVgrow(output, Priority.ALWAYS);
        setContent(root);
    }

    private void showMine() {
        output.setText(ReportService.studentReport(fixed, store.getCompanies()));
    }

    private void save() {
        if (output.getText().isBlank()) { Ui.error("Generate a report first."); return; }
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("report.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File f = fc.showSaveDialog(getTabPane().getScene().getWindow());
        if (f == null) return;
        try {
            ReportService.saveToFile(output.getText(), f.toPath());
            Ui.info("Report saved to " + f.getAbsolutePath());
        } catch (IOException ex) {
            Ui.error("Could not save report: " + ex.getMessage());
        }
    }

    @Override public void refresh() {
        if (fixed != null) { showMine(); return; }
        Student sel = studentBox.getValue();
        studentBox.setItems(FXCollections.observableArrayList(store.getStudents()));
        if (sel != null && store.getStudents().contains(sel)) studentBox.setValue(sel);
    }
}
