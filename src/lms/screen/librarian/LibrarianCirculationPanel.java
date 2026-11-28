package lms.screen.librarian;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import lms.object.Loan;
import lms.person.Librarian;
import lms.screen.util.UITheme;

/**
 * Panel Quản lý Mượn & Trả sách dành cho Thủ thư.
 * Cho phép lọc phiếu mượn, lập phiếu mượn mới, nhận trả sách và ghi nhận thanh toán phạt.
 */
public final class LibrarianCirculationPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Librarian librarian;
	private final boolean demoMode;
	private final transient LibrarianMockData mockData;
	private final transient LibrarianViewListener listener;

	private transient List<Loan> currentLoans = new ArrayList<>();

	private JTextField txtSearchLoan;
	private JComboBox<String> cboLoanFilter;
	private JTable tblLoans;
	private DefaultTableModel modelLoans;

	private JButton btnIssueLoan;
	private JButton btnReturnBook;
	private JButton btnPayFine;

	public LibrarianCirculationPanel(Librarian librarian, boolean demoMode, LibrarianMockData mockData,
			LibrarianViewListener listener) {
		this.librarian = librarian;
		this.demoMode = demoMode;
		this.mockData = mockData;
		this.listener = listener;

		setLayout(new BorderLayout(0, 12));
		setOpaque(false);
		initUI();
	}

	private void initUI() {
		// --- Thanh tìm kiếm & bộ lọc ---
		JPanel pnlSearchCard = UITheme.createWhiteCardPanel(new BorderLayout(10, 0));
		pnlSearchCard.setBorder(new EmptyBorder(12, 14, 12, 14));

		txtSearchLoan = UITheme.createStyledTextField("", "Tìm theo mã phiếu, mã sách hoặc mã độc giả...");
		txtSearchLoan.setPreferredSize(new Dimension(0, 36));
		txtSearchLoan.addActionListener(e -> renderLoansTable());

		cboLoanFilter = new JComboBox<>(new String[] {
				"Tất cả", "Đang mượn", "Đã trả", "Chưa thanh toán phạt"
		});
		cboLoanFilter.setFont(UITheme.FONT_REGULAR);
		cboLoanFilter.setPreferredSize(new Dimension(180, 36));
		cboLoanFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		cboLoanFilter.addActionListener(e -> renderLoansTable());

		JButton btnRefresh = UITheme.createSecondaryButton("Làm mới");
		btnRefresh.setPreferredSize(new Dimension(95, 36));
		btnRefresh.addActionListener(e -> loadLoansAsync());

		JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlSearchControls.setOpaque(false);
		pnlSearchControls.add(cboLoanFilter);
		pnlSearchControls.add(btnRefresh);

		pnlSearchCard.add(txtSearchLoan, BorderLayout.CENTER);
		pnlSearchCard.add(pnlSearchControls, BorderLayout.EAST);

		add(pnlSearchCard, BorderLayout.NORTH);

		// --- Bảng phiếu mượn ---
		String[] columns = { "Mã phiếu", "Mã sách", "Mã ĐG", "Ngày mượn", "Ngày trả", "Trạng thái", "Tiền phạt" };
		modelLoans = UITheme.createReadOnlyTableModel(columns);
		tblLoans = UITheme.createStyledTable(modelLoans);
		tblLoans.getColumnModel().getColumn(0).setMaxWidth(85);
		tblLoans.getColumnModel().getColumn(1).setMaxWidth(85);
		tblLoans.getColumnModel().getColumn(2).setMaxWidth(85);
		tblLoans.getColumnModel().getColumn(5).setPreferredWidth(120);
		tblLoans.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());
		tblLoans.getColumnModel().getColumn(6).setPreferredWidth(130);
		tblLoans.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());

		JPanel pnlTableCard = UITheme.createWhiteCardPanel(new BorderLayout(0, 10));
		pnlTableCard.setBorder(new EmptyBorder(12, 14, 12, 14));
		pnlTableCard.add(new JScrollPane(tblLoans), BorderLayout.CENTER);

		// --- Nút chức năng ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		btnPayFine = UITheme.createSecondaryButton("Xác nhận thanh toán phạt");
		btnPayFine.setPreferredSize(new Dimension(195, 36));
		btnPayFine.addActionListener(e -> payFineAction());

		btnReturnBook = UITheme.createSecondaryButton("Nhận trả sách");
		btnReturnBook.setPreferredSize(new Dimension(135, 36));
		btnReturnBook.addActionListener(e -> returnBookAction());

		btnIssueLoan = UITheme.createPrimaryButton("Lập phiếu mượn mới");
		btnIssueLoan.setPreferredSize(new Dimension(175, 36));
		btnIssueLoan.addActionListener(e -> issueLoanAction());

		pnlActions.add(btnPayFine);
		pnlActions.add(btnReturnBook);
		pnlActions.add(btnIssueLoan);
		pnlTableCard.add(pnlActions, BorderLayout.SOUTH);

		add(pnlTableCard, BorderLayout.CENTER);
	}

	// =========================================================================
	// TẢI DỮ LIỆU
	// =========================================================================

	public void loadLoansAsync() {
		listener.onStatusUpdate("Đang tải danh sách phiếu mượn...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<Loan> result = demoMode
						? mockData.getAllMockLoans()
						: DatabaseOperations.getAllLoans();
				SwingUtilities.invokeLater(() -> {
					currentLoans = result != null ? result : new ArrayList<>();
					renderLoansTable();
					listener.onStatusUpdate("Đã tải " + currentLoans.size() + " phiếu mượn.", UITheme.TEXT_MUTED);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					listener.onStatusUpdate("Lỗi tải phiếu mượn: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể tải danh sách phiếu mượn:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	// =========================================================================
	// XỬ LÝ NGHIỆP VỤ
	// =========================================================================

	private void issueLoanAction() {
		JTextField txtBorrowerId = UITheme.createStyledTextField("", "Mã độc giả (số)");
		JTextField txtBookId = UITheme.createStyledTextField("", "Mã sách (số)");

		Object[] fields = {
				"Mã độc giả:", txtBorrowerId,
				"Mã sách:", txtBookId
		};

		int result = JOptionPane.showConfirmDialog(this, fields,
				"Lập phiếu mượn mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		int borrowerId;
		int bookId;
		try {
			borrowerId = Integer.parseInt(txtBorrowerId.getText().trim());
			bookId = Integer.parseInt(txtBookId.getText().trim());
		} catch (NumberFormatException ex) {
			UITheme.showWarning(this, "Mã độc giả và mã sách phải là số nguyên hợp lệ!");
			return;
		}

		btnIssueLoan.setEnabled(false);
		listener.onStatusUpdate("Đang lập phiếu mượn...", UITheme.PRIMARY_COLOR);

		final int bId = borrowerId;
		final int bkId = bookId;
		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.addMockLoan(bkId, bId);
				} else {
					DatabaseOperations.createLoan(bkId, bId, librarian.getId());
				}

				SwingUtilities.invokeLater(() -> {
					btnIssueLoan.setEnabled(true);
					listener.onStatusUpdate("Lập phiếu mượn thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã lập phiếu mượn cho Độc giả #" + bId + ", Sách #" + bkId + ".",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnIssueLoan.setEnabled(true);
					listener.onStatusUpdate("Lỗi lập phiếu mượn: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể lập phiếu mượn:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void returnBookAction() {
		List<Loan> filtered = getFilteredLoans();
		int selectedRow = tblLoans.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= filtered.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một phiếu mượn trong bảng để nhận trả sách!");
			return;
		}

		Loan selectedLoan = filtered.get(selectedRow);
		if (selectedLoan.getDateReturned() != null) {
			UITheme.showWarning(this, "Phiếu mượn #" + selectedLoan.getLoanId() + " đã được trả rồi!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Xác nhận nhận trả sách cho phiếu mượn #" + selectedLoan.getLoanId() + "?",
				"Xác nhận trả sách", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnReturnBook.setEnabled(false);
		listener.onStatusUpdate("Đang xử lý trả sách...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.returnMockLoan(selectedLoan.getLoanId());
				} else {
					librarian.checkIn(selectedLoan.getLoanId());
				}

				SwingUtilities.invokeLater(() -> {
					btnReturnBook.setEnabled(true);
					listener.onStatusUpdate("Nhận trả sách thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã nhận trả sách cho phiếu mượn #" + selectedLoan.getLoanId() + ".",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnReturnBook.setEnabled(true);
					listener.onStatusUpdate("Lỗi trả sách: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể xử lý trả sách:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void payFineAction() {
		List<Loan> filtered = getFilteredLoans();
		int selectedRow = tblLoans.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= filtered.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một phiếu mượn để xác nhận thanh toán phạt!");
			return;
		}

		Loan selectedLoan = filtered.get(selectedRow);
		if (selectedLoan.isFinePaid()) {
			UITheme.showWarning(this, "Phiếu mượn #" + selectedLoan.getLoanId() + " đã thanh toán phạt rồi!");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this,
				"Xác nhận ghi nhận thanh toán phạt cho phiếu #" + selectedLoan.getLoanId() + "?",
				"Xác nhận thanh toán phạt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnPayFine.setEnabled(false);
		listener.onStatusUpdate("Đang ghi nhận thanh toán phạt...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.payFineMockLoan(selectedLoan.getLoanId());
				} else {
					DatabaseOperations.updateFineStatus(selectedLoan.getLoanId());
				}

				SwingUtilities.invokeLater(() -> {
					btnPayFine.setEnabled(true);
					listener.onStatusUpdate("Ghi nhận thanh toán phạt thành công!", UITheme.SUCCESS_COLOR);
					listener.onDataChanged();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnPayFine.setEnabled(true);
					listener.onStatusUpdate("Lỗi thanh toán phạt: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể ghi nhận thanh toán phạt:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	// =========================================================================
	// RENDER BẢNG
	// =========================================================================

	private List<Loan> getFilteredLoans() {
		int filterIdx = cboLoanFilter != null ? cboLoanFilter.getSelectedIndex() : 0;
		String keyword = txtSearchLoan != null ? txtSearchLoan.getText().trim().toLowerCase() : "";

		List<Loan> filtered = new ArrayList<>();
		for (Loan l : currentLoans) {
			boolean returned = l.getDateReturned() != null;
			if (filterIdx == 1 && returned) {
				continue;
			}
			if (filterIdx == 2 && !returned) {
				continue;
			}
			if (filterIdx == 3 && l.isFinePaid()) {
				continue;
			}

			if (!keyword.isEmpty()) {
				boolean matchesSearch = String.valueOf(l.getLoanId()).contains(keyword)
						|| String.valueOf(l.getBookId()).contains(keyword)
						|| String.valueOf(l.getBorrowerId()).contains(keyword);
				if (!matchesSearch) {
					continue;
				}
			}
			filtered.add(l);
		}
		return filtered;
	}

	private void renderLoansTable() {
		modelLoans.setRowCount(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		for (Loan l : getFilteredLoans()) {
			boolean returned = l.getDateReturned() != null;
			String issuedStr = l.getIssuedDate() != null ? sdf.format(l.getIssuedDate()) : "-";
			String returnedStr = returned ? sdf.format(l.getDateReturned()) : "Chưa trả";
			String statusStr = returned ? "Đã trả" : "Đang mượn";
			String fineStr = l.isFinePaid() ? "Đã thanh toán" : "Chưa thanh toán";

			modelLoans.addRow(new Object[] {
					l.getLoanId(),
					l.getBookId(),
					l.getBorrowerId(),
					issuedStr,
					returnedStr,
					statusStr,
					fineStr
			});
		}
	}
}
