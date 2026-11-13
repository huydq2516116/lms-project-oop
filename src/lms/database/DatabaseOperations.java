package lms.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import lms.object.Book;
import lms.object.Loan;

public class DatabaseOperations {
    private static final String URL = "jdbc:postgresql://ep-quiet-cell-a1hp77um-pooler.ap-southeast-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_kcT1Cla4yorx&sslmode=require&channelBinding=require";
    private static final String USER = "postgres";
    private static final String PASSWORD = "27062007";

    public static void createPerson(String username, String password, String name, String address,
            String phoneno, String role) throws SQLException {

        String sql = "INSERT INTO persons (user_name, password, name, address, phone_no, role) VALUES (?, ? , ? , ? , ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, address);
                pstmt.setString(5, phoneno);
                pstmt.setString(6, role);
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm người dùng thành công!");
                }
            }
        }

    }

    public static void createPerson(String username, String password, String name, String address,
            String phoneNo, String role, double salary, int deskOrOfficeNo) throws SQLException {
        String sql = "INSERT INTO persons (user_name, password, name, address, phone_no, role) VALUES (?, ? , ? , ? , ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, address);
                pstmt.setString(5, phoneNo);
                pstmt.setString(6, role);
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm người dùng thành công!");
                    int generatedId = -1;
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            // Retrieve the ID (column index 1)
                            generatedId = generatedKeys.getInt(1);
                            System.out.println("Successfully inserted row! Generated ID: " + generatedId);
                        } else {
                            System.out.println("Insertion succeeded, but no ID was obtained.");
                        }
                    }
                    if (role.equals("clerk"))
                        createClerk(conn, generatedId, salary, deskOrOfficeNo);
                    else if (role.equals("librarians"))
                        createLibrarian(conn, generatedId, salary, deskOrOfficeNo);
                }
            }
        }

    }

    public static void createHoldRequest(int borrower_id, int book_id) throws SQLException {
        String sql = "INSERT INTO hold_requests (request_date, borrower_id, book_id) VALUES (?, ? , ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setInt(2, borrower_id);
                pstmt.setInt(3, book_id);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm hr thành công!");
                }
            }
        }
    }

    public static void createLoan(int book_id, int borrower_id, int issuer_id) throws SQLException {
        String sql = "INSERT INTO loans (issued_date, book_id ,borrower_id, issuer_id) VALUES (?, ? ,?,?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setInt(2, book_id);
                pstmt.setInt(3, borrower_id);
                pstmt.setInt(4, book_id);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm loan thành công!");
                }
            }
        }
    }

    public static void createBook(String title, String author, String subject) throws SQLException {
        String sql = "INSERT INTO books (title, author ,subject, is_issued) VALUES (?, ? ,?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                pstmt.setString(3, subject);
                pstmt.setBoolean(4, false);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm book thành công!");
                }
            }
        }
    }

    public static String findPasswordByUserName(String personName) throws SQLException {
        String sql = "SELECT password FROM persons WHERE user_name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, personName);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        return rs.getString("password");
                    }
                    return null;
                }
            }
        }
    }

    public static ArrayList<Book> getAllBooks() throws SQLException {
        String sql = "SELECT * FROM books";
        var books = new ArrayList<Book>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        books.add(new Book(rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getString(4), rs.getBoolean(5)));
                    }
                }
            }
        }
        return books;
    }

    public static ArrayList<Loan> getAllLoans() throws SQLException {
        String sql = "SELECT * FROM loans";
        var loans = new ArrayList<Loan>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        loans.add(new Loan(rs.getInt(1), rs.getDate(2), rs.getDate(3), rs.getBoolean(4), rs.getInt(5),
                                rs.getInt(6), rs.getInt(7), rs.getInt(8)));
                    }
                }
            }
        }
        return loans;
    }

    private static void createClerk(Connection conn, int id, double salary, int deskNo) {
        String sql = "INSERT INTO clerks (id, salary, desk_no) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, deskNo);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Thêm người dùng thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    private static void createLibrarian(Connection conn, int id, double salary, int officeNo) {
        String sql = "INSERT INTO librarians (id, salary, desk_no) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, officeNo);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Thêm người dùng thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            createBook("Harry Potter", "J.K.Rowling", "Fantasy");
        } catch (SQLException ex) {
            System.getLogger(DatabaseOperations.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

}
