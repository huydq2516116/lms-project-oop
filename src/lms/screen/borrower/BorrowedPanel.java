package lms.screen.borrower;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import lms.object.Book;
import lms.person.Borrower;
import lms.screen.util.UITheme;

/**
 * Panel hiển thị danh sách các đầu sách mà độc giả đang mượn.
 */
public final class BorrowedPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerMockData mockData;
	private final transient BorrowerViewListener listener;

	private transient List<Book> currentBorrowedBooks = new ArrayList<>();

	private JTable tblBorrowedBooks;
	private DefaultTableModel modelBorrowedBooks;
	private JButton btnRefreshBorrowed;

	public BorrowedPanel(Borrower borrower, boolean demoMode, BorrowerMockData mockData,
			BorrowerViewListener listener) {
		this.borrower = borrower;
		this.demoMode = demoMode;
		this.mockData = mockData;
		this.listener = listener;

		setLayout(new BorderLayout(0, 12));
		setBackground(UITheme.CARD_BG);
		putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1,#E2E8F0");
		setBorder(new EmptyBorder(14, 16, 14, 16));
		initUI();
	}

	private void initUI() {
		// --- Phần đầu: Mô tả & Nút làm mới ---
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);

		JLabel lblDesc = new JLabel("Các đầu sách bạn đang giữ (chưa hoàn tất trả/thanh toán phạt)");
		lblDesc.setFont(UITheme.FONT_REGULAR);
		lblDesc.setForeground(UITheme.TEXT_MUTED);

		btnRefreshBorrowed = UITheme.createSecondaryButton("Làm mới");
		btnRefreshBorrowed.setPreferredSize(new Dimension(100, 34));
		btnRefreshBorrowed.addActionListener(e -> loadBorrowedBooksAsync());

		pnlTop.add(lblDesc, BorderLayout.WEST);
		pnlTop.add(btnRefreshBorrowed, BorderLayout.EAST);

		// --- Bảng dữ liệu (bọc trong JScrollPane) ---
		String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Chủ đề", "Trạng thái" };
		modelBorrowedBooks = UITheme.createReadOnlyTableModel(columns);
		tblBorrowedBooks = UITheme.createStyledTable(modelBorrowedBooks);
		tblBorrowedBooks.getColumnModel().getColumn(0).setMaxWidth(95);
		tblBorrowedBooks.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());

		add(pnlTop, BorderLayout.NORTH);
		add(new JScrollPane(tblBorrowedBooks), BorderLayout.CENTER);
	}

	public void loadBorrowedBooksAsync() {
		new Thread(() -> {
			try {
				List<Book> result = demoMode ? mockData.createMockBorrowedBooks() : borrower.getBorrowedBooks();
				SwingUtilities.invokeLater(() -> {
					currentBorrowedBooks = result != null ? result : new ArrayList<>();
					renderBorrowedBooksTable();
					listener.onBorrowedCountChanged(currentBorrowedBooks.size());
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> listener
						.onStatusUpdate("Lỗi tải sách đang mượn: " + ex.getMessage(), UITheme.DANGER_COLOR));
			}
		}).start();
	}

	private void renderBorrowedBooksTable() {
		modelBorrowedBooks.setRowCount(0);
		for (Book b : currentBorrowedBooks) {
			modelBorrowedBooks.addRow(new Object[] {
					b.getBookId(),
					b.getTitle(),
					b.getAuthor(),
					b.getSubject(),
					"Đang mượn"
			});
		}
	}
}
