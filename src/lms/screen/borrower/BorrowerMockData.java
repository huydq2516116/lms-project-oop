package lms.screen.borrower;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lms.object.Book;
import lms.object.HoldRequest;
import lms.object.Loan;

/**
 * Cung cấp dữ liệu giả lập (Mock Data) phục vụ chế độ demoMode khi chạy thử độc lập.
 */
public class BorrowerMockData {

	private final int borrowerId;
	private final List<HoldRequest> mockHoldRequests;
	private int nextHoldId = 302;

	public BorrowerMockData(int borrowerId) {
		this.borrowerId = borrowerId;
		this.mockHoldRequests = new ArrayList<>();
		this.mockHoldRequests.add(new HoldRequest(105, new Date(), borrowerId, 301));
	}

	/**
	 * Lọc danh sách sách mẫu theo tiêu chí tìm kiếm
	 */
	public List<Book> filterMockBooks(String mode, String keyword) {
		List<Book> all = new ArrayList<>();
		all.add(new Book(101, "Clean Code", "Robert C. Martin", "Software Engineering", false));
		all.add(new Book(102, "Effective Java", "Joshua Bloch", "Java Programming", true));
		all.add(new Book(103, "Design Patterns", "Erich Gamma", "Architecture", false));
		all.add(new Book(104, "Head First Java", "Kathy Sierra", "Java Programming", false));
		all.add(new Book(105, "Introduction to Algorithms", "Thomas H. Cormen", "Algorithms", true));

		if ("ALL".equals(mode) || keyword == null || keyword.isEmpty()) {
			return all;
		}
		String kw = keyword.toLowerCase();
		List<Book> filtered = new ArrayList<>();
		for (Book b : all) {
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

	/**
	 * Tạo danh sách sách đang mượn mẫu
	 */
	public List<Book> createMockBorrowedBooks() {
		List<Book> list = new ArrayList<>();
		list.add(new Book(102, "Effective Java", "Joshua Bloch", "Java Programming", true));
		return list;
	}

	/**
	 * Tạo danh sách lịch sử phiếu mượn mẫu
	 */
	public List<Loan> createMockLoans() {
		List<Loan> list = new ArrayList<>();
		long dayMs = 86_400_000L;
		Date now = new Date();
		list.add(new Loan(501, new Date(now.getTime() - 5 * dayMs), null, false, 102, borrowerId, 2, 0,
				new Date(now.getTime() + 9 * dayMs)));
		list.add(new Loan(480, new Date(now.getTime() - 20 * dayMs), new Date(now.getTime() - 12 * dayMs), true,
				101, borrowerId, 2, 2, new Date(now.getTime() - 6 * dayMs)));
		return list;
	}

	/**
	 * Trả về danh sách yêu cầu đặt giữ mẫu hiện tại
	 */
	public List<HoldRequest> createMockHoldRequests() {
		return new ArrayList<>(mockHoldRequests);
	}

	/**
	 * Thêm yêu cầu đặt giữ mới trong chế độ demo
	 */
	public synchronized void addMockHoldRequest(int bookId) {
		mockHoldRequests.add(new HoldRequest(bookId, new Date(), borrowerId, nextHoldId++));
	}

	/**
	 * Xóa yêu cầu đặt giữ theo vị trí trong chế độ demo
	 */
	public synchronized void removeMockHoldRequest(int index) {
		if (index >= 0 && index < mockHoldRequests.size()) {
			mockHoldRequests.remove(index);
		}
	}
}
