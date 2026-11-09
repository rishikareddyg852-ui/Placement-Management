package placement.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.exception.ValidationException;
import placement.model.Student;
import placement.service.DataStore;

class StudentTab extends Tab implements Refreshable {
    private final DataStore store;
    private final TableView<Student> table = new TableView<>();
    private final TextField roll = new TextField(), name = new TextField(), email = new TextField(),
            branch = new TextField(), cgpa = new TextField(), tenth = new TextField(),
            twelfth = new TextField(), backlogs = new TextField("0"), skills = new TextField();

    StudentTab(DataStore store) {
        super("Students");
        this.store = store;
        setClosable(false);

        branch.setPromptText("e.g. IT, CSE");
        skills.setPromptText("Java, Python, SQL");

        String[] labels = {"Roll No", "Name", "Email", "Branch", "CGPA", "10th %", "12th %", "Backlogs", "Skills (comma separated)"};
        TextField[] fields = {roll, name, email, branch, cgpa, tenth, twelfth, backlogs, skills};
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(fields[i], 1, i);
        }

        Button register = new Button("Register");
        Button updateSkills = new Button("Update Skills");
        Button delete = new Button("Delete");
        delete.getStyleClass().add("danger");
        register.setOnAction(e -> register());
        updateSkills.setOnAction(e -> updateSkills());
        delete.setOnAction(e -> delete());

        table.getColumns().add(Ui.<Student>col("Roll No", Student::getRollNo));
        table.getColumns().add(Ui.<Student>col("Name", Student::getName));
        table.getColumns().add(Ui.<Student>col("Branch", Student::getBranch));
        table.getColumns().add(Ui.<Student>col("CGPA", s -> String.format("%.2f", s.getCgpa())));
        table.getColumns().add(Ui.<Student>col("Backlogs", s -> String.valueOf(s.getBacklogs())));
        table.getColumns().add(Ui.<Student>col("Skills", s -> String.join(", ", s.getSkills())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) skills.setText(String.join(", ", s.getSkills()));
        });

        VBox left = new VBox(10, new Label("Student Profile"), form, new HBox(8, register, updateSkills, delete));
        left.setPadding(new Insets(10));
        left.getStyleClass().add("card");
        HBox root = new HBox(10, left, table);
        root.setPadding(new Insets(6));
        HBox.setHgrow(table, Priority.ALWAYS);
        setContent(root);
    }

    private void register() {
        try {
            Student s = new Student(roll.getText(), name.getText(), email.getText(), branch.getText(),
                    Ui.num(cgpa, "CGPA"), Ui.num(tenth, "10th %"), Ui.num(twelfth, "12th %"),
                    Ui.integer(backlogs, "Backlogs"));
            s.setSkillsFromCsv(skills.getText());
            store.addStudent(s);
            Ui.save(store);
            for (TextField f : new TextField[]{roll, name, email, branch, cgpa, tenth, twelfth, skills}) f.clear();
            backlogs.setText("0");
            refresh();
        } catch (ValidationException ex) {
            Ui.error(ex.getMessage());
        }
    }

    private void updateSkills() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) { Ui.error("Select a student in the table first."); return; }
        s.setSkillsFromCsv(skills.getText());
        Ui.save(store);
        refresh();
    }

    private void delete() {
        Student s = table.getSelectionModel().getSelectedItem();
        if (s == null) { Ui.error("Select a student to delete."); return; }
        store.getStudents().remove(s);
        Ui.save(store);
        refresh();
    }

    @Override public void refresh() {
        table.setItems(FXCollections.observableArrayList(store.getStudents()));
    }
}
