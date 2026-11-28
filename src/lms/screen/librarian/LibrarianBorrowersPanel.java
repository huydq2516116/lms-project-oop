package lms.screen.librarian;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import lms.database.DatabaseOperations;
import lms.person.Borrower;
import lms.person.Librarian;
import lms.screen.util.UITheme;

/**
 * Panel Quản lý Độc giả dành cho Thủ thư: tìm kiếm, thêm mới và cập nhật thông tin.
 */
public final class LibrarianBorrowersPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Librarian librarian;
	private final boolean demoMode;
	private final transient LibrarianMockData mockData;
	private final transient LibrarianViewListener listener;

	private JTextField txtSearchBorrower;
	private JTable tblBorrowers;
	private DefaultTableModel modelBorrowers;
	private JButton btnAddBorrower;
	private JButton btnUpdateBorrower;

	public LibrarianBorrowersPanel(Librarian librarian, boolean demoMode, LibrarianMockData mockData,
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
		// --- Thanh tìm kiếm ---
		JPanel pnlSearchCard = UITheme.createWhiteCardPanel(new BorderLayout(10, 0));
		pnlSearchCard.setBorder(new EmptyBorder(12, 14, 12, 14));

		txtSearchBorrower = UITheme.createStyledTextField("", "Tìm kiếm theo tên, username hoặc ID...");
		txtSearchBorrower.setPreferredSize(new Dimension(0, 36));
		txtSearchBorrower.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loadBorrowersAsync();
				}
			}
		});

		JButton btnSearch = UITheme.createPrimaryButton("Tìm kiếm");
		btnSearch.setPreferredSize(new Dimension(105, 36));
		btnSearch.addActionListener(e -> loadBorrowersAsync());

		JButton btnRefresh = UITheme.createSecondaryButton("Làm mới");
		btnRefresh.setPreferredSize(new Dimension(95, 36));
		btnRefresh.addActionListener(e -> {
			txtSearchBorrower.setText("");
			loadBorrowersAsync();
		});

		JPanel pnlSearchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		pnlSearchControls.setOpaque(false);
		pnlSearchControls.add(btnSearch);
		pnlSearchControls.add(btnRefresh);

		pnlSearchCard.add(txtSearchBorrower, BorderLayout.CENTER);
		pnlSearchCard.add(pnlSearchControls, BorderLayout.EAST);

		add(pnlSearchCard, BorderLayout.NORTH);

		// --- Bảng danh sách độc giả ---
		String[] columns = { "ID", "Họ tên", "Username", "Địa chỉ", "Số điện thoại" };
		modelBorrowers = UITheme.createReadOnlyTableModel(columns);
		tblBorrowers = UITheme.createStyledTable(modelBorrowers);
		tblBorrowers.getColumnModel().getColumn(0).setMaxWidth(70);
		tblBorrowers.getColumnModel().getColumn(1).setPreferredWidth(180);
		tblBorrowers.getColumnModel().getColumn(2).setPreferredWidth(130);

		JPanel pnlTableCard = UITheme.createWhiteCardPanel(new BorderLayout(0, 10));
		pnlTableCard.setBorder(new EmptyBorder(12, 14, 12, 14));
		pnlTableCard.add(new JScrollPane(tblBorrowers), BorderLayout.CENTER);

		// --- Nút chức năng ---
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pnlActions.setOpaque(false);

		btnUpdateBorrower = UITheme.createSecondaryButton("Cập nhật thông tin");
		btnUpdateBorrower.setPreferredSize(new Dimension(160, 36));
		btnUpdateBorrower.addActionListener(e -> updateBorrowerAction());

		btnAddBorrower = UITheme.createPrimaryButton("Thêm độc giả mới");
		btnAddBorrower.setPreferredSize(new Dimension(165, 36));
		btnAddBorrower.addActionListener(e -> addBorrowerAction());

		pnlActions.add(btnUpdateBorrower);
		pnlActions.add(btnAddBorrower);
		pnlTableCard.add(pnlActions, BorderLayout.SOUTH);

		add(pnlTableCard, BorderLayout.CENTER);
	}

	// =========================================================================
	// TẢI DỮ LIỆU
	// =========================================================================

	public void loadBorrowersAsync() {
		String keyword = txtSearchBorrower.getText().trim();
		listener.onStatusUpdate("Đang tải danh sách độc giả...", UITheme.PRIMARY_COLOR);
		new Thread(() -> {
			try {
				List<Object[]> result = demoMode
						? mockData.searchMockBorrowers(keyword)
						: DatabaseOperations.searchBorrowers(keyword);
				SwingUtilities.invokeLater(() -> {
					modelBorrowers.setRowCount(0);
					for (Object[] row : result) {
						modelBorrowers.addRow(row);
					}
					listener.onStatusUpdate("Đã tải " + result.size() + " độc giả.", UITheme.TEXT_MUTED);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					listener.onStatusUpdate("Lỗi tải độc giả: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể tải danh sách độc giả:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	// =========================================================================
	// XỬ LÝ SỰ KIỆN
	// =========================================================================

	private void addBorrowerAction() {
		JTextField txtUsername = UITheme.createStyledTextField("", "Tên đăng nhập");
		JTextField txtPassword = UITheme.createStyledTextField("", "Mật khẩu");
		JTextField txtName = UITheme.createStyledTextField("", "Họ và tên");
		JTextField txtAddress = UITheme.createStyledTextField("", "Địa chỉ");
		JTextField txtPhone = UITheme.createStyledTextField("", "Số điện thoại");

		Object[] fields = {
				"Tên đăng nhập:", txtUsername,
				"Mật khẩu:", txtPassword,
				"Họ và tên:", txtName,
				"Địa chỉ:", txtAddress,
				"Số điện thoại:", txtPhone
		};

		int result = JOptionPane.showConfirmDialog(this, fields,
				"Thêm độc giả mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String username = txtUsername.getText().trim();
		String password = txtPassword.getText().trim();
		String name = txtName.getText().trim();
		String address = txtAddress.getText().trim();
		String phone = txtPhone.getText().trim();

		if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
			UITheme.showWarning(this, "Tên đăng nhập, mật khẩu và họ tên là bắt buộc!");
			return;
		}
		if (!phone.isEmpty() && !phone.matches("^[0-9+\\-\\s]{8,15}$")) {
			UITheme.showWarning(this, "Số điện thoại không hợp lệ (chỉ gồm số, 8-15 ký tự)!");
			return;
		}

		btnAddBorrower.setEnabled(false);
		listener.onStatusUpdate("Đang thêm độc giả mới...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.addMockBorrower(name, username, address, phone);
				} else {
					librarian.registerBorrower(username, password, name, address, phone);
				}
				SwingUtilities.invokeLater(() -> {
					btnAddBorrower.setEnabled(true);
					listener.onStatusUpdate("Thêm độc giả thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã thêm độc giả \"" + name + "\" thành công!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadBorrowersAsync();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnAddBorrower.setEnabled(true);
					listener.onStatusUpdate("Lỗi thêm độc giả: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể thêm độc giả:\n" + ex.getMessage());
				});
			}
		}).start();
	}

	private void updateBorrowerAction() {
		int selectedRow = tblBorrowers.getSelectedRow();
		if (selectedRow < 0) {
			UITheme.showWarning(this, "Vui lòng chọn một độc giả để cập nhật!");
			return;
		}

		int borrowerId = (int) modelBorrowers.getValueAt(selectedRow, 0);
		Object nameVal = modelBorrowers.getValueAt(selectedRow, 1);
		Object userVal = modelBorrowers.getValueAt(selectedRow, 2);
		Object addrVal = modelBorrowers.getValueAt(selectedRow, 3);
		Object phoneVal = modelBorrowers.getValueAt(selectedRow, 4);

		String currentName = nameVal != null ? nameVal.toString() : "";
		String currentUsername = userVal != null ? userVal.toString() : "";
		String currentAddress = addrVal != null ? addrVal.toString() : "";
		String currentPhone = phoneVal != null ? phoneVal.toString() : "";

		JLabel lblId = UITheme.createFormLabel("Mã độc giả: #" + borrowerId);
		lblId.setFont(UITheme.FONT_BOLD);
		lblId.setForeground(UITheme.PRIMARY_COLOR);

		JLabel lblName = UITheme.createFormLabel("Họ tên: " + currentName);

		JTextField txtUsername = UITheme.createStyledTextField(currentUsername, "Nhập tên đăng nhập...");
		JTextField txtAddress = UITheme.createStyledTextField(currentAddress, "Nhập địa chỉ...");
		JTextField txtPhone = UITheme.createStyledTextField(currentPhone, "Nhập số điện thoại...");

		Object[] fields = {
				lblId, lblName,
				"Tên đăng nhập:", txtUsername,
				"Địa chỉ:", txtAddress,
				"Số điện thoại:", txtPhone
		};

		int result = JOptionPane.showConfirmDialog(this, fields,
				"Cập nhật thông tin độc giả", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		String newUsername = txtUsername.getText().trim();
		String newAddress = txtAddress.getText().trim();
		String newPhone = txtPhone.getText().trim();

		if (newUsername.isEmpty()) {
			UITheme.showWarning(this, "Tên đăng nhập không được để trống!");
			return;
		}
		if (!newPhone.isEmpty() && !newPhone.matches("^[0-9+\\-\\s]{8,15}$")) {
			UITheme.showWarning(this, "Số điện thoại không hợp lệ (chỉ gồm số, 8-15 ký tự)!");
			return;
		}

		btnUpdateBorrower.setEnabled(false);
		listener.onStatusUpdate("Đang cập nhật thông tin độc giả...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (demoMode) {
					mockData.updateMockBorrower(borrowerId, newUsername, newAddress, newPhone);
				} else {
					Borrower updated = new Borrower(borrowerId, newUsername, "", currentName, newAddress, newPhone);
					librarian.updateBorrower(updated);
				}
				SwingUtilities.invokeLater(() -> {
					btnUpdateBorrower.setEnabled(true);
					listener.onStatusUpdate("Cập nhật thông tin thành công!", UITheme.SUCCESS_COLOR);
					JOptionPane.showMessageDialog(this,
							"Đã cập nhật thông tin độc giả #" + borrowerId + ".",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
					loadBorrowersAsync();
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnUpdateBorrower.setEnabled(true);
					listener.onStatusUpdate("Lỗi cập nhật: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể cập nhật thông tin:\n" + ex.getMessage());
				});
			}
		}).start();
	}
}
