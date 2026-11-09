package placement.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.exception.ValidationException;
import placement.model.*;
import placement.service.DataStore;

class QuestionTab extends Tab implements Refreshable {
    private final DataStore store;
    private final TableView<Question> table = new TableView<>();
    private final ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Aptitude", "Technical"));
    private final ComboBox<String> correctBox = new ComboBox<>(FXCollections.observableArrayList("A", "B", "C", "D"));
    private final TextField topic = new TextField();
    private final TextArea text = new TextArea();
    private final TextField[] opts = {new TextField(), new TextField(), new TextField(), new TextField()};

    QuestionTab(DataStore store) {
        super("Question Bank");
        this.store = store;
        setClosable(false);

        typeBox.getSelectionModel().selectFirst();
        text.setPrefRowCount(3);
        text.setWrapText(true);
        topic.setPromptText("e.g. Percentages, Java, SQL");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("Type"), typeBox);
        form.addRow(1, new Label("Topic"), topic);
        form.addRow(2, new Label("Question"), text);
        for (int i = 0; i < 4; i++) form.addRow(3 + i, new Label("Option " + (char) ('A' + i)), opts[i]);
        form.addRow(7, new Label("Correct"), correctBox);

        Button add = new Button("Add");
        Button update = new Button("Update Selected");
        Button delete = new Button("Delete Selected");
        delete.getStyleClass().add("danger");
        add.setOnAction(e -> add());
        update.setOnAction(e -> update());
        delete.setOnAction(e -> delete());

        table.getColumns().add(Ui.<Question>col("Category", Question::getCategory));
        table.getColumns().add(Ui.<Question>col("Topic", Question::getTopic));
        table.getColumns().add(Ui.<Question>col("Question", Question::getText));
        table.getColumns().add(Ui.<Question>col("Answer", Question::getCorrectOption));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, q) -> { if (q != null) load(q); });

        VBox left = new VBox(10, new Label("Add / Edit MCQ"), form, new HBox(8, add, update, delete));
        left.setPadding(new Insets(10));
        left.getStyleClass().add("card");
        left.setPrefWidth(380);
        HBox root = new HBox(10, left, table);
        root.setPadding(new Insets(6));
        HBox.setHgrow(table, Priority.ALWAYS);
        setContent(root);
    }

    private void load(Question q) {
        typeBox.setValue(q.getCategory());
        topic.setText(q.getTopic());
        text.setText(q.getText());
        String[] o = q.getOptions();
        for (int i = 0; i < 4; i++) opts[i].setText(o[i]);
        correctBox.getSelectionModel().select(q.getCorrectIndex());
    }

    private Question build() throws ValidationException {
        String[] o = new String[4];
        for (int i = 0; i < 4; i++) o[i] = opts[i].getText();
        int correct = correctBox.getSelectionModel().getSelectedIndex();
        if ("Aptitude".equals(typeBox.getValue()))
            return new AptitudeQuestion(topic.getText(), text.getText(), o, correct);
        return new TechnicalQuestion(topic.getText(), text.getText(), o, correct);
    }

    private void add() {
        try {
            store.getQuestions().add(build());
            Ui.save(store);
            clear();
            refresh();
        } catch (ValidationException ex) {
            Ui.error(ex.getMessage());
        }
    }

    private void update() {
        int idx = table.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Ui.error("Select a question in the table first."); return; }
        try {
            store.getQuestions().set(idx, build());
            Ui.save(store);
            refresh();
        } catch (ValidationException ex) {
            Ui.error(ex.getMessage());
        }
    }

    private void delete() {
        int idx = table.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Ui.error("Select a question to delete."); return; }
        store.getQuestions().remove(idx);
        Ui.save(store);
        refresh();
    }

    private void clear() {
        topic.clear();
        text.clear();
        for (TextField f : opts) f.clear();
        correctBox.getSelectionModel().clearSelection();
    }

    @Override public void refresh() {
        table.setItems(FXCollections.observableArrayList(store.getQuestions()));
    }
}
