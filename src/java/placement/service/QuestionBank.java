package placement.service;

import java.util.*;
import placement.exception.ValidationException;
import placement.model.*;

/** Built-in question bank. Each entry is: topic, text, correct answer, then three wrong answers. */
public final class QuestionBank {
    private QuestionBank() {}

    /** Adds every built-in question whose text is not already present. Returns how many were added. */
    public static int mergeInto(List<Question> target) {
        Set<String> have = new HashSet<>();
        for (Question q : target) have.add(q.getText().toLowerCase());
        int added = 0;
        for (Question q : all())
            if (have.add(q.getText().toLowerCase())) { target.add(q); added++; }
        return added;
    }

    public static List<Question> all() {
        List<Question> l = new ArrayList<>();
        aptitude(l);
        technical(l);
        return l;
    }

    private static void apt(List<Question> l, String topic, String text, String right, String w1, String w2, String w3) {
        try {
            String[] o = shuffled(text, right, w1, w2, w3);
            l.add(new AptitudeQuestion(topic, text, o, Arrays.asList(o).indexOf(right)));
        } catch (ValidationException e) { throw new IllegalStateException(e); }
    }

    private static void tech(List<Question> l, String topic, String text, String right, String w1, String w2, String w3) {
        try {
            String[] o = shuffled(text, right, w1, w2, w3);
            l.add(new TechnicalQuestion(topic, text, o, Arrays.asList(o).indexOf(right)));
        } catch (ValidationException e) { throw new IllegalStateException(e); }
    }

    /** Same question always gets the same option order, so the correct answer is not always "B". */
    private static String[] shuffled(String seed, String... opts) {
        List<String> list = new ArrayList<>(Arrays.asList(opts));
        Collections.shuffle(list, new Random(seed.hashCode()));
        return list.toArray(new String[0]);
    }

    private static void aptitude(List<Question> l) {
        apt(l, "Percentages", "What is 20% of 250?", "50", "40", "60", "45");
        apt(l, "Time & Work", "A can finish a job in 10 days and B in 15 days. Working together, how many days do they need?", "6", "5", "8", "12");
        apt(l, "Profit & Loss", "An item bought for 400 is sold at 25% profit. What is the selling price?", "500", "450", "520", "480");
        apt(l, "Series", "Next number in the series 2, 6, 12, 20, ?", "30", "28", "32", "26");
        apt(l, "Percentages", "A number is increased by 20% and then decreased by 20%. What is the net change?", "4% decrease", "No change", "4% increase", "2% decrease");
        apt(l, "Ratio", "If A:B = 2:3 and B:C = 4:5, then A:C = ?", "8:15", "2:5", "4:5", "10:12");
        apt(l, "Time & Distance", "A 150 m long train passes a pole in 15 seconds. What is its speed in km/h?", "36", "30", "40", "54");
        apt(l, "Averages", "What is the average of the first 10 natural numbers?", "5.5", "5", "6", "10");
        apt(l, "Probability", "A fair die is rolled once. What is the probability of getting an even number?", "1/2", "1/3", "1/6", "2/3");
        apt(l, "Probability", "Two fair coins are tossed. What is the probability of exactly two heads?", "1/4", "1/2", "1/3", "3/4");
        apt(l, "Permutations", "In how many ways can 4 people be arranged in a row?", "24", "12", "16", "8");
        apt(l, "Combinations", "What is the value of 5C2?", "10", "5", "20", "25");
        apt(l, "Number System", "What is the sum of the first 20 natural numbers?", "210", "200", "190", "220");
        apt(l, "Number System", "Which of these is a prime number?", "29", "27", "33", "39");
        apt(l, "Ages", "A father is 3 times as old as his son. After 10 years he will be twice as old. What is the son's present age?", "10", "15", "20", "12");
        apt(l, "Alligation", "In what ratio (water : milk) must water be mixed with milk costing 12 per litre to get a mixture worth 10 per litre?", "1:5", "5:1", "1:6", "2:5");
        apt(l, "Compound Interest", "What is the compound interest on 1000 for 2 years at 10% per annum?", "210", "200", "220", "1210");
        apt(l, "Time & Work", "10 men finish a job in 12 days. How many days will 15 men take?", "8", "6", "9", "10");
        apt(l, "Boats & Streams", "A boat's speed in still water is 12 km/h and the stream flows at 3 km/h. What is the downstream speed?", "15 km/h", "9 km/h", "12 km/h", "18 km/h");
        apt(l, "Series", "Next number in the series 1, 4, 9, 16, ?", "25", "20", "24", "36");
        apt(l, "Series", "Next number in the series 2, 4, 8, 16, ?", "32", "24", "30", "64");
        apt(l, "Series", "Next number in the series 3, 6, 11, 18, ?", "27", "26", "28", "25");
        apt(l, "Coding-Decoding", "If ROAD is written as SPBE, how is LION written?", "MJPO", "KHNM", "MIPO", "LJPO");
        apt(l, "Directions", "A man walks 5 km north, then 5 km east, then 5 km south. How far is he from the start?", "5 km", "10 km", "15 km", "0 km");
        apt(l, "Blood Relations", "Pointing to a girl, Ravi says, \"She is the daughter of my mother's only brother.\" How is she related to Ravi?", "Cousin", "Sister", "Niece", "Aunt");
        apt(l, "Clocks", "What is the angle between the hands of a clock at 3:00?", "90 degrees", "60 degrees", "120 degrees", "180 degrees");
        apt(l, "Calendar", "Today is Monday. What day will it be after 61 days?", "Saturday", "Friday", "Sunday", "Wednesday");
        apt(l, "Percentages", "In an exam 40% of the students failed and 300 passed. How many students appeared?", "500", "450", "600", "750");
        apt(l, "Profit & Loss", "A shopkeeper marks goods 20% above cost and gives a 10% discount. What is his profit percent?", "8%", "10%", "12%", "6%");
        apt(l, "Geometry", "What is the area of a triangle with base 10 cm and height 6 cm?", "30 sq cm", "60 sq cm", "16 sq cm", "36 sq cm");
        apt(l, "Geometry", "What is the circumference of a circle of radius 7 cm (use pi = 22/7)?", "44 cm", "22 cm", "154 cm", "49 cm");
        apt(l, "HCF & LCM", "What is the LCM of 12 and 18?", "36", "6", "72", "54");
        apt(l, "HCF & LCM", "What is the HCF of 48 and 36?", "12", "6", "24", "9");
        apt(l, "Simple Interest", "At what rate per annum does 2000 earn 400 simple interest in 2 years?", "10%", "5%", "8%", "12%");
        apt(l, "Number System", "Which number is the odd one out: 2, 3, 5, 9, 11?", "9", "2", "3", "11");
    }

