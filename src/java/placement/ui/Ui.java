package placement.ui;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import placement.exception.ValidationException;
import placement.service.DataStore;

/** Small helpers shared by all tabs. */
final class Ui {
    private Ui() {}

    static void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        String css = css();
        if (css != null) a.getDialogPane().getStylesheets().add(css);
        a.showAndWait();
    }

    static String css() {
        var url = Ui.class.getResource("/placement/style.css");
        return url == null ? null : url.toExternalForm();
    }

    static void error(String msg) { alert(Alert.AlertType.ERROR, msg); }
    static void info(String msg) { alert(Alert.AlertType.INFORMATION, msg); }

    static void save(DataStore store) {
        try {
            store.save();
        } catch (IOException e) {
            error("Could not save data: " + e.getMessage());
        }
    }

    static double num(TextField f, String label) throws ValidationException {
        try {
            return Double.parseDouble(f.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a number.");
        }
    }

    static int integer(TextField f, String label) throws ValidationException {
        try {
            return Integer.parseInt(f.getText().trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a whole number.");
        }
    }

    static List<String> csv(String text) {
        List<String> out = new ArrayList<>();
        if (text != null)
            for (String s : text.split(",")) if (!s.isBlank()) out.add(s.trim());
        return out;
    }

    static <T> TableColumn<T, String> col(String title, Function<T, String> f) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
        return c;
    }

    /** Makes a typed value in an editable Spinner count even if the user does not press Enter. */
    static void commitOnFocusLost(Spinner<Integer> sp) {
        sp.focusedProperty().addListener((o, was, is) -> { if (!is) readSpinner(sp); });
    }

    /** Reads what is currently typed in the spinner (falls back to its value if the text is not a number). */
    static int readSpinner(Spinner<Integer> sp) {
        try {
            int v = Integer.parseInt(sp.getEditor().getText().trim());
            if (sp.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory f)
                v = Math.max(f.getMin(), Math.min(f.getMax(), v));
            sp.getValueFactory().setValue(v);
            return v;
        } catch (NumberFormatException e) {
            sp.getEditor().setText(String.valueOf(sp.getValue()));
            return sp.getValue();
        }
    }
}
