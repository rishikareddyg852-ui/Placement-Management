package placement.service;

import placement.exception.ValidationException;
import placement.model.Student;

/** Simple login rules. Change COORDINATOR_PASSWORD to your own. */
public final class AuthService {
    private static final String COORDINATOR_PASSWORD = "admin123";

    private AuthService() {}

    public static boolean coordinatorLogin(String password) {
        return COORDINATOR_PASSWORD.equals(password);
    }

    public static Student studentLogin(DataStore store, String roll, String password) throws ValidationException {
        if (roll == null || roll.isBlank()) throw new ValidationException("Enter your roll number.");
        Student s = store.findStudent(roll);
        if (s == null || !s.checkPassword(password)) throw new ValidationException("Invalid roll number or password.");
        return s;
    }
}
