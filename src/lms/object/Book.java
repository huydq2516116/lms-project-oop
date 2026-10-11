package lms.object;

public class Book {
	private final int bookId;
	private String title;
	private String author;
	private String subject;
	private boolean isIssued;
	
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
	
	
}
