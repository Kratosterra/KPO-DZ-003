
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class RandomStudent {
    static int numOfStudents = 35;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Welcome to the random student selector! Type /h for help.");
        createTables();
        while (true) {
            System.out.print("> ");
            input = scanner.nextLine();

            if (input.equals("/h")) {
                System.out.println("Type /r to select a random student, /g to generate students, /l to list all students with their grades.");
            } else if (input.equals("/r")) {
                String name = selectRandomStudent();
                if (name == null) {
                    System.out.println("There are no students who haven't responded yet.");
                } else {
                    System.out.println("The selected student is: " + name);
                    System.out.print("Did the student attend the seminar? (Y/N) ");
                    input = scanner.nextLine();
                    boolean attended = input.equalsIgnoreCase("Y");
                    System.out.print("Enter the student's grade: ");
                    input = scanner.nextLine();
                    int grade = Integer.parseInt(input);
                    addGrade(name, attended, grade);
                }
            } else if (input.equals("/l")) {
                listStudents();
            } else if (input.equals("/g")) {
                generateStudents(numOfStudents);
            } else {
                System.out.println("Unknown command. Type /h for help.");
            }
        }
    }

    public static void generateStudents(int numStudents) {
        String[] names = {"Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Janet", "Kevin", "Linda", "Mallory", "Nancy", "Oscar", "Peggy", "Quincy", "Randy", "Sybil", "Ted", "Ursula", "Victor", "Walter", "Xavier", "Yvonne", "Zoe"};
        Random rand = new Random();

        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "username", "password");

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO students (name) VALUES (?)");

            for (int i = 0; i < numStudents; i++) {
                String name = names[rand.nextInt(names.length)];
                stmt.setString(1, name);
                stmt.executeUpdate();
            }

            System.out.println(numStudents + " students added.");

            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listStudents() {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "username", "password");

            PreparedStatement stmt = conn.prepareStatement("SELECT name, grade FROM students");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int grade = rs.getInt("grade");
                System.out.println(name + ": " + grade);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTables() {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "username", "password");

            String createStudentsTable = "CREATE TABLE students (id SERIAL PRIMARY KEY, name VARCHAR(255), attended BOOLEAN, grade INT)";
            conn.createStatement().execute(createStudentsTable);

            System.out.println("Tables created.");

            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static String selectRandomStudent() {
        String name = null;
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "username", "password");

            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM students WHERE attended = false ORDER BY RANDOM() LIMIT 1");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }

            // Update the student's attendance flag to true
            stmt = conn.prepareStatement("UPDATE students SET attended = true WHERE name = ?");
            stmt.setString(1, name);
            stmt.executeUpdate();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static void addGrade(String name, boolean attended, int grade) {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydatabase", "username", "password");

            PreparedStatement stmt = conn.prepareStatement("UPDATE students SET attended = ?, grade = ? WHERE name = ?");
            stmt.setBoolean(1, attended);
            stmt.setInt(2, grade);
            stmt.setString(3, name);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Grade added for " + name);
            } else {
                System.out.println("Student " + name + " not found.");
            }

            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}