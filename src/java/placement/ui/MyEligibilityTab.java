package placement.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.model.Company;
import placement.model.Student;
import placement.service.DataStore;
import placement.service.EligibilityService;

/** Shows one student which companies they qualify for, and why not for the rest. */
class MyEligibilityTab extends Tab implements Refreshable {
    private record Row(Company company, List<String> reasons) {}

    private final DataStore store;
    private final Student me;
    private final Label summary = new Label();
    private final TableView<Row> table = new TableView<>();

    MyEligibilityTab(DataStore store, Student me) {
        super("My Eligibility");
        this.store = store;
        this.me = me;
        setClosable(false);

        table.getColumns().add(Ui.<Row>col("Company", r -> r.company().getName()));
        table.getColumns().add(Ui.<Row>col("Role", r -> r.company().getRole()));
        table.getColumns().add(Ui.<Row>col("CTC", r -> r.company().getCtc()));
        table.getColumns().add(Ui.<Row>col("Status", r -> r.reasons().isEmpty() ? "ELIGIBLE" : "Not eligible"));
        table.getColumns().add(Ui.<Row>col("What is missing", r -> String.join("; ", r.reasons())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        summary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e2a78;");
        VBox root = new VBox(10, summary, table);
        root.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(root);
    }

    @Override public void refresh() {
        List<Row> rows = new ArrayList<>();
        int ok = 0;
        for (Company c : store.getCompanies()) {
            List<String> why = EligibilityService.check(me, c);
            if (why.isEmpty()) ok++;
            rows.add(new Row(c, why));
        }
        table.setItems(FXCollections.observableArrayList(rows));
        summary.setText("You are eligible for " + ok + " of " + store.getCompanies().size() + " companies");
    }
}
