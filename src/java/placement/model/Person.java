package placement.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import placement.exception.ValidationException;

/** Base class for people in the system (inheritance: Person -> Student). */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final String id;
    private String name;
    private String email;
    private String passwordHash;   // null = account created by coordinator, password is the ID until changed

    protected Person(String id, String name, String email) throws ValidationException {
        if (id == null || id.isBlank()) throw new ValidationException("Roll number / ID is required.");
        if (name == null || name.isBlank()) throw new ValidationException("Name is required.");
        if (email == null || !EMAIL.matcher(email.trim()).matches())
            throw new ValidationException("Enter a valid email address.");
        this.id = id.trim();
        this.name = name.trim();
        this.email = email.trim();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public abstract String getRole();

    public void setPassword(String password) throws ValidationException {
        if (password == null || password.length() < 4)
            throw new ValidationException("Password must be at least 4 characters.");
        this.passwordHash = hash(password);
    }

    public boolean checkPassword(String password) {
        if (password == null) return false;
        if (passwordHash == null) return id.equalsIgnoreCase(password.trim());
        return passwordHash.equals(hash(password));
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest((id.toLowerCase() + ":" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
