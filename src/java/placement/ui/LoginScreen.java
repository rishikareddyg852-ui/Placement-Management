package placement.ui;

import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.exception.ValidationException;
import placement.model.Student;
import placement.service.AuthService;
import placement.service.DataStore;

public class LoginScreen extends StackPane {

    public LoginScreen(DataStore store, Consumer<Student> onStudent, Runnable onCoordinator) {
        getStyleClass().add("login-bg");

        Label title = new Label("Smart Placement Manager");
        title.getStyleClass().add("login-title");
        Label sub = new Label("Sign in to continue");
        sub.getStyleClass().add("login-sub");

        ToggleGroup group = new ToggleGroup();
        ToggleButton studentBtn = new ToggleButton("Student");
        ToggleButton coordBtn = new ToggleButton("Coordinator");
        studentBtn.setToggleGroup(group);
        coordBtn.setToggleGroup(group);
        studentBtn.setSelected(true);
        group.selectedToggleProperty().addListener((o, a, n) -> { if (n == null && a != null) a.setSelected(true); });
        HBox roles = new HBox(8, studentBtn, coordBtn);
        roles.setAlignment(Pos.CENTER);

        TextField roll = new TextField();
        roll.setPromptText("Roll number");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);

        Button login = new Button("Log in");
        login.setDefaultButton(true);
        login.setMaxWidth(Double.MAX_VALUE);
        Hyperlink register = new Hyperlink("New student? Create an account");

        studentBtn.selectedProperty().addListener((o, was, isStudent) -> {
            roll.setVisible(isStudent);
            roll.setManaged(isStudent);
            register.setVisible(isStudent);
            register.setManaged(isStudent);
            error.setText("");
        });

        login.setOnAction(e -> {
            try {
                if (studentBtn.isSelected()) {
                    onStudent.accept(AuthService.studentLogin(store, roll.getText(), pass.getText()));
                } else if (AuthService.coordinatorLogin(pass.getText())) {
                    onCoordinator.run();
                } else {
                    throw new ValidationException("Incorrect coordinator password.");
                }
            } catch (ValidationException ex) {
                error.setText(ex.getMessage());
            }
        });

        register.setOnAction(e -> {
            Student s = registerDialog(store);
            if (s != null) {
                roll.setText(s.getRollNo());
                pass.clear();
                error.setText("");
                Ui.info("Account created. Log in with your roll number and password.");
            }
        });

        VBox card = new VBox(12, title, sub, roles, roll, pass, error, login, register);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(380);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        getChildren().add(card);
    }

    private static Student registerDialog(DataStore store) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Create student account");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        String css = Ui.css();
        if (css != null) dlg.getDialogPane().getStylesheets().add(css);

        TextField roll = new TextField(), name = new TextField(), email = new TextField(),
                branch = new TextField(), cgpa = new TextField(), tenth = new TextField(),
                twelfth = new TextField(), backlogs = new TextField("0"), skills = new TextField();
        PasswordField pass = new PasswordField();
        branch.setPromptText("e.g. IT, CSE");
        skills.setPromptText("Java, Python, SQL");

        String[] labels = {"Roll No", "Name", "Email", "Branch", "CGPA", "10th %", "12th %", "Backlogs",
                "Skills (comma separated)", "Password (min 4 chars)"};
        Control[] fields = {roll, name, email, branch, cgpa, tenth, twelfth, backlogs, skills, pass};
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(fields[i], 1, i);
        }
        Label error = new Label();
        error.getStyleClass().add("error");
        VBox content = new VBox(10, form, error);
        content.setStyle("-fx-padding: 10;");
        dlg.getDialogPane().setContent(content);

        Student[] created = new Student[1];
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                Student s = new Student(roll.getText(), name.getText(), email.getText(), branch.getText(),
                        Ui.num(cgpa, "CGPA"), Ui.num(tenth, "10th %"), Ui.num(twelfth, "12th %"),
                        Ui.integer(backlogs, "Backlogs"));
                s.setSkillsFromCsv(skills.getText());
                s.setPassword(pass.getText());
                store.addStudent(s);
                Ui.save(store);
                created[0] = s;
            } catch (ValidationException ex) {
                error.setText(ex.getMessage());
                ev.consume();
            }
        });
        dlg.showAndWait();
        return created[0];
    }
}
