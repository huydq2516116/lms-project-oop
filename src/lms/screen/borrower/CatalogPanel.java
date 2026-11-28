package lms.screen.borrower;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import lms.database.DatabaseOperations;
import lms.object.Book;
import lms.person.Borrower;
import lms.screen.util.UITheme;

/**
 * Panel Tra cứu sách & Tổng quan (3 thẻ thống kê, thanh tìm kiếm, bảng sách, chi tiết & đặt giữ).
 */
public final class CatalogPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerMockData mockData;
	private final transient BorrowerViewListener listener;

	private transient List<Book> currentBooks = new ArrayList<>();

	// Thẻ thống kê nhanh trên Dashboard
	private JLabel lblStatBorrowedCount;
	private JLabel lblStatLoansCount;
	private JLabel lblStatHoldsCount;

	// Khu vực Tra cứu sách
	private JTextField txtSearchBook;
	private JComboBox<String> cboSearchType;
	private JButton btnSearchBook;
	private JButton btnRefreshCatalog;
	private JButton btnPlaceHold;
	private JButton btnBookDetail;
	private JTable tblBooks;
	private DefaultTableModel modelBooks;

	public CatalogPanel(Borrower borrower, boolean demoMode, BorrowerMockData mockData,
			BorrowerViewListener listener) {
		this.borrower = borrower;
		this.demoMode = demoMode;
		this.mockData = mockData;
		this.listener = listener;

		setLayout(new BorderLayout(0, 14));
		setOpaque(false);
		initUI();
	}

	private void initUI() {
		// --- Khu vực 3 thẻ thống kê nhanh ---
		JPanel pnlStats = new JPanel(new GridLayout(1, 3, 14, 0));
		pnlStats.setOpaque(false);

		lblStatBorrowedCount = new JLabel("0");
		lblStatLoansCount = new JLabel("0");
		lblStatHoldsCount = new JLabel("0");

		pnlStats.add(createStatCard("Sách đang mượn", lblStatBorrowedCount, UITheme.PRIMARY_COLOR));
		pnlStats.add(createStatCard("Tổng lượt mượn", lblStatLoansCount, UITheme.SUCCESS_COLOR));
		pnlStats.add(createStatCard("Đang đặt giữ", lblStatHoldsCount, UITheme.WARNING_COLOR));

		// --- Thanh tìm kiếm hiện đại ---
		JPanel pnlSearchCard = UITheme.createWhiteCardPanel(new BorderLayout(10, 0));
		pnlSearchCard.setBorder(new EmptyBorder(12, 14, 12, 14));

		txtSearchBook = new JTextField();
		txtSearchBook.setFont(UITheme.FONT_REGULAR);
		txtSearchBook.setPreferredSize(new Dimension(0, 36));
		txtSearchBook.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Nhập tên sách, tác giả hoặc chủ đề cần tìm...");
		txtSearchBook.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		txtSearchBook.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		txtSearchBook.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchBooksAction();
				}
			}
		});

		cboSearchType = new JComboBox<>(new String[] {
				"Tất cả sách", "Theo tên sách", "Theo tác giả", "Theo chủ đề"
		});
		cboSearchType.setFont(UITheme.FONT_REGULAR);
		cboSearchType.setPreferredSize(new Dimension(150, 36));
		cboSearchType.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

		btnSearchBook = UITheme.createPrimaryButton("Tìm kiếm");
		btnSearchBook.setPreferredSize(new Dimension(105, 36));
		btnSearchBook.addActionListener(e -> searchBooksAction());

		btnRefreshCatalog = UITheme.createSecondaryButton("Làm mới");
		btnRefreshCatalog.setPreferredSize(new Dimension(95, 36));
		btnRefreshCatalog.addActionListener(e -> refreshCatalog());

		JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlSearchControls.setOpaque(false);
		pnlSearchControls.add(cboSearchType);
		pnlSearchControls.add(btnSearchBook);
		pnlSearchControls.add(btnRefreshCatalog);

		pnlSearchCard.add(txtSearchBook, BorderLayout.CENTER);
		pnlSearchCard.add(pnlSearchControls, BorderLayout.EAST);

		JPanel pnlTop = new JPanel(new BorderLayout(0, 12));
		pnlTop.setOpaque(false);
		pnlTop.add(pnlStats, BorderLayout.NORTH);
		pnlTop.add(pnlSearchCard, BorderLayout.SOUTH);

		add(pnlTop, BorderLayout.NORTH);

		// --- Bảng dữ liệu sách (bọc trong JScrollPane) ---
		String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Chủ đề", "Trạng thái" };
		modelBooks = UITheme.createReadOnlyTableModel(columns);
		tblBooks = UITheme.createStyledTable(modelBooks);
		tblBooks.getColumnModel().getColumn(0).setPreferredWidth(80);
		tblBooks.getColumnModel().getColumn(0).setMaxWidth(95);
		tblBooks.getColumnModel().getColumn(1).setPreferredWidth(260);
		tblBooks.getColumnModel().getColumn(4).setPreferredWidth(135);
		tblBooks.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());

		JPanel pnlTableCard = UITheme.createWhiteCardPanel(new BorderLayout(0, 10));
		pnlTableCard.setBorder(new EmptyBorder(12, 14, 12, 14));
		pnlTableCard.add(new JScrollPane(tblBooks), BorderLayout.CENTER);

		// --- Panel nút chức năng dưới bảng ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		btnBookDetail = UITheme.createSecondaryButton("Xem chi tiết");
		btnBookDetail.setPreferredSize(new Dimension(120, 36));
		btnBookDetail.addActionListener(e -> showBookDetailAction());

		btnPlaceHold = UITheme.createPrimaryButton("Đặt giữ sách (Place Hold)");
		btnPlaceHold.setPreferredSize(new Dimension(190, 36));
		btnPlaceHold.addActionListener(e -> placeHoldAction());

		pnlActions.add(btnBookDetail);
		pnlActions.add(btnPlaceHold);
		pnlTableCard.add(pnlActions, BorderLayout.SOUTH);

		add(pnlTableCard, BorderLayout.CENTER);
	}

	/**
	 * Tạo thẻ thống kê nhanh
	 */
	private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
		JPanel pnlCard = UITheme.createWhiteCardPanel(new BorderLayout(0, 4));
		pnlCard.setBorder(new EmptyBorder(14, 16, 14, 16));

		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(UITheme.FONT_REGULAR);
		lblTitle.setForeground(UITheme.TEXT_MUTED);

		lblValue.setFont(UITheme.FONT_STAT);
		lblValue.setForeground(accent);

		pnlCard.add(lblTitle, BorderLayout.NORTH);
		pnlCard.add(lblValue, BorderLayout.CENTER);
		return pnlCard;
	}

	// =========================================================================
	// HÀM PUBLIC CẬP NHẬT THỐNG KÊ & TẢI DỮ LIỆU
	// =========================================================================

	public void updateBorrowedCount(int count) {
		lblStatBorrowedCount.setText(String.valueOf(count));
	}

	public void updateLoansCount(int count) {
		lblStatLoansCount.setText(String.valueOf(count));
	}

	public void updateHoldsCount(int count) {
		lblStatHoldsCount.setText(String.valueOf(count));
	}

	public void refreshCatalog() {
		txtSearchBook.setText("");
		cboSearchType.setSelectedIndex(0);
		loadBooksAsync("ALL", "");
	}

	public void loadBooksAsync(String mode, String keyword) {
		listener.onStatusUpdate("Đang tải danh sách sách...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<Book> result;
				if (demoMode) {
					result = mockData.filterMockBooks(mode, keyword);
				} else {
					result = switch (mode) {
						case "TITLE" -> borrower.searchByTitle(keyword);
						case "AUTHOR" -> borrower.searchByAuthor(keyword);
						case "SUBJECT" -> borrower.searchBySubject(keyword);
						default -> DatabaseOperations.getAllBooks();
					};
				}
				SwingUtilities.invokeLater(() -> {
					currentBooks = result != null ? result : new ArrayList<>();
					renderBooksTable();
					listener.onStatusUpdate("Đã tải " + currentBooks.size() + " đầu sách.", UITheme.TEXT_MUTED);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					listener.onStatusUpdate("Lỗi tải sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể tải danh sách sách từ cơ sở dữ liệu:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	// =========================================================================
	// XỬ LÝ SỰ KIỆN TRONG CATALOG
	// =========================================================================

	private void searchBooksAction() {
		String keyword = txtSearchBook.getText().trim();
		int typeIdx = cboSearchType.getSelectedIndex();

		if (typeIdx == 0 && keyword.isEmpty()) {
			loadBooksAsync("ALL", "");
			return;
		}
		if (keyword.isEmpty()) {
			UITheme.showWarning(this, "Vui lòng nhập từ khóa tìm kiếm!");
			txtSearchBook.requestFocusInWindow();
			return;
		}

		String mode = switch (typeIdx) {
			case 1 -> "TITLE";
			case 2 -> "AUTHOR";
			case 3 -> "SUBJECT";
			default -> "TITLE";
		};
		loadBooksAsync(mode, keyword);
	}

	private void placeHoldAction() {
		int selectedRow = tblBooks.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= currentBooks.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một cuốn sách trong bảng trước khi đặt giữ!");
			return;
		}

		Book selectedBook = currentBooks.get(selectedRow);
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Bạn có muốn gửi yêu cầu đặt giữ cuốn sách:\n\"" + selectedBook.getTitle() + "\" (ID: "
						+ selectedBook.getBookId() + ")?",
				"Xác nhận đặt giữ sách",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnPlaceHold.setEnabled(false);
		listener.onStatusUpdate("Đang gửi yêu cầu đặt giữ...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.addMockHoldRequest(selectedBook.getBookId());
				} else {
					borrower.placeHold(selectedBook);
				}

				SwingUtilities.invokeLater(() -> {
					btnPlaceHold.setEnabled(true);
					listener.onStatusUpdate("Đã gửi yêu cầu đặt giữ sách thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đặt giữ thành công cuốn \"" + selectedBook.getTitle() + "\"!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onHoldPlaced();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnPlaceHold.setEnabled(true);
					listener.onStatusUpdate("Lỗi đặt giữ sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể đặt giữ sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void showBookDetailAction() {
		int selectedRow = tblBooks.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= currentBooks.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một cuốn sách để xem chi tiết!");
			return;
		}

		Book b = currentBooks.get(selectedRow);
		String status = b.isIssued() ? "Đang được mượn" : "Có sẵn tại thư viện";
		String info = String.format(
				"Mã sách: #%d%nTên sách: %s%nTác giả: %s%nChủ đề: %s%nTrạng thái: %s",
				b.getBookId(), b.getTitle(), b.getAuthor(), b.getSubject(), status);
		JOptionPane.showMessageDialog(this, info, "Chi tiết đầu sách", JOptionPane.INFORMATION_MESSAGE);
	}

	private void renderBooksTable() {
		modelBooks.setRowCount(0);
		for (Book b : currentBooks) {
			modelBooks.addRow(new Object[] {
					b.getBookId(),
					b.getTitle(),
					b.getAuthor(),
					b.getSubject(),
					b.isIssued() ? "Đang được mượn" : "Có sẵn"
			});
		}
	}
}
