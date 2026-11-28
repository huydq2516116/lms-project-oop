package lms.screen.librarian;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import lms.person.Librarian;
import lms.screen.util.UITheme;

/**
 * Panel Quản lý Kho sách (Tab mặc định của LibrarianScreen).
 * Gồm 3 thẻ thống kê nhanh, tìm kiếm đa tiêu chí, lọc trạng thái và CRUD sách.
 */
public final class ManageBooksPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Librarian librarian;
	private final boolean demoMode;
	private final transient LibrarianMockData mockData;
	private final transient LibrarianViewListener listener;

	private transient List<Book> currentBooks = new ArrayList<>();
	private transient List<Book> displayedBooks = new ArrayList<>();

	// 3 thẻ thống kê nhanh
	private JLabel lblStatTotalBooks;
	private JLabel lblStatAvailableBooks;
	private JLabel lblStatIssuedBooks;

	// Tìm kiếm & bộ lọc
	private JTextField txtSearchBook;
	private JComboBox<String> cboSearchType;
	private JComboBox<String> cboStatusFilter;
	private JTable tblBooks;
	private DefaultTableModel modelBooks;

	// Nút CRUD
	private JButton btnAddBook;
	private JButton btnEditBook;
	private JButton btnDeleteBook;

	public ManageBooksPanel(Librarian librarian, boolean demoMode, LibrarianMockData mockData,
			LibrarianViewListener listener) {
		this.librarian = librarian;
		this.demoMode = demoMode;
		this.mockData = mockData;
		this.listener = listener;

		setLayout(new BorderLayout(0, 14));
		setOpaque(false);
		initUI();
	}

	private void initUI() {
		// --- 3 thẻ thống kê nhanh ---
		JPanel pnlStats = new JPanel(new GridLayout(1, 3, 14, 0));
		pnlStats.setOpaque(false);

		lblStatTotalBooks = new JLabel("0");
		lblStatAvailableBooks = new JLabel("0");
		lblStatIssuedBooks = new JLabel("0");

		pnlStats.add(createStatCard("Tổng số đầu sách", lblStatTotalBooks, UITheme.PRIMARY_COLOR));
		pnlStats.add(createStatCard("Sách có sẵn", lblStatAvailableBooks, UITheme.SUCCESS_COLOR));
		pnlStats.add(createStatCard("Sách đang được mượn", lblStatIssuedBooks, UITheme.WARNING_COLOR));

		// --- Thanh tìm kiếm & bộ lọc ---
		JPanel pnlSearchCard = UITheme.createWhiteCardPanel(new BorderLayout(10, 0));
		pnlSearchCard.setBorder(new EmptyBorder(12, 14, 12, 14));

		txtSearchBook = UITheme.createStyledTextField("", "Nhập tên sách, tác giả hoặc chủ đề...");
		txtSearchBook.setPreferredSize(new Dimension(0, 36));
		txtSearchBook.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchBooksAction();
				}
			}
		});

		cboSearchType = new JComboBox<>(new String[] {
				"Tất cả", "Tên sách", "Tác giả", "Chủ đề"
		});
		cboSearchType.setFont(UITheme.FONT_REGULAR);
		cboSearchType.setPreferredSize(new Dimension(120, 36));
		cboSearchType.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

		cboStatusFilter = new JComboBox<>(new String[] {
				"Tất cả", "Có sẵn", "Đang được mượn"
		});
		cboStatusFilter.setFont(UITheme.FONT_REGULAR);
		cboStatusFilter.setPreferredSize(new Dimension(145, 36));
		cboStatusFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		cboStatusFilter.addActionListener(e -> renderBooksTable());

		JButton btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
		btnSearch.setPreferredSize(new Dimension(100, 36));
		btnSearch.addActionListener(e -> searchBooksAction());

		JButton btnRefresh = UITheme.createSecondaryButton("Làm mới");
		btnRefresh.setPreferredSize(new Dimension(90, 36));
		btnRefresh.addActionListener(e -> refreshBooks());

		JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlSearchControls.setOpaque(false);
		pnlSearchControls.add(cboSearchType);
		pnlSearchControls.add(cboStatusFilter);
		pnlSearchControls.add(btnSearch);
		pnlSearchControls.add(btnRefresh);

		pnlSearchCard.add(txtSearchBook, BorderLayout.CENTER);
		pnlSearchCard.add(pnlSearchControls, BorderLayout.EAST);

		JPanel pnlTop = new JPanel(new BorderLayout(0, 12));
		pnlTop.setOpaque(false);
		pnlTop.add(pnlStats, BorderLayout.NORTH);
		pnlTop.add(pnlSearchCard, BorderLayout.SOUTH);

		add(pnlTop, BorderLayout.NORTH);

		// --- Bảng danh sách sách ---
		String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Chủ đề", "Trạng thái" };
		modelBooks = UITheme.createReadOnlyTableModel(columns);
		tblBooks = UITheme.createStyledTable(modelBooks);
		tblBooks.getColumnModel().getColumn(0).setPreferredWidth(80);
		tblBooks.getColumnModel().getColumn(0).setMaxWidth(95);
		tblBooks.getColumnModel().getColumn(1).setPreferredWidth(250);
		tblBooks.getColumnModel().getColumn(4).setPreferredWidth(135);
		tblBooks.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());

		JPanel pnlTableCard = UITheme.createWhiteCardPanel(new BorderLayout(0, 10));
		pnlTableCard.setBorder(new EmptyBorder(12, 14, 12, 14));
		pnlTableCard.add(new JScrollPane(tblBooks), BorderLayout.CENTER);

		// --- Nút CRUD ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		btnDeleteBook = new JButton("Xóa sách");
		btnDeleteBook.setFont(UITheme.FONT_BOLD);
		btnDeleteBook.setPreferredSize(new Dimension(115, 36));
		btnDeleteBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnDeleteBook.putClientProperty(FlatClientProperties.STYLE,
				String.format("arc: 8; background: #%06X; foreground: #FFFFFF; borderWidth: 0",
						UITheme.DANGER_COLOR.getRGB() & 0xFFFFFF));
		btnDeleteBook.addActionListener(e -> deleteBookAction());

		btnEditBook = UITheme.createSecondaryButton("Chỉnh sửa sách");
		btnEditBook.setPreferredSize(new Dimension(140, 36));
		btnEditBook.addActionListener(e -> editBookAction());

		btnAddBook = UITheme.createPrimaryButton("Thêm sách mới");
		btnAddBook.setPreferredSize(new Dimension(145, 36));
		btnAddBook.addActionListener(e -> addBookAction());

		pnlActions.add(btnDeleteBook);
		pnlActions.add(btnEditBook);
		pnlActions.add(btnAddBook);
		pnlTableCard.add(pnlActions, BorderLayout.SOUTH);

		add(pnlTableCard, BorderLayout.CENTER);
	}

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

	public void updateBookStats(int totalBooks, int availableBooks, int issuedBooks) {
		lblStatTotalBooks.setText(String.valueOf(totalBooks));
		lblStatAvailableBooks.setText(String.valueOf(availableBooks));
		lblStatIssuedBooks.setText(String.valueOf(issuedBooks));
	}

	public void refreshBooks() {
		txtSearchBook.setText("");
		cboSearchType.setSelectedIndex(0);
		cboStatusFilter.setSelectedIndex(0);
		loadBooksAsync("ALL", "");
	}

	public void loadBooksAsync(String mode, String keyword) {
		listener.onStatusUpdate("Đang tải danh sách kho sách...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<Book> result;
				List<Book> allForStats = null;
				if (demoMode) {
					result = mockData.filterMockBooks(mode, keyword);
					allForStats = mockData.getAllMockBooks();
				} else {
					result = switch (mode) {
						case "TITLE" -> DatabaseOperations.getBooksByTitle(keyword);
						case "AUTHOR" -> DatabaseOperations.getBooksByAuthor(keyword);
						case "SUBJECT" -> DatabaseOperations.getBooksBySubject(keyword);
						default -> DatabaseOperations.getAllBooks();
					};
					allForStats = "ALL".equals(mode) ? result : DatabaseOperations.getAllBooks();
				}

				final List<Book> loadedBooks = result != null ? result : new ArrayList<>();
				final List<Book> statsBooks = allForStats != null ? allForStats : loadedBooks;
				SwingUtilities.invokeLater(() -> {
					currentBooks = loadedBooks;
					renderBooksTable();
					notifyStats(statsBooks);
					listener.onStatusUpdate("Đã tải " + displayedBooks.size() + " đầu sách.", UITheme.TEXT_MUTED);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					listener.onStatusUpdate("Lỗi tải kho sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể tải danh sách sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void notifyStats(List<Book> allBooks) {
		int total = allBooks.size();
		int issued = 0;
		for (Book b : allBooks) {
			if (b.isIssued()) {
				issued++;
			}
		}
		listener.onBookStatsChanged(total, total - issued, issued);
	}

	// =========================================================================
	// XỬ LÝ TÌM KIẾM & CRUD SÁCH
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

	private void addBookAction() {
		JTextField txtTitle = UITheme.createStyledTextField("", "Nhập tên sách...");
		JTextField txtAuthor = UITheme.createStyledTextField("", "Nhập tên tác giả...");
		JTextField txtSubject = UITheme.createStyledTextField("", "Nhập chủ đề / thể loại...");

		Object[] fields = {
				"Tên sách:", txtTitle,
				"Tác giả:", txtAuthor,
				"Chủ đề:", txtSubject
		};

		int result = JOptionPane.showConfirmDialog(this, fields,
				"Thêm sách mới vào thư viện", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String title = txtTitle.getText().trim();
		String author = txtAuthor.getText().trim();
		String subject = txtSubject.getText().trim();

		if (title.isEmpty() || author.isEmpty() || subject.isEmpty()) {
			UITheme.showWarning(this, "Vui lòng nhập đầy đủ Tên sách, Tác giả và Chủ đề!");
			return;
		}

		btnAddBook.setEnabled(false);
		listener.onStatusUpdate("Đang thêm sách mới...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.addMockBook(title, author, subject);
				} else {
					librarian.createBook(title, author, subject);
				}

				SwingUtilities.invokeLater(() -> {
					btnAddBook.setEnabled(true);
					listener.onStatusUpdate("Thêm sách mới thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã thêm đầu sách \"" + title + "\" thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnAddBook.setEnabled(true);
					listener.onStatusUpdate("Lỗi thêm sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể thêm sách mới:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void editBookAction() {
		int selectedRow = tblBooks.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= displayedBooks.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một cuốn sách trong bảng để chỉnh sửa!");
			return;
		}

		Book selected = displayedBooks.get(selectedRow);
		JLabel lblBookId = UITheme.createFormLabel("Mã sách: #" + selected.getBookId());
		lblBookId.setFont(UITheme.FONT_BOLD);
		lblBookId.setForeground(UITheme.PRIMARY_COLOR);

		JTextField txtTitle = UITheme.createStyledTextField(selected.getTitle(), "Nhập tên sách...");
		JTextField txtAuthor = UITheme.createStyledTextField(selected.getAuthor(), "Nhập tác giả...");
		JTextField txtSubject = UITheme.createStyledTextField(selected.getSubject(), "Nhập chủ đề...");
		JCheckBox chkIssued = new JCheckBox("Đang được mượn", selected.isIssued());
		chkIssued.setFont(UITheme.FONT_REGULAR);

		Object[] fields = {
				lblBookId,
				"Tên sách:", txtTitle,
				"Tác giả:", txtAuthor,
				"Chủ đề:", txtSubject,
				"Trạng thái:", chkIssued
		};

		int result = JOptionPane.showConfirmDialog(this, fields,
				"Chỉnh sửa thông tin sách", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String newTitle = txtTitle.getText().trim();
		String newAuthor = txtAuthor.getText().trim();
		String newSubject = txtSubject.getText().trim();
		boolean newIssued = chkIssued.isSelected();

		if (newTitle.isEmpty() || newAuthor.isEmpty() || newSubject.isEmpty()) {
			UITheme.showWarning(this, "Tên sách, tác giả và chủ đề không được để trống!");
			return;
		}

		btnEditBook.setEnabled(false);
		listener.onStatusUpdate("Đang cập nhật thông tin sách...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.updateMockBook(selected.getBookId(), newTitle, newAuthor, newSubject, newIssued);
				} else {
					librarian.updateBook(selected.getBookId(), newTitle, newAuthor, newSubject, newIssued);
				}

				SwingUtilities.invokeLater(() -> {
					btnEditBook.setEnabled(true);
					listener.onStatusUpdate("Cập nhật thông tin sách thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã cập nhật sách #" + selected.getBookId() + " thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnEditBook.setEnabled(true);
					listener.onStatusUpdate("Lỗi cập nhật sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể cập nhật thông tin sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void deleteBookAction() {
		int selectedRow = tblBooks.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= displayedBooks.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một cuốn sách cần xóa!");
			return;
		}

		Book selected = displayedBooks.get(selectedRow);
		if (selected.isIssued()) {
			UITheme.showWarning(this,
					"Không thể xóa cuốn sách \"" + selected.getTitle() + "\" vì sách đang được mượn!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Bạn chắc chắn muốn xóa cuốn sách:\n\"" + selected.getTitle() + "\" (Mã #" + selected.getBookId()
						+ ")?",
				"Xác nhận xóa sách", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnDeleteBook.setEnabled(false);
		listener.onStatusUpdate("Đang xóa sách...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.deleteMockBook(selected.getBookId());
				} else {
					librarian.deleteBook(selected.getBookId());
				}

				SwingUtilities.invokeLater(() -> {
					btnDeleteBook.setEnabled(true);
					listener.onStatusUpdate("Đã xóa sách #" + selected.getBookId() + ".", UITheme.SUCCESS_COLOR);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnDeleteBook.setEnabled(true);
					listener.onStatusUpdate("Lỗi xóa sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể xóa sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void renderBooksTable() {
		modelBooks.setRowCount(0);
		displayedBooks = new ArrayList<>();
		int statusIdx = cboStatusFilter != null ? cboStatusFilter.getSelectedIndex() : 0;

		for (Book b : currentBooks) {
			if (statusIdx == 1 && b.isIssued()) {
				continue;
			}
			if (statusIdx == 2 && !b.isIssued()) {
				continue;
			}
			displayedBooks.add(b);
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
