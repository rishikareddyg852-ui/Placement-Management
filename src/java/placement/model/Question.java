package placement.model;

import java.io.Serializable;
import placement.exception.ValidationException;

/** Abstract MCQ. Subclasses define the category (polymorphism). */
public abstract class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String topic, text;
    private final String[] options;
    private final int correctIndex;

    protected Question(String topic, String text, String[] options, int correctIndex) throws ValidationException {
        if (text == null || text.isBlank()) throw new ValidationException("Question text is required.");
        if (options == null || options.length != 4) throw new ValidationException("Exactly 4 options are required.");
        String[] clean = new String[4];
        for (int i = 0; i < 4; i++) {
            if (options[i] == null || options[i].isBlank())
                throw new ValidationException("All four options must be filled in.");
            clean[i] = options[i].trim();
        }
        if (correctIndex < 0 || correctIndex > 3) throw new ValidationException("Select the correct option.");
        this.topic = (topic == null || topic.isBlank()) ? "General" : topic.trim();
        this.text = text.trim();
        this.options = clean;
        this.correctIndex = correctIndex;
    }

    public abstract String getCategory();

    public String getTopic() { return topic; }
    public String getText() { return text; }
    public String[] getOptions() { return options.clone(); }
    public int getCorrectIndex() { return correctIndex; }
    public String getCorrectOption() { return options[correctIndex]; }
    public boolean isCorrect(int index) { return index == correctIndex; }
}
