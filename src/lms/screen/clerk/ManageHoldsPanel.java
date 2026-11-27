package lms.screen.clerk;

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
import lms.person.Clerk;
import lms.screen.util.UITheme;

/**
 * Panel xử lý yêu cầu đặt giữ sách: cho mượn theo đặt giữ hoặc hủy yêu cầu.
 */
public final class ManageHoldsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Clerk clerk;
	private final boolean demoMode;
	private final transient ClerkMockData mockData;
	private final transient ClerkViewListener listener;

	private transient List<HoldRequest> currentHoldRequests = new ArrayList<>();

	private JTable tblHoldRequests;
	private DefaultTableModel modelHoldRequests;
	private JButton btnFulfillHold;
	private JButton btnCancelHold;

	public ManageHoldsPanel(Clerk clerk, boolean demoMode, ClerkMockData mockData,
			ClerkViewListener listener) {
		this.clerk = clerk;
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
		// --- Phần đầu ---
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.setOpaque(false);

		JLabel lblDesc = new JLabel("Danh sách yêu cầu đặt giữ sách đang chờ xử lý");
		lblDesc.setFont(UITheme.FONT_REGULAR);
		lblDesc.setForeground(UITheme.TEXT_MUTED);

		JButton btnRefresh = UITheme.createSecondaryButton("Làm mới");
		btnRefresh.setPreferredSize(new Dimension(100, 34));
		btnRefresh.addActionListener(e -> loadHoldRequestsAsync());

		pnlTop.add(lblDesc, BorderLayout.WEST);
		pnlTop.add(btnRefresh, BorderLayout.EAST);

		// --- Bảng ---
		String[] columns = { "Mã yêu cầu", "Mã sách", "Mã ĐG", "Ngày yêu cầu" };
		modelHoldRequests = UITheme.createReadOnlyTableModel(columns);
		tblHoldRequests = UITheme.createStyledTable(modelHoldRequests);
		tblHoldRequests.getColumnModel().getColumn(0).setMaxWidth(100);
		tblHoldRequests.getColumnModel().getColumn(1).setMaxWidth(85);
		tblHoldRequests.getColumnModel().getColumn(2).setMaxWidth(85);

		// --- Nút chức năng ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		btnCancelHold = new JButton("Hủy yêu cầu");
		btnCancelHold.setFont(UITheme.FONT_BOLD);
		btnCancelHold.setPreferredSize(new Dimension(135, 36));
		btnCancelHold.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnCancelHold.putClientProperty(FlatClientProperties.STYLE,
				"arc: 8; background: #DC3545; foreground: #FFFFFF; borderWidth: 0");
		btnCancelHold.addActionListener(e -> cancelHoldAction());

		btnFulfillHold = UITheme.createPrimaryButton("Cho mượn theo đặt giữ");
		btnFulfillHold.setPreferredSize(new Dimension(195, 36));
		btnFulfillHold.addActionListener(e -> fulfillHoldAction());

		pnlActions.add(btnCancelHold);
		pnlActions.add(btnFulfillHold);

		add(pnlTop, BorderLayout.NORTH);
		add(new JScrollPane(tblHoldRequests), BorderLayout.CENTER);
		add(pnlActions, BorderLayout.SOUTH);
	}

	// =========================================================================
	// TẢI DỮ LIỆU
	// =========================================================================

	public void loadHoldRequestsAsync() {
		listener.onStatusUpdate("Đang tải yêu cầu đặt giữ...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<HoldRequest> result;
				if (demoMode) {
					result = mockData.getAllMockHoldRequests();
				} else {
					result = DatabaseOperations.getAllHoldRequests();
				}
				SwingUtilities.invokeLater(() -> {
					currentHoldRequests = result != null ? result : new ArrayList<>();
					renderHoldRequestsTable();
					listener.onPendingHoldsCountChanged(currentHoldRequests.size());
					listener.onStatusUpdate("Đã tải " + currentHoldRequests.size() + " yêu cầu đặt giữ.",
							UITheme.TEXT_MUTED);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> listener
						.onStatusUpdate("Lỗi tải yêu cầu đặt giữ: " + ex.getMessage(), UITheme.DANGER_COLOR));
			}
		}).start();
	}

	// =========================================================================
	// XỬ LÝ SỰ KIỆN
	// =========================================================================

	private void fulfillHoldAction() {
		int selectedRow = tblHoldRequests.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= currentHoldRequests.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một yêu cầu đặt giữ để cho mượn!");
			return;
		}

		HoldRequest req = currentHoldRequests.get(selectedRow);
		int confirm = JOptionPane.showConfirmDialog(this,
				"Xác nhận cho mượn sách #" + req.getBookId() + " cho Độc giả #" + req.getBorrowerId()
						+ "?\n(Yêu cầu đặt giữ #" + req.getHoldRequestId() + " sẽ bị xóa)",
				"Cho mượn theo đặt giữ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		btnFulfillHold.setEnabled(false);
		listener.onStatusUpdate("Đang xử lý cho mượn theo đặt giữ...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.addMockLoan(req.getBookId(), req.getBorrowerId());
					mockData.removeMockHoldRequest(selectedRow);
				} else {
					clerk.checkOut(req.getBorrowerId(), req.getBookId());
					DatabaseOperations.deleteHoldRequest(req.getHoldRequestId());
				}

				SwingUtilities.invokeLater(() -> {
					btnFulfillHold.setEnabled(true);
					listener.onStatusUpdate("Cho mượn theo đặt giữ thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã lập phiếu mượn sách #" + req.getBookId()
									+ " cho Độc giả #" + req.getBorrowerId() + ".",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					listener.onTransactionCompleted();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnFulfillHold.setEnabled(true);
					listener.onStatusUpdate("Lỗi xử lý: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể xử lý cho mượn:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void cancelHoldAction() {
		int selectedRow = tblHoldRequests.getSelectedRow();
		if (selectedRow < 0 || selectedRow >= currentHoldRequests.size()) {
			UITheme.showWarning(this, "Vui lòng chọn một yêu cầu đặt giữ cần hủy!");
			return;
		}

		HoldRequest req = currentHoldRequests.get(selectedRow);
		int confirm = JOptionPane.showConfirmDialog(this,
				"Bạn chắc chắn muốn hủy yêu cầu đặt giữ #" + req.getHoldRequestId() + "?",
				"Xác nhận hủy đặt giữ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

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
					hr.getBorrowerId(),
					dateStr
			});
		}
	}
}
