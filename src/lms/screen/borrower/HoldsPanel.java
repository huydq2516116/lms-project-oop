package lms.screen.borrower;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

import lms.database.DatabaseOperations;
import lms.object.HoldRequest;
import lms.person.Borrower;
import lms.screen.util.UITheme;

/**
 * Panel quản lý danh sách yêu cầu đặt giữ sách và thao tác hủy đặt giữ.
 */
public final class HoldsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerMockData mockData;
	private final transient BorrowerViewListener listener;

	private transient List<HoldRequest> currentHoldRequests = new ArrayList<>();

	private JTable tblHoldRequests;
	private DefaultTableModel modelHoldRequests;
	private JButton btnCancelHold;
	private JButton btnRefreshHolds;

	public HoldsPanel(Borrower borrower, boolean demoMode, BorrowerMockData mockData,
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

		JLabel lblDesc = new JLabel("Danh sách yêu cầu giữ sách bạn đã đăng ký");
		lblDesc.setFont(UITheme.FONT_REGULAR);
		lblDesc.setForeground(UITheme.TEXT_MUTED);

		btnRefreshHolds = UITheme.createSecondaryButton("Làm mới");
		btnRefreshHolds.setPreferredSize(new Dimension(100, 34));
		btnRefreshHolds.addActionListener(e -> loadHoldRequestsAsync());

		pnlTop.add(lblDesc, BorderLayout.WEST);
		pnlTop.add(btnRefreshHolds, BorderLayout.EAST);

		// --- Bảng dữ liệu (bọc trong JScrollPane) ---
		String[] columns = { "Mã yêu cầu", "Mã sách", "Ngày yêu cầu" };
		modelHoldRequests = UITheme.createReadOnlyTableModel(columns);
		tblHoldRequests = UITheme.createStyledTable(modelHoldRequests);

		// --- Panel nút chức năng ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		pnlActions.setOpaque(false);

		btnCancelHold = new JButton("Hủy yêu cầu đặt giữ");
		btnCancelHold.setFont(UITheme.FONT_BOLD);
		btnCancelHold.setPreferredSize(new Dimension(175, 36));
		btnCancelHold.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnCancelHold.putClientProperty(FlatClientProperties.STYLE,
				"arc: 8; background: #DC3545; foreground: #FFFFFF; borderWidth: 0");
		btnCancelHold.addActionListener(e -> cancelHoldRequestAction());
		pnlActions.add(btnCancelHold);

		add(pnlTop, BorderLayout.NORTH);
		add(new JScrollPane(tblHoldRequests), BorderLayout.CENTER);
		add(pnlActions, BorderLayout.SOUTH);
	}

	public void loadHoldRequestsAsync() {
		new Thread(() -> {
			try {
				List<HoldRequest> result = demoMode
						? mockData.createMockHoldRequests()
						: DatabaseOperations.getHoldRequestsByBorrowerId(borrower.getId());
				SwingUtilities.invokeLater(() -> {
					currentHoldRequests = result != null ? result : new ArrayList<>();
					renderHoldRequestsTable();
					listener.onHoldsCountChanged(currentHoldRequests.size());
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> listener
						.onStatusUpdate("Lỗi tải yêu cầu đặt giữ: " + ex.getMessage(), UITheme.DANGER_COLOR));
			}
		}).start();
	}

	private void cancelHoldRequestAction() {
		int selectedRow = tblHoldRequests.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= currentHoldRequests.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một yêu cầu đặt giữ cần hủy!");
			return;
		}

		HoldRequest req = currentHoldRequests.get(selectedRow);
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Bạn chắc chắn muốn hủy yêu cầu đặt giữ #" + req.getHoldRequestId() + "?",
				"Xác nhận hủy đặt giữ",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnCancelHold.setEnabled(false);
		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.removeMockHoldRequest(selectedRow);
				} else {
					DatabaseOperations.deleteHoldRequest(req.getHoldRequestId());
				}

				SwingUtilities.invokeLater(() -> {
					btnCancelHold.setEnabled(true);
					listener.onStatusUpdate("Đã hủy yêu cầu đặt giữ.", UITheme.SUCCESS_COLOR);
					loadHoldRequestsAsync();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnCancelHold.setEnabled(true);
					listener.onStatusUpdate("Lỗi hủy đặt giữ: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể hủy yêu cầu đặt giữ:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void renderHoldRequestsTable() {
		modelHoldRequests.setRowCount(0);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		for (HoldRequest hr : currentHoldRequests) {
			String dateStr = hr.getRequestDate() != null ? sdf.format(hr.getRequestDate()) : "-";
			modelHoldRequests.addRow(new Object[] {
					hr.getHoldRequestId(),
					hr.getBookId(),
					dateStr
			});
		}
	}
}
