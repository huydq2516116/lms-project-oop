package lms.person;

import java.sql.SQLException;

import lms.database.DatabaseOperations;

public class Librarian extends Staff {
	private final int officeNo;
	public Librarian(int id, int officeNo ,String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo, salary);
		this.officeNo = officeNo;
	}
	public int getOfficeNo() {
		return officeNo;
	}

	// Add a new book to the collection
    public void createBook(String title, String author, String subject) throws SQLException {

        DatabaseOperations.createBook(title, author, subject);
    }

    // Remove a book from the collection
    public void deleteBook(int bookId) throws SQLException {

        DatabaseOperations.deleteBook(bookId);
    }

    // Update book information
    public void updateBook(int bookId, String title, String author, String subject, boolean is_issued) throws SQLException {

        DatabaseOperations.updateBook(bookId, title, author, subject, is_issued);
    }
}
