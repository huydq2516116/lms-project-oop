package lms.screen.librarian;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lms.object.Book;
import lms.object.HoldRequest;
import lms.object.Loan;

/**
 * Cung cấp dữ liệu giả lập (Mock Data) phục vụ chế độ demoMode khi chạy thử LibrarianScreen độc lập.
 */
public class LibrarianMockData {

	private final int librarianId;
	private final List<Book> mockBooks;
	private final List<Loan> mockLoans;
	private final List<HoldRequest> mockHoldRequests;
	private final List<Object[]> mockBorrowers;

	private int nextBookId = 106;
	private int nextLoanId = 510;
	private int nextBorrowerId = 4;

	public LibrarianMockData(int librarianId) {
		this.librarianId = librarianId;
		this.mockBooks = new ArrayList<>(createInitialBooks());
		this.mockLoans = new ArrayList<>(createInitialLoans());
		this.mockHoldRequests = new ArrayList<>();
		this.mockHoldRequests.add(new HoldRequest(103, new Date(), 1, 301));
		this.mockHoldRequests.add(new HoldRequest(104, new Date(), 2, 302));
		this.mockBorrowers = new ArrayList<>(createInitialBorrowers());
	}

	private List<Book> createInitialBooks() {
		List<Book> list = new ArrayList<>();
		list.add(new Book(101, "Clean Code", "Robert C. Martin", "Software Engineering", false));
		list.add(new Book(102, "Effective Java", "Joshua Bloch", "Java Programming", true));
		list.add(new Book(103, "Design Patterns", "Erich Gamma", "Architecture", false));
		list.add(new Book(104, "Head First Java", "Kathy Sierra", "Java Programming", false));
		list.add(new Book(105, "Introduction to Algorithms", "Thomas H. Cormen", "Algorithms", true));
		return list;
	}

	private List<Loan> createInitialLoans() {
		List<Loan> list = new ArrayList<>();
		long dayMs = 86_400_000L;
		Date now = new Date();
		list.add(new Loan(501, new Date(now.getTime() - 5 * dayMs), null, false,
				102, 1, librarianId, 0, new Date(now.getTime() + 9 * dayMs)));
		list.add(new Loan(502, new Date(now.getTime() - 20 * dayMs),
				new Date(now.getTime() - 12 * dayMs), true, 101, 2, librarianId, librarianId,
				new Date(now.getTime() - 6 * dayMs)));
		list.add(new Loan(503, new Date(now.getTime() - 30 * dayMs), null, false,
				105, 3, librarianId, 0, new Date(now.getTime() - 16 * dayMs)));
		return list;
	}

	private List<Object[]> createInitialBorrowers() {
		List<Object[]> list = new ArrayList<>();
		list.add(new Object[] { 1, "Nguyễn Văn An", "nguyenan", "Hà Nội", "0912345678" });
		list.add(new Object[] { 2, "Trần Thị Bình", "tranbth", "TP HCM", "0987654321" });
		list.add(new Object[] { 3, "Lê Minh Cường", "leminhc", "Đà Nẵng", "0909123456" });
		return list;
	}

	// =========================================================================
	// SÁCH (CRUD + TÌM KIẾM)
	// =========================================================================

	public synchronized List<Book> getAllMockBooks() {
		return new ArrayList<>(mockBooks);
	}

	public synchronized List<Book> filterMockBooks(String mode, String keyword) {
		if ("ALL".equals(mode) || keyword == null || keyword.isBlank()) {
			return new ArrayList<>(mockBooks);
		}
		String kw = keyword.trim().toLowerCase();
		List<Book> filtered = new ArrayList<>();
		for (Book b : mockBooks) {
			boolean match = switch (mode) {
				case "TITLE" -> b.getTitle().toLowerCase().contains(kw);
				case "AUTHOR" -> b.getAuthor().toLowerCase().contains(kw);
				case "SUBJECT" -> b.getSubject().toLowerCase().contains(kw);
				default -> false;
			};
			if (match) {
				filtered.add(b);
			}
		}
		return filtered;
	}

