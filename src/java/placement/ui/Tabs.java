package placement.ui;

import java.util.List;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import placement.model.Student;
import placement.service.DataStore;

/** Builds the tab set for each role; refreshes a tab's data whenever it is selected. */
public final class Tabs {
    private Tabs() {}

    /** Coordinator: sees all students, question bank, companies, eligibility and reports. */
    public static TabPane coordinator(DataStore store) {
        return wrap(List.of(new StudentTab(store), new QuestionTab(store), new CompanyTab(store),
                new EligibilityTab(store), new ReportTab(store, null)));
    }

    /** Student: sees only their own profile, tests, eligibility and report. */
    public static TabPane student(DataStore store, Student me) {
        return wrap(List.of(new MyProfileTab(store, me), new TestTab(store, me),
                new MyEligibilityTab(store, me), new ReportTab(store, me)));
    }

    private static TabPane wrap(List<Tab> tabs) {
        TabPane pane = new TabPane();
        pane.getTabs().addAll(tabs);
        pane.getSelectionModel().selectedItemProperty().addListener((o, a, t) -> {
            if (t instanceof Refreshable r) r.refresh();
        });
        tabs.forEach(t -> ((Refreshable) t).refresh());
        return pane;
    }
}
