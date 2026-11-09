package placement.ui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import placement.model.Student;

/**
 * Gives every student their own colour theme. The stylesheet uses the looked-up colours
 * -app-primary, -app-dark, -app-bg ... and this class overrides them on the scene root.
 */
public final class Theme {
    /** Nice default colours; a student gets one based on their roll number until they pick their own. */
    public static final String[] PALETTE = {
            "#7a5fc0",  // lavender (default look)
            "#5f6ec9",  // periwinkle
            "#a063b8",  // orchid
            "#86489a",  // plum
            "#9372cc",  // lilac
            "#c0527a",  // rose
            "#3b8f9b",  // teal
            "#4f9a78"   // sage
    };

    private Theme() {}

    /** The colour for this student: their saved choice, or a default picked from their roll number. */
    public static String colorOf(Student s) {
        if (s.getThemeColor() != null && !s.getThemeColor().isBlank()) return s.getThemeColor();
        int i = Math.abs(s.getRollNo().toLowerCase().hashCode() % PALETTE.length);
        return PALETTE[i];
    }

    /** Re-colours the whole app below {@code root} (call with scene.getRoot()). */
    public static void apply(Node root, String hex) {
        if (root == null) return;
        Color c;
        try {
            c = Color.web(hex);
        } catch (RuntimeException e) {
            c = Color.web(PALETTE[0]);
        }
        // Keep every pick soft and professional: muted saturation, readable with white text.
        Color primary = Color.hsb(c.getHue(), Math.min(Math.max(c.getSaturation(), 0.38), 0.62),
                Math.min(Math.max(c.getBrightness(), 0.55), 0.78));
        Color dark = Color.hsb(primary.getHue(), Math.min(1.0, primary.getSaturation() + 0.1),
                primary.getBrightness() * 0.45);
        root.setStyle(String.join(";",
                "-app-primary:" + toHex(primary),
                "-app-dark:" + toHex(dark),
                "-app-bg:" + toHex(primary.interpolate(Color.WHITE, 0.93)),
                "-app-bg2:" + toHex(primary.interpolate(Color.WHITE, 0.965)),
                "-app-tint2:" + toHex(primary.interpolate(Color.WHITE, 0.87)),
                "-app-tint3:" + toHex(primary.interpolate(Color.WHITE, 0.76)),
                "-app-border:" + toHex(primary.interpolate(Color.WHITE, 0.71))));
    }

    /** Back to the default (coordinator / login) look. */
    public static void reset(Node root) {
        if (root != null) root.setStyle("");
    }

    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
    }
}
