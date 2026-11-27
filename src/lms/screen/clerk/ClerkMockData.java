package lms.screen.clerk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lms.object.Book;
import lms.object.HoldRequest;
import lms.object.Loan;

/**
 * Cung cấp dữ liệu giả lập (Mock Data) phục vụ chế độ demoMode khi chạy thử ClerkScreen độc lập.
 */
public class ClerkMockData {

	private final int clerkId;
	private final List<HoldRequest> mockHoldRequests;
	private final List<Loan> mockLoans;
	private int nextLoanId = 510;

	public ClerkMockData(int clerkId) {
		this.clerkId = clerkId;
		this.mockHoldRequests = new ArrayList<>();
		this.mockHoldRequests.add(new HoldRequest(103, new Date(), 1, 301));
		this.mockHoldRequests.add(new HoldRequest(104, new Date(), 2, 302));
		this.mockLoans = new ArrayList<>(createInitialLoans());
	}

	private List<Loan> createInitialLoans() {
		List<Loan> list = new ArrayList<>();
		long dayMs = 86_400_000L;
		Date now = new Date();
		// Phiếu đang mượn
		list.add(new Loan(501, new Date(now.getTime() - 5 * dayMs), null, false,
				102, 1, clerkId, 0));
		// Phiếu đã trả, đã thanh toán phạt
		list.add(new Loan(502, new Date(now.getTime() - 20 * dayMs),
				new Date(now.getTime() - 12 * dayMs), true, 101, 2, clerkId, clerkId));
		// Phiếu đang mượn, chưa thanh toán phạt (quá hạn)
		list.add(new Loan(503, new Date(now.getTime() - 30 * dayMs), null, false,
				105, 3, clerkId, 0));
		return list;
	}

	/**
	 * Trả về toàn bộ sách mẫu trong thư viện
	 */
	public List<Book> getAllMockBooks() {
		List<Book> all = new ArrayList<>();
		all.add(new Book(101, "Clean Code", "Robert C. Martin", "Software Engineering", false));
		all.add(new Book(102, "Effective Java", "Joshua Bloch", "Java Programming", true));
		all.add(new Book(103, "Design Patterns", "Erich Gamma", "Architecture", false));
		all.add(new Book(104, "Head First Java", "Kathy Sierra", "Java Programming", false));
		all.add(new Book(105, "Introduction to Algorithms", "Thomas H. Cormen", "Algorithms", true));
		return all;
	}

	/**
	 * Lọc danh sách sách mẫu theo tiêu chí tìm kiếm
	 */
	public List<Book> filterMockBooks(String mode, String keyword) {
		List<Book> all = getAllMockBooks();
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
	 * Trả về danh sách phiếu mượn mẫu (toàn hệ thống)
	 */
	public List<Loan> getAllMockLoans() {
		return new ArrayList<>(mockLoans);
	}

	/**
	 * Thêm phiếu mượn mới trong chế độ demo
	 */
	public synchronized void addMockLoan(int bookId, int borrowerId) {
		mockLoans.add(new Loan(nextLoanId++, new Date(), null, false,
				bookId, borrowerId, clerkId, 0));
	}

	/**
	 * Đánh dấu phiếu mượn đã trả trong chế độ demo
	 */
	public synchronized void returnMockLoan(int loanId) {
		for (int i = 0; i < mockLoans.size(); i++) {
			if (mockLoans.get(i).getLoanId() == loanId) {
				Loan old = mockLoans.get(i);
				Loan updated = new Loan(old.getLoanId(), old.getIssuedDate(), new Date(),
						old.isFinePaid(), old.getBookId(), old.getBorrowerId(),
						old.getIssuerId(), clerkId);
				mockLoans.set(i, updated);
				return;
			}
		}
	}

	/**
	 * Đánh dấu phiếu mượn đã thanh toán phạt trong chế độ demo
	 */
	public synchronized void payFineMockLoan(int loanId) {
		for (int i = 0; i < mockLoans.size(); i++) {
			if (mockLoans.get(i).getLoanId() == loanId) {
				Loan old = mockLoans.get(i);
				Loan updated = new Loan(old.getLoanId(), old.getIssuedDate(), old.getDateReturned(),
						true, old.getBookId(), old.getBorrowerId(),
						old.getIssuerId(), old.getReceiverId());
				mockLoans.set(i, updated);
				return;
			}
		}
	}

	/**
	 * Trả về danh sách yêu cầu đặt giữ mẫu hiện tại (toàn hệ thống)
	 */
	public List<HoldRequest> getAllMockHoldRequests() {
		return new ArrayList<>(mockHoldRequests);
	}

	/**
	 * Xóa yêu cầu đặt giữ theo vị trí trong chế độ demo
	 */
	public synchronized void removeMockHoldRequest(int index) {
		if (index >= 0 && index < mockHoldRequests.size()) {
			mockHoldRequests.remove(index);
		}
	}

	/**
	 * Trả về dữ liệu độc giả mẫu dưới dạng mảng Object[][] cho bảng hiển thị
	 */
	public List<Object[]> getMockBorrowers() {
		List<Object[]> list = new ArrayList<>();
		list.add(new Object[] { 1, "Nguyễn Văn An", "nguyenan", "Hà Nội", "0912345678" });
		list.add(new Object[] { 2, "Trần Thị Bình", "tranbth", "TP HCM", "0987654321" });
		list.add(new Object[] { 3, "Lê Minh Cường", "leminhc", "Đà Nẵng", "0909123456" });
		return list;
	}
}
