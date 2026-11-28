package lms.screen.borrower;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import lms.database.DatabaseOperations;
import lms.person.Borrower;
import lms.screen.util.UITheme;

/**
 * Panel hiển thị và chỉnh sửa thông tin hồ sơ cá nhân của độc giả.
 */
public final class ProfilePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerViewListener listener;

	private JLabel lblProfileId;
	private JLabel lblProfileName;
	private JTextField txtProfileUsername;
	private JTextField txtProfileAddress;
	private JTextField txtProfilePhone;
	private JButton btnSaveProfile;

	public ProfilePanel(Borrower borrower, boolean demoMode, BorrowerViewListener listener) {
		this.borrower = borrower;
		this.demoMode = demoMode;
		this.listener = listener;

		setLayout(new BorderLayout());
		setOpaque(false);
		initUI();
	}

	private void initUI() {
		JPanel pnlFormCard = UITheme.createWhiteCardPanel(new GridBagLayout());
		pnlFormCard.setBorder(new EmptyBorder(24, 28, 24, 28));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;

		// Mã độc giả
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Mã độc giả:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		lblProfileId = new JLabel("#" + borrower.getId());
		lblProfileId.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblProfileId.setForeground(UITheme.PRIMARY_COLOR);
		pnlFormCard.add(lblProfileId, gbc);

		// Họ và tên
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Họ và tên:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		lblProfileName = new JLabel(UITheme.safeText(borrower.getName(), "-"));
		lblProfileName.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblProfileName.setForeground(UITheme.TEXT_PRIMARY);
		pnlFormCard.add(lblProfileName, gbc);

		// Tên đăng nhập
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Tên đăng nhập:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfileUsername = UITheme.createStyledTextField(borrower.getUserName(), "Nhập tên đăng nhập...");
		pnlFormCard.add(txtProfileUsername, gbc);

		// Địa chỉ
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Địa chỉ:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfileAddress = UITheme.createStyledTextField(borrower.getAddress(), "Nhập địa chỉ liên hệ...");
		pnlFormCard.add(txtProfileAddress, gbc);

		// Số điện thoại
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Số điện thoại:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfilePhone = UITheme.createStyledTextField(borrower.getPhoneNo(), "Nhập số điện thoại...");
		pnlFormCard.add(txtProfilePhone, gbc);

		// Nút Lưu thay đổi
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(16, 8, 4, 8);
		btnSaveProfile = UITheme.createPrimaryButton("Lưu thay đổi");
		btnSaveProfile.setPreferredSize(new Dimension(150, 38));
		btnSaveProfile.addActionListener(e -> saveProfileAction());
		pnlFormCard.add(btnSaveProfile, gbc);

		add(pnlFormCard, BorderLayout.NORTH);
	}

	/**
	 * Kiểm tra đầu vào hợp lệ và lưu thay đổi hồ sơ cá nhân
	 */
	private void saveProfileAction() {
		String newUsername = txtProfileUsername.getText().trim();
		String newAddress = txtProfileAddress.getText().trim();
		String newPhone = txtProfilePhone.getText().trim();

		if (newUsername.isEmpty() || newAddress.isEmpty() || newPhone.isEmpty()) {
			UITheme.showWarning(this, "Tên đăng nhập, địa chỉ và số điện thoại không được để trống!");
			return;
		}
		if (!newPhone.matches("^[0-9+\\-\\s]{8,15}$")) {
			UITheme.showWarning(this, "Số điện thoại không hợp lệ (chỉ gồm số, 8-15 ký tự)!");
			txtProfilePhone.requestFocusInWindow();
			return;
		}

		btnSaveProfile.setEnabled(false);
		listener.onStatusUpdate("Đang cập nhật thông tin cá nhân...", UITheme.PRIMARY_COLOR);

		new Thread(() -> {
			try {
				if (!demoMode) {
					DatabaseOperations.updatePerson(borrower.getId(), newUsername, newAddress, newPhone);
				}
				SwingUtilities.invokeLater(() -> {
					borrower.setUserName(newUsername);
					borrower.setAddress(newAddress);
					borrower.setPhoneNo(newPhone);

					btnSaveProfile.setEnabled(true);
					listener.onStatusUpdate("Cập nhật hồ sơ thành công!", UITheme.SUCCESS_COLOR);
					listener.onProfileUpdated();
					JOptionPane.showMessageDialog(this, "Đã lưu thông tin hồ sơ cá nhân!",
							"Thành công", JOptionPane.INFORMATION_MESSAGE);
				});
			} catch (SQLException ex) {
				SwingUtilities.invokeLater(() -> {
					btnSaveProfile.setEnabled(true);
					listener.onStatusUpdate("Lỗi cập nhật hồ sơ: " + ex.getMessage(), UITheme.DANGER_COLOR);
					UITheme.showError(this, "Không thể cập nhật hồ sơ:\n" + ex.getMessage());
				});
			}
		}).start();
	}
}
