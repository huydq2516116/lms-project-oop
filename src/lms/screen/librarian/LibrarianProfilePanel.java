package lms.screen.librarian;

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
import lms.person.Librarian;
import lms.screen.util.UITheme;

/**
 * Panel hiển thị và chỉnh sửa hồ sơ cá nhân của Thủ thư đang đăng nhập.
 */
public final class LibrarianProfilePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final transient Librarian librarian;
	private final boolean demoMode;
	private final transient LibrarianViewListener listener;

	private JLabel lblProfileId;
	private JLabel lblProfileName;
	private JLabel lblProfileOfficeNo;
	private JTextField txtProfileUsername;
	private JTextField txtProfileAddress;
	private JTextField txtProfilePhone;
	private JButton btnSaveProfile;

	public LibrarianProfilePanel(Librarian librarian, boolean demoMode, LibrarianViewListener listener) {
		this.librarian = librarian;
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

		// Mã thủ thư
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Mã thủ thư:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		lblProfileId = new JLabel("#" + librarian.getId());
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
		lblProfileName = new JLabel(UITheme.safeText(librarian.getName(), "-"));
		lblProfileName.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblProfileName.setForeground(UITheme.TEXT_PRIMARY);
		pnlFormCard.add(lblProfileName, gbc);

		// Số phòng
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Số phòng (Office No):"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		lblProfileOfficeNo = new JLabel(String.valueOf(librarian.getOfficeNo()));
		lblProfileOfficeNo.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblProfileOfficeNo.setForeground(UITheme.TEXT_PRIMARY);
		pnlFormCard.add(lblProfileOfficeNo, gbc);

		// Tên đăng nhập
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Tên đăng nhập:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfileUsername = UITheme.createStyledTextField(librarian.getUserName(), "Nhập tên đăng nhập...");
		pnlFormCard.add(txtProfileUsername, gbc);

		// Địa chỉ
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Địa chỉ:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfileAddress = UITheme.createStyledTextField(librarian.getAddress(), "Nhập địa chỉ liên hệ...");
		pnlFormCard.add(txtProfileAddress, gbc);

		// Số điện thoại
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 0.0;
		pnlFormCard.add(UITheme.createFormLabel("Số điện thoại:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtProfilePhone = UITheme.createStyledTextField(librarian.getPhoneNo(), "Nhập số điện thoại...");
		pnlFormCard.add(txtProfilePhone, gbc);

		// Nút lưu
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(16, 8, 4, 8);
		btnSaveProfile = UITheme.createPrimaryButton("Lưu thay đổi");
		btnSaveProfile.setPreferredSize(new Dimension(150, 38));
		btnSaveProfile.addActionListener(e -> saveProfileAction());
		pnlFormCard.add(btnSaveProfile, gbc);

		add(pnlFormCard, BorderLayout.NORTH);
	}

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
					DatabaseOperations.updatePerson(librarian.getId(), newUsername, newAddress, newPhone);
				}
				SwingUtilities.invokeLater(() -> {
					librarian.setUserName(newUsername);
					librarian.setAddress(newAddress);
					librarian.setPhoneNo(newPhone);

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
