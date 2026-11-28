package lms.object;

import java.sql.SQLException;

import lms.database.DatabaseOperations;

public class Book {
	private final int bookId;
	private String title;
	private String author;
	private String subject;
	private boolean isIssued;
	private boolean removed;
	
	public Book(int bookId, String title, String author, String subject, boolean isIssued) {
		super();
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.subject = subject;
		this.isIssued = isIssued;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isIssued() {
		return isIssued;
	}

	public void setIssued(boolean isIssued) {
		this.isIssued = isIssued;
	}

	public int getBookId() {
		return bookId;
	}

	public boolean isAvailable() {
		return !isIssued && !removed;
	}

	public Loan checkOut(int borrowerId, int issuerId) throws SQLException {
		lend(borrowerId, issuerId);
		return Loan.latestForBook(bookId);
	}

	void lend(int borrowerId, int issuerId) throws SQLException {
		refresh();
		if (!isAvailable()) {
			throw new IllegalStateException("Book is unavailable for checkout.");
		}
		// shortcut: separate writes, not one transaction; move into DatabaseOperations when several clerks share it.
		// Marked issued first so a failure can never leave a lent book looking available.
		saveIssued(true);
		try {
			DatabaseOperations.createLoan(bookId, borrowerId, issuerId);
		} catch (SQLException failure) {
			try {
				saveIssued(false);
			} catch (SQLException undoFailure) {
				failure.addSuppressed(undoFailure);
			}
			throw failure;
		}
	}

	public void markReturned() throws SQLException {
		refresh();
		if (removed) {
			throw new IllegalStateException("Book has been removed from the catalog.");
		}
		saveIssued(false);
	}

	/** Takes a lost book out of circulation but keeps its row, so its loans and fines survive. */
	public void markLost() throws SQLException {
		refresh();
		// shortcut: no lost status in the schema, so a lost book is stored as issued; add a column to tell them apart.
		saveIssued(true);
	}

	public void remove() throws SQLException {
		refresh();
		if (isIssued) {
			throw new IllegalStateException("Cannot remove an issued book.");
		}
		DatabaseOperations.deleteBook(bookId);
		removed = true;
	}

	/** Reloads from the database so that another user's changes are neither ignored nor overwritten. */
	private void refresh() throws SQLException {
		Book stored = DatabaseOperations.getBookById(bookId);
		if (stored == null) {
			removed = true;
			return;
		}
		title = stored.title;
		author = stored.author;
		subject = stored.subject;
		isIssued = stored.isIssued;
	}

	void saveIssued(boolean issued) throws SQLException {
		DatabaseOperations.updateBook(bookId, title, author, subject, issued);
		isIssued = issued;
	}
}
