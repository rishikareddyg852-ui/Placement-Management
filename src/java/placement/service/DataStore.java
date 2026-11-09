package placement.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import placement.exception.ValidationException;
import placement.model.*;

/** Holds all application data and persists it to files (Java serialization). */
public class DataStore {
    private final Path dir;
    private final List<Student> students;
    private final List<Question> questions;
    private final List<Company> companies;

    public DataStore(String folder) {
        dir = Paths.get(folder);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Could not create data folder: " + e.getMessage());
        }
        students = read("students.dat");
        questions = read("questions.dat");
        companies = read("companies.dat");
        boolean freshInstall = questions.isEmpty() && companies.isEmpty();
        if (freshInstall) seedCompanies();

        // Merge the big built-in question bank once (a flag file stops deleted questions from coming back).
        Path flag = dir.resolve("bank-v2.flag");
        if (freshInstall || !Files.exists(flag)) {
            QuestionBank.mergeInto(questions);
            try {
                Files.writeString(flag, "question bank merged");
                save();
            } catch (IOException e) {
                System.err.println("Could not save question bank: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> read(String file) {
        Path p = dir.resolve(file);
        if (!Files.exists(p)) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(p)))) {
            return (List<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void write(String file, List<?> data) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(dir.resolve(file))))) {
            out.writeObject(new ArrayList<>(data));
        }
    }

    public synchronized void save() throws IOException {
        write("students.dat", students);
        write("questions.dat", questions);
        write("companies.dat", companies);
    }

    public List<Student> getStudents() { return students; }
    public List<Question> getQuestions() { return questions; }
    public List<Company> getCompanies() { return companies; }

    public Student findStudent(String roll) {
        if (roll == null) return null;
        return students.stream().filter(s -> s.getRollNo().equalsIgnoreCase(roll.trim())).findFirst().orElse(null);
    }

    public void addStudent(Student s) throws ValidationException {
        boolean exists = students.stream().anyMatch(x -> x.getRollNo().equalsIgnoreCase(s.getRollNo()));
        if (exists) throw new ValidationException("A student with roll number " + s.getRollNo() + " already exists.");
        students.add(s);
    }

    public void addCompany(Company c) throws ValidationException {
        boolean exists = companies.stream().anyMatch(x -> x.toString().equalsIgnoreCase(c.toString()));
        if (exists) throw new ValidationException("This company/role is already added.");
        companies.add(c);
    }

    /** Sample companies so the app is usable on first launch (questions come from QuestionBank). */
    private void seedCompanies() {
        try {
            companies.add(new Company("TechNova (sample)", "Software Engineer", "6 LPA", 7.0, 60, 60, 0,
                    List.of("IT", "CSE"), List.of("Java")));
            companies.add(new Company("DataWorks (sample)", "Data Analyst", "5 LPA", 6.5, 60, 60, 1,
                    List.of(), List.of("SQL")));
            companies.add(new Company("CloudSpark (sample)", "Cloud Associate", "8 LPA", 7.5, 65, 65, 0,
                    List.of("IT", "CSE"), List.of("Python")));
        } catch (ValidationException e) {
            System.err.println("Seed data invalid: " + e.getMessage());
        }
    }
}