	public synchronized void addMockBook(String title, String author, String subject) {
		mockBooks.add(new Book(nextBookId++, title, author, subject, false));
	}

	public synchronized void updateMockBook(int bookId, String title, String author, String subject, boolean isIssued) {
		for (Book b : mockBooks) {
			if (b.getBookId() == bookId) {
				b.setTitle(title);
				b.setAuthor(author);
				b.setSubject(subject);
				b.setIssued(isIssued);
				return;
			}
		}
	}

	public synchronized void deleteMockBook(int bookId) {
		mockBooks.removeIf(b -> b.getBookId() == bookId);
	}

	// =========================================================================
	// PHIẾU MƯỢN
	// =========================================================================

	public synchronized List<Loan> getAllMockLoans() {
		return new ArrayList<>(mockLoans);
	}

	public synchronized void addMockLoan(int bookId, int borrowerId) {
		Date now = new Date();
		Date dueDate = new Date(now.getTime() + 14 * 86_400_000L);
		mockLoans.add(new Loan(nextLoanId++, now, null, false,
				bookId, borrowerId, librarianId, 0, dueDate));
		for (Book b : mockBooks) {
			if (b.getBookId() == bookId) {
				b.setIssued(true);
				break;
			}
		}
	}

	public synchronized void returnMockLoan(int loanId) {
		for (int i = 0; i < mockLoans.size(); i++) {
			Loan old = mockLoans.get(i);
			if (old.getLoanId() == loanId) {
				Loan updated = new Loan(old.getLoanId(), old.getIssuedDate(), new Date(),
						old.isFinePaid(), old.getBookId(), old.getBorrowerId(),
						old.getIssuerId(), librarianId, old.getDueDate());
				mockLoans.set(i, updated);
				for (Book b : mockBooks) {
					if (b.getBookId() == old.getBookId()) {
						b.setIssued(false);
						break;
					}
				}
				return;
			}
		}
	}

	public synchronized void payFineMockLoan(int loanId) {
		for (int i = 0; i < mockLoans.size(); i++) {
			Loan old = mockLoans.get(i);
			if (old.getLoanId() == loanId) {
				Loan updated = new Loan(old.getLoanId(), old.getIssuedDate(), old.getDateReturned(),
						true, old.getBookId(), old.getBorrowerId(),
						old.getIssuerId(), old.getReceiverId(), old.getDueDate());
				mockLoans.set(i, updated);
				return;
			}
		}
	}

	// =========================================================================
	// YÊU CẦU ĐẶT GIỮ & ĐỘC GIẢ
	// =========================================================================

	public synchronized List<HoldRequest> getAllMockHoldRequests() {
		return new ArrayList<>(mockHoldRequests);
	}

	public synchronized void removeMockHoldRequest(int index) {
		if (index >= 0 && index < mockHoldRequests.size()) {
			mockHoldRequests.remove(index);
		}
	}

	public synchronized List<Object[]> searchMockBorrowers(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return new ArrayList<>(mockBorrowers);
		}
		String kw = keyword.trim().toLowerCase();
		List<Object[]> filtered = new ArrayList<>();
		for (Object[] row : mockBorrowers) {
			if (row[1].toString().toLowerCase().contains(kw)
					|| row[2].toString().toLowerCase().contains(kw)
					|| row[0].toString().equals(kw)) {
				filtered.add(row);
			}
		}
		return filtered;
	}

	public synchronized void addMockBorrower(String name, String username, String address, String phone) {
		mockBorrowers.add(new Object[] { nextBorrowerId++, name, username, address, phone });
	}

	public synchronized void updateMockBorrower(int id, String username, String address, String phone) {
		for (Object[] row : mockBorrowers) {
			if ((int) row[0] == id) {
				row[2] = username;
				row[3] = address;
				row[4] = phone;
				return;
			}
		}
	}
}
