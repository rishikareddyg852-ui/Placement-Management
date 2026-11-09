package placement.exception;

/** Thrown when user input or domain data is invalid. */
public class ValidationException extends Exception {
    public ValidationException(String message) { super(message); }
}
