package lms.person;

import java.sql.SQLException;
import java.util.ArrayList;

import lms.database.DatabaseOperations;
import lms.object.Book;
import lms.object.Loan;

public class Borrower extends Person {

	private int borrowerId;

	public Borrower(int id, String userName, String password, String name, String address, String phoneNo) {
		super(id, userName, password, name, address, phoneNo);
		this.borrowerId = id;
	}

	// Search books by title
    public ArrayList<Book> searchByTitle(String title)
            throws SQLException {
        return DatabaseOperations.getBooksByTitle(title);
    }

    // Search books by author
    public ArrayList<Book> searchByAuthor(String author)
            throws SQLException {
        return DatabaseOperations.getBooksByAuthor(author);
    }

    // Search books by subject
    public ArrayList<Book> searchBySubject(String subject)
            throws SQLException {
        return DatabaseOperations.getBooksBySubject(subject);
    }

    // Place a hold request on a book
    public void placeHold(Book book) throws SQLException {
        if (book == null) {
            throw new IllegalArgumentException(
                "Book cannot be null."
            );
        }

        DatabaseOperations.createHoldRequest(
            borrowerId, book.getBookId()
        );
    }

    // Get books currently borrowed by this borrower
    public ArrayList<Book> getBorrowedBooks() throws SQLException {
        return DatabaseOperations.getBooksBorrowedByBorrower(
            borrowerId
        );
    }

    // Get loan records belonging to this borrower
    public ArrayList<Loan> getLoans() throws SQLException {
        return DatabaseOperations.getLoansByBorrowerId(
            borrowerId
        );
    }



}
