package lms.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseOperations {
    private static final String URL = "jdbc:postgresql://localhost:5432/LMSDb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "27062007";

    public static void createPerson(String username, String password, String name, String address,
            String phoneno, String role) throws SQLException {

        String sql = "INSERT INTO persons (id, user_name, password, name, address, phone_no, role) VALUES (?, ?, ? , ? , ? , ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int personCount = readTableRowCount(conn, "persons");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, personCount);
                pstmt.setString(2, username);
                pstmt.setString(3, password);
                pstmt.setString(4, name);
                pstmt.setString(5, address);
                pstmt.setString(6, phoneno);
                pstmt.setString(7, role);
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm người dùng thành công!");
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
            }
        }

    }

    public static void createPerson(String username, String password, String name, String address,
            String phoneno, String role, double salary) throws SQLException {
        String sql = "INSERT INTO persons (id, user_name, password, name, address, phone_no, role) VALUES (?, ?, ? , ? , ? , ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int personCount = readTableRowCount(conn, "persons");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, personCount);
                pstmt.setString(2, username);
                pstmt.setString(3, password);
                pstmt.setString(4, name);
                pstmt.setString(5, address);
                pstmt.setString(6, phoneno);
                pstmt.setString(7, role);

                if (role.equals("clerk"))
                    createClerk(conn, personCount, salary);
                else if (role.equals("librarians"))
                    createLibrarian(conn, personCount, salary);
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("-> [CREATE] Thêm người dùng thành công!");
                }

            } catch (SQLException e) {
                System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
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
            } catch (SQLException e) {
                System.err.println("Error searching for user: " + e.getMessage());
            }
        }

        return null;
    }

    private static void createClerk(Connection conn, int id, double salary) {
        String sql = "INSERT INTO clerks (id, salary, desk_no) VALUES (?, ?, ?)";
        int clerkCount = readTableRowCount(conn, "clerks");
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, clerkCount);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Thêm người dùng thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    private static void createLibrarian(Connection conn, int id, double salary) {
        String sql = "INSERT INTO librarians (id, salary, desk_no) VALUES (?, ?, ?)";
        int librarianCount = readTableRowCount(conn, "librarians");

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, salary);
            pstmt.setInt(3, librarianCount);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("-> [CREATE] Thêm người dùng thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    private static int readTableRowCount(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        int count = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1); // Get the value of the 1st column in the result
            }
        } catch (SQLException e) {
            System.err.println("Error counting rows: " + e.getMessage());
        }

        return count;
    }

}
