package placement.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.model.*;
import placement.service.DataStore;
import placement.service.EligibilityService;

class EligibilityTab extends Tab implements Refreshable {
    private record Row(Student student, List<String> reasons) {}

    private final DataStore store;
    private final ComboBox<Company> companyBox = new ComboBox<>();
    private final CheckBox onlyEligible = new CheckBox("Show only eligible");
    private final Label summary = new Label();
    private final TableView<Row> table = new TableView<>();

    EligibilityTab(DataStore store) {
        super("Eligibility");
        this.store = store;
        setClosable(false);

        Button check = new Button("Check Eligibility");
        check.setOnAction(e -> check());
        HBox top = new HBox(10, new Label("Company:"), companyBox, onlyEligible, check, summary);
        top.setPadding(new Insets(10));

        table.getColumns().add(Ui.<Row>col("Roll No", r -> r.student().getRollNo()));
        table.getColumns().add(Ui.<Row>col("Name", r -> r.student().getName()));
        table.getColumns().add(Ui.<Row>col("Branch", r -> r.student().getBranch()));
        table.getColumns().add(Ui.<Row>col("CGPA", r -> String.format("%.2f", r.student().getCgpa())));
        table.getColumns().add(Ui.<Row>col("Status", r -> r.reasons().isEmpty() ? "ELIGIBLE" : "Not eligible"));
        table.getColumns().add(Ui.<Row>col("Reasons", r -> String.join("; ", r.reasons())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox root = new VBox(10, top, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(root);
    }

    private void check() {
        Company c = companyBox.getValue();
        if (c == null) { Ui.error("Select a company."); return; }
        List<Row> rows = new ArrayList<>();
        int eligible = 0;
        for (Student s : store.getStudents()) {
            List<String> why = EligibilityService.check(s, c);
            if (why.isEmpty()) eligible++;
            if (!onlyEligible.isSelected() || why.isEmpty()) rows.add(new Row(s, why));
        }
        table.setItems(FXCollections.observableArrayList(rows));
        summary.setText(eligible + " of " + store.getStudents().size() + " students eligible");
    }

    @Override public void refresh() {
        Company sel = companyBox.getValue();
        companyBox.setItems(FXCollections.observableArrayList(store.getCompanies()));
        if (sel != null && store.getCompanies().contains(sel)) companyBox.setValue(sel);
    }
}
