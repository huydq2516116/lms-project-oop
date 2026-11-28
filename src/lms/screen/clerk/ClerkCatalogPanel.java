package lms.screen.clerk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
import lms.screen.util.UITheme;

/**
 * Panel tra cứu kho sách: tìm kiếm theo tên, tác giả, chủ đề và xem trạng thái (chỉ xem).
 */
public final class ClerkCatalogPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final boolean demoMode;
	private final transient ClerkMockData mockData;
	private final transient ClerkViewListener listener;

	private transient List<Book> currentBooks = new ArrayList<>();

	private JTextField txtSearchBook;
	private JComboBox<String> cboSearchType;
	private JTable tblBooks;
	private DefaultTableModel modelBooks;

	public ClerkCatalogPanel(boolean demoMode, ClerkMockData mockData,
			ClerkViewListener listener) {
		this.demoMode = demoMode;
		this.mockData = mockData;
		this.listener = listener;

		setLayout(new BorderLayout(0, 12));
		setOpaque(false);
		initUI();
	}

	private void initUI() {
		// --- Thanh tìm kiếm ---
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

		JButton btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
		btnSearch.setPreferredSize(new Dimension(105, 36));
		btnSearch.addActionListener(e -> searchBooksAction());

		JButton btnRefresh = UITheme.createSecondaryButton("Làm mới");
		btnRefresh.setPreferredSize(new Dimension(95, 36));
		btnRefresh.addActionListener(e -> refreshCatalog());

		JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlSearchControls.setOpaque(false);
		pnlSearchControls.add(cboSearchType);
		pnlSearchControls.add(btnSearch);
		pnlSearchControls.add(btnRefresh);

		pnlSearchCard.add(txtSearchBook, BorderLayout.CENTER);
		pnlSearchCard.add(pnlSearchControls, BorderLayout.EAST);

		add(pnlSearchCard, BorderLayout.NORTH);

		// --- Bảng sách ---
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

		// --- Nút xem chi tiết ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		JButton btnBookDetail = UITheme.createSecondaryButton("Xem chi tiết");
		btnBookDetail.setPreferredSize(new Dimension(120, 36));
		btnBookDetail.addActionListener(e -> showBookDetailAction());
		pnlActions.add(btnBookDetail);
		pnlTableCard.add(pnlActions, BorderLayout.SOUTH);

		add(pnlTableCard, BorderLayout.CENTER);
	}

	// =========================================================================
	// TẢI DỮ LIỆU
	// =========================================================================

	public void loadBooksAsync(String mode, String keyword) {
		listener.onStatusUpdate("Đang tải danh sách sách...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<Book> result;
				if (demoMode) {
					result = mockData.filterMockBooks(mode, keyword);
				} else {
					result = switch (mode) {
						case "TITLE" -> DatabaseOperations.getBooksByTitle(keyword);
						case "AUTHOR" -> DatabaseOperations.getBooksByAuthor(keyword);
						case "SUBJECT" -> DatabaseOperations.getBooksBySubject(keyword);
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
					UITheme.showError(this, "Không thể tải danh sách sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	public void refreshCatalog() {
		txtSearchBook.setText("");
		cboSearchType.setSelectedIndex(0);
		loadBooksAsync("ALL", "");
	}

	// =========================================================================
	// XỬ LÝ SỰ KIỆN
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
