package placement;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import placement.model.Student;
import placement.service.DataStore;
import placement.ui.LoginScreen;
import placement.ui.Tabs;
import placement.ui.Theme;

public class MainApp extends Application {
    private DataStore store;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        store = new DataStore("data");
        scene = new Scene(new Pane(), 1150, 680);
        var css = getClass().getResource("/placement/style.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("Smart College Placement Preparation & Recruitment Manager");
        stage.setScene(scene);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        scene.setRoot(new LoginScreen(store, this::showStudent, this::showCoordinator));
    }

    private void showStudent(Student s) {
        show(Tabs.student(store, s), s.getName() + "  (" + s.getRollNo() + ")");
        Theme.apply(scene.getRoot(), Theme.colorOf(s));   // each student gets their own colours
    }

    private void showCoordinator() {
        show(Tabs.coordinator(store), "Placement Coordinator");
    }

    private void show(TabPane tabs, String who) {
        Label title = new Label("Smart Placement Manager");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Practice tests  |  Company eligibility  |  Performance reports");
        subtitle.getStyleClass().add("app-subtitle");
        VBox titles = new VBox(2, title, subtitle);

        Label user = new Label(who);
        user.getStyleClass().add("app-user");
        Button logout = new Button("Log out");
        logout.getStyleClass().add("logout");
        logout.setOnAction(e -> {
            try { store.save(); } catch (java.io.IOException ex) { System.err.println("Save failed: " + ex.getMessage()); }
            showLogin();
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(14, titles, spacer, user, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabs);
        scene.setRoot(root);
    }

    @Override
    public void stop() {
        try { store.save(); } catch (java.io.IOException e) { System.err.println("Save on exit failed: " + e.getMessage()); }
    }

    public static void main(String[] args) { launch(args); }
}
