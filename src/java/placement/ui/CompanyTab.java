package placement.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import placement.exception.ValidationException;
import placement.model.Company;
import placement.service.DataStore;

class CompanyTab extends Tab implements Refreshable {
    private final DataStore store;
    private final TableView<Company> table = new TableView<>();
    private final TextField name = new TextField(), role = new TextField(), ctc = new TextField(),
            minCgpa = new TextField(), min10 = new TextField(), min12 = new TextField(),
            maxBack = new TextField("0"), branches = new TextField(), skills = new TextField();

    CompanyTab(DataStore store) {
        super("Companies");
        this.store = store;
        setClosable(false);

        branches.setPromptText("IT, CSE (blank = all)");
        skills.setPromptText("Java, SQL");
        ctc.setPromptText("e.g. 6 LPA");

        String[] labels = {"Company", "Role", "CTC", "Min CGPA", "Min 10th %", "Min 12th %",
                "Max backlogs", "Allowed branches", "Required skills"};
        TextField[] fields = {name, role, ctc, minCgpa, min10, min12, maxBack, branches, skills};
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(fields[i], 1, i);
        }

        Button add = new Button("Add Company");
        Button delete = new Button("Delete Selected");
        delete.getStyleClass().add("danger");
        add.setOnAction(e -> add());
        delete.setOnAction(e -> delete());

        table.getColumns().add(Ui.<Company>col("Company", Company::getName));
        table.getColumns().add(Ui.<Company>col("Role", Company::getRole));
        table.getColumns().add(Ui.<Company>col("CTC", Company::getCtc));
        table.getColumns().add(Ui.<Company>col("Min CGPA", c -> String.valueOf(c.getMinCgpa())));
        table.getColumns().add(Ui.<Company>col("10th/12th", c -> c.getMinTenth() + " / " + c.getMinTwelfth()));
        table.getColumns().add(Ui.<Company>col("Max Backlogs", c -> String.valueOf(c.getMaxBacklogs())));
        table.getColumns().add(Ui.<Company>col("Branches", c -> c.getBranches().isEmpty() ? "All" : String.join(", ", c.getBranches())));
        table.getColumns().add(Ui.<Company>col("Skills", c -> String.join(", ", c.getRequiredSkills())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox left = new VBox(10, new Label("Company & Eligibility Criteria"), form, new HBox(8, add, delete));
        left.setPadding(new Insets(10));
        left.getStyleClass().add("card");
        HBox root = new HBox(10, left, table);
        root.setPadding(new Insets(6));
        HBox.setHgrow(table, Priority.ALWAYS);
        setContent(root);
    }

    private void add() {
        try {
            Company c = new Company(name.getText(), role.getText(), ctc.getText(),
                    Ui.num(minCgpa, "Min CGPA"), Ui.num(min10, "Min 10th %"), Ui.num(min12, "Min 12th %"),
                    Ui.integer(maxBack, "Max backlogs"), Ui.csv(branches.getText()), Ui.csv(skills.getText()));
            store.addCompany(c);
            Ui.save(store);
            for (TextField f : new TextField[]{name, role, ctc, minCgpa, min10, min12, branches, skills}) f.clear();
            maxBack.setText("0");
            refresh();
        } catch (ValidationException ex) {
            Ui.error(ex.getMessage());
        }
    }

    private void delete() {
        Company c = table.getSelectionModel().getSelectedItem();
        if (c == null) { Ui.error("Select a company to delete."); return; }
        store.getCompanies().remove(c);
        Ui.save(store);
        refresh();
    }

    @Override public void refresh() {
        table.setItems(FXCollections.observableArrayList(store.getCompanies()));
    }
}
