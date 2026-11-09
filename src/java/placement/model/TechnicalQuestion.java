package placement.model;

import placement.exception.ValidationException;

public class TechnicalQuestion extends Question {
    private static final long serialVersionUID = 1L;
    public TechnicalQuestion(String topic, String text, String[] options, int correct) throws ValidationException {
        super(topic, text, options, correct);
    }
    @Override public String getCategory() { return "Technical"; }
}
