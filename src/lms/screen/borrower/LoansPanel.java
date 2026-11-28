package lms.screen.borrower;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import lms.object.Loan;
import lms.person.Borrower;
import lms.screen.util.UITheme;

/**
 * Panel hiển thị lịch sử phiếu mượn kèm bộ lọc trạng thái (Tất cả / Đang mượn / Đã trả).
 */
public final class LoansPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerMockData mockData;
	private final transient BorrowerViewListener listener;

	private transient List<Loan> currentLoans = new ArrayList<>();

	private JComboBox<String> cboLoanFilter;
	private JTable tblLoans;
	private DefaultTableModel modelLoans;
	private JButton btnRefreshLoans;

	public LoansPanel(Borrower borrower, boolean demoMode, BorrowerMockData mockData,
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
		// --- Phần đầu: Mô tả, Bộ lọc trạng thái & Nút làm mới ---
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);

		JLabel lblDesc = new JLabel("Danh sách các phiếu mượn gắn với tài khoản của bạn");
		lblDesc.setFont(UITheme.FONT_REGULAR);
		lblDesc.setForeground(UITheme.TEXT_MUTED);

		JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlRight.setOpaque(false);

		cboLoanFilter = new JComboBox<>(new String[] { "Tất cả", "Đang mượn", "Đã trả" });
		cboLoanFilter.setFont(UITheme.FONT_REGULAR);
		cboLoanFilter.setPreferredSize(new Dimension(130, 34));
		cboLoanFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		cboLoanFilter.addActionListener(e -> renderLoansTable());

		btnRefreshLoans = UITheme.createSecondaryButton("Làm mới");
		btnRefreshLoans.setPreferredSize(new Dimension(100, 34));
		btnRefreshLoans.addActionListener(e -> loadLoansAsync());

		pnlRight.add(new JLabel("Lọc:"));
		pnlRight.add(cboLoanFilter);
		pnlRight.add(btnRefreshLoans);

		pnlTop.add(lblDesc, BorderLayout.WEST);
		pnlTop.add(pnlRight, BorderLayout.EAST);

		// --- Bảng dữ liệu (bọc trong JScrollPane) ---
		String[] columns = { "Mã phiếu", "Mã sách", "Ngày mượn", "Ngày trả", "Trạng thái", "Tiền phạt" };
		modelLoans = UITheme.createReadOnlyTableModel(columns);
		tblLoans = UITheme.createStyledTable(modelLoans);
		tblLoans.getColumnModel().getColumn(0).setMaxWidth(95);
		tblLoans.getColumnModel().getColumn(1).setMaxWidth(95);
		tblLoans.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());
		tblLoans.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());

		add(pnlTop, BorderLayout.NORTH);
		add(new JScrollPane(tblLoans), BorderLayout.CENTER);
	}

	public void loadLoansAsync() {
		new Thread(() -> {
			try {
				List<Loan> result = demoMode ? mockData.createMockLoans() : borrower.getLoans();
				SwingUtilities.invokeLater(() -> {
					currentLoans = result != null ? result : new ArrayList<>();
					renderLoansTable();
					listener.onLoansCountChanged(currentLoans.size());
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> listener
						.onStatusUpdate("Lỗi tải lịch sử mượn: " + ex.getMessage(), UITheme.DANGER_COLOR));
			}
		}).start();
	}

	private void renderLoansTable() {
		modelLoans.setRowCount(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		int filterIdx = cboLoanFilter != null ? cboLoanFilter.getSelectedIndex() : 0;

		for (Loan l : currentLoans) {
			boolean returned = l.getDateReturned() != null;
			if (filterIdx == 1 && returned) {
				continue;
			}
			if (filterIdx == 2 && !returned) {
				continue;
			}

			String issuedStr = l.getIssuedDate() != null ? sdf.format(l.getIssuedDate()) : "-";
			String returnedStr = returned ? sdf.format(l.getDateReturned()) : "Chưa trả";
			String statusStr = returned ? "Đã trả" : "Đang mượn";
			String fineStr = l.isFinePaid() ? "Đã thanh toán" : "Chưa thanh toán";

			modelLoans.addRow(new Object[] {
					l.getLoanId(),
					l.getBookId(),
					issuedStr,
					returnedStr,
					statusStr,
					fineStr
			});
		}
	}
}
