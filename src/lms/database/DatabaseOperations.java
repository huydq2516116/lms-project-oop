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
import lms.object.HoldRequest;
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
                    System.out.println("-> [CREATE] Create Person Success!");
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
                    System.out.println("-> [CREATE] Create Person Success!");
                    int generatedId = -1;
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            // Retrieve the ID (column index 1)
                            generatedId = generatedKeys.getInt(1);
                            System.out.println("Successfully inserted person! Generated ID: " + generatedId);
                        } else {
                            System.out.println("Insertion succeeded, but no ID was obtained.");
                        }
                    }
                    if (role.equals("clerk"))
                        createClerk(conn, generatedId, salary, deskOrOfficeNo);
                    else if (role.equals("librarian"))
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
                    System.out.println("-> [CREATE] Create HoldRequest Success!");
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
                pstmt.setInt(4, issuer_id);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Create Loan Success!");
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
                    System.out.println("-> [CREATE] Create book Success!");
                }
            }
        }
    }

    public static PasswordRole getPasswordByUserName(String personName) throws SQLException {
        String sql = "SELECT password, role FROM persons WHERE user_name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, personName);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        return new PasswordRole(rs.getString("password"), rs.getString("role"));
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

    // Search for items by title
    public static ArrayList<Book> getBooksByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE title LIKE ?";
        var books = new ArrayList<Book>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + title + "%");
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

    // Search for items by author
    public static ArrayList<Book> getBooksByAuthor(String author) throws SQLException {
        String sql = "SELECT * FROM books WHERE author LIKE ?";
        var books = new ArrayList<Book>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + author + "%");
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

    // Search for items by subject
    public static ArrayList<Book> getBooksBySubject(String subject) throws SQLException {
        String sql = "SELECT * FROM books WHERE subject LIKE ?";
        var books = new ArrayList<Book>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + subject + "%");
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

    public static Book getBookById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the ID parameter safely
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getBoolean(5));
                }
            }
        }
        return null; // Return null if no book with that ID exists
    }

    public static Loan getLoanById(int id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the ID parameter safely
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Loan(
                            rs.getInt(1),
                            rs.getDate(2),
                            rs.getDate(3),
                            rs.getBoolean(4),
                            rs.getInt(5),
                            rs.getInt(6),
                            rs.getInt(7),
                            rs.getInt(8));
                }
            }
        }
        return null; // Return null if no book with that ID exists
    }

    public static ArrayList<Loan> getLoansByBorrowerId(int id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE borrower_id = ?";
        var loans = new ArrayList<Loan>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Bind the ID parameter safely
                pstmt.setInt(1, id);

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

    public static ArrayList<Loan> getLoansByBookId(int id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE book_id = ?";
        var loans = new ArrayList<Loan>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Bind the ID parameter safely
                pstmt.setInt(1, id);

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

    public static ArrayList<HoldRequest> getHoldRequestsByBorrowerId(int id) throws SQLException {
        String sql = "SELECT * FROM hold_requests WHERE borrower_id = ?";
        var hold_requests = new ArrayList<HoldRequest>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Bind the ID parameter safely
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        hold_requests.add(new HoldRequest(rs.getInt(1), rs.getDate(2), rs.getInt(3), rs.getInt(4)));
                    }
                }
            }
        }
        return hold_requests;
    }

    public static ArrayList<HoldRequest> getHoldRequestsByBookId(int id) throws SQLException {
        String sql = "SELECT * FROM hold_requests WHERE book_id = ?";
        var hold_requests = new ArrayList<HoldRequest>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Bind the ID parameter safely
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        hold_requests.add(new HoldRequest(rs.getInt(1), rs.getDate(2), rs.getInt(3), rs.getInt(4)));
                    }
                }
            }
        }
        return hold_requests;
    }

    // Check the person's personal information
    public static PersonInfo getPersonInfo(String username) throws SQLException {
        String sql = "SELECT id, user_name, name, address, phone_no, role FROM persons WHERE user_name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        return new PersonInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getString(6));
                    }
                    return null;
                }
            }
        }
    }

    // List books currently borrowed by a specific borrower
    public static ArrayList<Book> getBooksBorrowedByBorrower(int borrowerId) throws SQLException {
        String sql = "SELECT b.id, b.title, b.author, b.subject, b.is_issued " +
                "FROM books b JOIN loans l ON b.id = l.book_id " +
                "WHERE l.borrower_id = ? AND l.fine_paid = false";
        var books = new ArrayList<Book>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, borrowerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getInt(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getBoolean(5)));
                }
            }
        }
        return books;
    }

    // Check in an item that has been returned
    public static void updateLoanReturnDate(int loanId, Date returnDate) throws SQLException {
        String sql = "UPDATE loans SET date_returned = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the parameters safely
            pstmt.setDate(1, returnDate);
            pstmt.setInt(2, loanId);

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Loan return date updated successfully.");
            } else {
                System.out.println("No loan found with ID: " + loanId);
            }
        }
    }

    // Record that a fine has been paid
    public static void updateFineStatus(int loanId) throws SQLException {
        String sql = "UPDATE loans SET finePaid = true WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the parameters safely
            pstmt.setInt(1, loanId);

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Fine status updated successfully.");
            } else {
                System.out.println("No loan found with ID: " + loanId);
            }
        }
    }

    // Update a borrower's personal information (address, phone number, etc.)
    public static void updatePerson(int personId, String user_name, String address, String phone_no)
            throws SQLException {
        String sql = "UPDATE persons SET user_name = ?, address = ?, phone_no = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the parameters safely
            pstmt.setString(1, user_name);
            pstmt.setString(2, address);
            pstmt.setString(3, phone_no);
            pstmt.setInt(4, personId);

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Person updated successfully.");
            } else {
                System.out.println("No person found with ID: " + personId);
            }
        }
    }

    // Change the information recorded about an item
    public static void updateBook(int bookId, String title, String author, String subject, boolean is_issued)
            throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, subject = ?, is_issued = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the parameters safely
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, subject);
            pstmt.setBoolean(4, is_issued);
            pstmt.setInt(5, bookId);

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Fine status updated successfully.");
            } else {
                System.out.println("No book found with ID: " + bookId);
            }
        }
    }

    // Delete an item from the collection
    public static void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the ID parameter safely
            pstmt.setInt(1, bookId);

            // Execute the delete statement and check affected rows
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully.");
            } else {
                System.out.println("No book found with ID: " + bookId);
            }
        }
    }

    public static void deleteHoldRequest(int holdRequestId) throws SQLException {
        String sql = "DELETE FROM hold_requests WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind the ID parameter safely
            pstmt.setInt(1, holdRequestId);

            // Execute the delete statement and check affected rows
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully.");
            } else {
                System.out.println("No book found with ID: " + holdRequestId);
            }
        }
    }

    private static void createClerk(Connection conn, int id, double salary, int deskNo) {
        String sql = "INSERT INTO clerks (id, salary, desk_no) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, deskNo);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Create Clerk Success!");
            }
        } catch (SQLException e) {
            System.err.println("Error when create clerk: " + e.getMessage());
        }
    }

    private static void createLibrarian(Connection conn, int id, double salary, int officeNo) {
        String sql = "INSERT INTO librarians (id, salary, office_no) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, officeNo);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Create Librarians Success!");
            }
        } catch (SQLException e) {
            System.err.println("Error when add librarians: " + e.getMessage());
        }
    }

    public record PasswordRole(String password, String role) {
    }

    public record PersonInfo(int id, String userName, String name, String address, String phoneNo, String role) {
    }

}