    private static void technical(List<Question> l) {
        // ---- Java ----
        tech(l, "Java", "Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super");
        tech(l, "Java", "Which collection does not allow duplicate elements?", "HashSet", "ArrayList", "LinkedList", "Vector");
        tech(l, "Java", "Which keyword prevents a class from being inherited?", "final", "static", "abstract", "private");
        tech(l, "Java", "Which method is the entry point of a Java program?", "public static void main(String[] args)", "public void main()", "static void start()", "void run()");
        tech(l, "Java", "Which of these is NOT a primitive type in Java?", "String", "int", "char", "boolean");
        tech(l, "Java", "What is the default value of an int instance variable?", "0", "null", "1", "undefined");
        tech(l, "Java", "Which collection stores key-value pairs?", "HashMap", "HashSet", "ArrayList", "Stack");
        tech(l, "Java", "What does JVM stand for?", "Java Virtual Machine", "Java Variable Method", "Java Visual Mode", "Joint Virtual Memory");
        tech(l, "Java", "Which exception is thrown when an integer is divided by zero?", "ArithmeticException", "NullPointerException", "NumberFormatException", "IOException");
        tech(l, "Java", "Which block always executes, whether or not an exception occurs?", "finally", "catch", "throws", "throw");
        tech(l, "Java", "Method overloading is an example of which kind of polymorphism?", "Compile-time", "Runtime", "Dynamic binding", "Late binding");
        tech(l, "Java", "Method overriding is resolved at which time?", "Runtime", "Compile time", "Link time", "Build time");
        tech(l, "Java", "Which access modifier restricts visibility to the same class only?", "private", "protected", "public", "default");
        tech(l, "Java", "Which class is the parent of all classes in Java?", "Object", "Class", "Main", "System");
        tech(l, "Java", "Which keyword is used to create an object?", "new", "this", "class", "create");
        tech(l, "Java", "Which of these can an interface NOT have?", "Constructors", "Abstract methods", "Static methods", "Constants");
        tech(l, "Java", "Which of these classes is thread-safe?", "StringBuffer", "StringBuilder", "ArrayList", "HashMap");
        tech(l, "Java", "Which method starts the execution of a new thread?", "start()", "run()", "begin()", "execute()");
        tech(l, "Java", "Which keyword is used to implement an interface?", "implements", "extends", "inherits", "uses");
        tech(l, "Java", "What is the size of an int in Java?", "32 bits", "16 bits", "64 bits", "8 bits");
        tech(l, "Java", "Which collection keeps insertion order and allows duplicates?", "ArrayList", "HashSet", "TreeSet", "HashMap");
        tech(l, "Java", "Which keyword calls the parent class constructor?", "super", "this", "parent", "base");
        tech(l, "Java", "Strings in Java are:", "Immutable", "Mutable", "Primitive", "Always null");
        tech(l, "Java", "Which statement skips the rest of the current loop iteration?", "continue", "break", "exit", "skip");
        tech(l, "Java", "Which declaration creates a constant in Java?", "final int X = 5;", "const int X = 5;", "static X = 5;", "constant int X = 5;");
        // ---- SQL / DBMS ----
        tech(l, "SQL", "Which SQL clause filters rows after GROUP BY?", "HAVING", "WHERE", "ORDER BY", "LIMIT");
        tech(l, "SQL", "Which command removes a table along with its structure?", "DROP TABLE", "TRUNCATE", "DELETE", "REMOVE");
        tech(l, "SQL", "Which keyword removes duplicate rows from a SELECT result?", "DISTINCT", "UNIQUE", "DIFFERENT", "ONLY");
        tech(l, "SQL", "Which constraint uniquely identifies each row in a table?", "PRIMARY KEY", "FOREIGN KEY", "CHECK", "DEFAULT");
        tech(l, "SQL", "Which join returns only the matching rows from both tables?", "INNER JOIN", "LEFT JOIN", "FULL OUTER JOIN", "CROSS JOIN");
        tech(l, "SQL", "Which function returns the number of rows?", "COUNT()", "SUM()", "LEN()", "TOTAL()");
        tech(l, "SQL", "Which clause sorts the result set?", "ORDER BY", "GROUP BY", "SORT", "ARRANGE");
        tech(l, "SQL", "Which operator searches for a pattern in a column?", "LIKE", "MATCH", "IN", "BETWEEN");
        tech(l, "SQL", "Which command adds new rows to a table?", "INSERT", "ADD", "UPDATE", "APPEND");
        tech(l, "DBMS", "In ACID properties, what does the letter I stand for?", "Isolation", "Integrity", "Indexing", "Inheritance");
        tech(l, "DBMS", "Normalization is mainly used to:", "Reduce data redundancy", "Increase redundancy", "Encrypt data", "Speed up the CPU");
        // ---- DSA ----
        tech(l, "DSA", "Time complexity of binary search on a sorted array?", "O(log n)", "O(n)", "O(n log n)", "O(1)");
        tech(l, "DSA", "Which data structure follows LIFO?", "Stack", "Queue", "Array", "Tree");
        tech(l, "DSA", "Which data structure follows FIFO?", "Queue", "Stack", "Heap", "Graph");
        tech(l, "DSA", "Worst-case time complexity of bubble sort?", "O(n^2)", "O(n)", "O(log n)", "O(n log n)");
        tech(l, "DSA", "Which traversal of a binary search tree gives sorted order?", "Inorder", "Preorder", "Postorder", "Level order");
        tech(l, "DSA", "Average time to get a value from a HashMap?", "O(1)", "O(n)", "O(log n)", "O(n^2)");
        tech(l, "DSA", "Which sorting algorithm has O(n log n) worst-case time?", "Merge sort", "Quick sort", "Insertion sort", "Bubble sort");
        tech(l, "DSA", "Which data structure is used in Breadth First Search?", "Queue", "Stack", "Heap", "Set");
        tech(l, "DSA", "Which data structure is typically used for iterative Depth First Search?", "Stack", "Queue", "Hash table", "Array of queues");
        tech(l, "DSA", "Inserting a node at the head of a singly linked list takes:", "O(1)", "O(n)", "O(log n)", "O(n^2)");
        tech(l, "DSA", "Binary search requires the array to be:", "Sorted", "Unsorted", "Of even size", "Of distinct elements only");
        // ---- OOP / OS / Networks / Web / misc ----
        tech(l, "OOP", "Wrapping data and methods into a single unit is called:", "Encapsulation", "Inheritance", "Polymorphism", "Compilation");
        tech(l, "OOP", "Hiding implementation details and showing only essential features is called:", "Abstraction", "Overloading", "Serialization", "Recursion");
        tech(l, "OS", "Which scheduling algorithm gives the minimum average waiting time?", "Shortest Job First", "FCFS", "Round Robin", "Priority with aging");
        tech(l, "OS", "Which of these is a necessary condition for deadlock?", "Circular wait", "Preemption", "Unlimited memory", "Multiple CPUs");
        tech(l, "Networks", "Which OSI layer is responsible for routing?", "Network layer", "Transport layer", "Data link layer", "Session layer");
        tech(l, "Networks", "What is the default port of HTTP?", "80", "21", "25", "443");
        tech(l, "Networks", "What is the default port of HTTPS?", "443", "80", "8080", "22");
        tech(l, "Networks", "Which protocol is connection-oriented?", "TCP", "UDP", "IP", "ICMP");
        tech(l, "Python", "Which of these is immutable in Python?", "tuple", "list", "dict", "set");
        tech(l, "Python", "Which keyword defines a function in Python?", "def", "function", "fun", "define");
        tech(l, "Web", "Which HTML tag creates a hyperlink?", "<a>", "<link>", "<href>", "<p>");
        tech(l, "Web", "Which CSS property changes the text colour?", "color", "font-color", "text-color", "foreground");
        tech(l, "Database", "Which of these is a NoSQL database?", "MongoDB", "MySQL", "PostgreSQL", "Oracle");
        tech(l, "Git", "Which Git command saves staged changes to the local repository?", "git commit", "git push", "git pull", "git clone");
    }
}
