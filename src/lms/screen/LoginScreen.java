package lms.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import lms.database.DatabaseOperations;
import lms.database.DatabaseOperations.PasswordRole;

/**
 * Màn hình đăng nhập hệ thống LMS (Library Management System).
 * Tuân thủ quy tắc phát triển Java Swing + FlatLaf của dự án.
 */
public class LoginScreen extends JFrame {
	private static final long serialVersionUID = 3908597196893209836L;

	// Component định danh theo tiền tố chuẩn (txt, lbl, btn, pnl)
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JLabel lblResult;
	private JButton btnLogin;

	public LoginScreen() {
		// 1. Khởi tạo theme FlatLaf Light trước khi vẽ giao diện
		FlatLightLaf.setup();

		// 2. Cấu hình JFrame cơ bản
		setTitle("LMS - Đăng Nhập Hệ Thống");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(420, 360);
		setMinimumSize(new Dimension(380, 320));
		setLocationRelativeTo(null); // Canh giữa màn hình máy tính

		// 3. Khởi tạo các vùng giao diện
		initUI();

		// 4. Xử lý sự kiện nhấn Enter: chuyển focus hoặc kích hoạt đăng nhập
		KeyAdapter enterKeyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (e.getSource() == txtUsername) {
						if (txtPassword.getPassword().length == 0) {
							txtPassword.requestFocusInWindow();
						} else {
							loginButtonPressed();
						}
					} else {
						loginButtonPressed();
					}
				}
			}
		};
		txtUsername.addKeyListener(enterKeyListener);
		txtPassword.addKeyListener(enterKeyListener);

		setVisible(true);
	}

	/**
	 * Khởi tạo cấu trúc các vùng giao diện chính (Header, Body form, Nút thao tác)
	 */
	private void initUI() {
		JPanel pnlRoot = new JPanel(new BorderLayout());
		pnlRoot.setBorder(new EmptyBorder(25, 30, 25, 30));

		// Khu vực Header: Tiêu đề & phụ đề
		pnlRoot.add(createHeaderPanel(), BorderLayout.NORTH);

		// Khu vực Form: Nhập Username, Password và hiển thị thông báo lỗi
		pnlRoot.add(createFormPanel(), BorderLayout.CENTER);

		// Khu vực Nút chức năng (Login button)
		pnlRoot.add(createActionPanel(), BorderLayout.SOUTH);

		setContentPane(pnlRoot);
	}

	/**
	 * Khu vực Header
	 */
	private JPanel createHeaderPanel() {
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

		JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

		JLabel lblSubtitle = new JLabel("Hệ Thống Quản Lý Thư Viện", SwingConstants.CENTER);
		lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblSubtitle.setForeground(new Color(110, 110, 110));

		pnlHeader.add(lblTitle, BorderLayout.NORTH);
		pnlHeader.add(lblSubtitle, BorderLayout.SOUTH);
		return pnlHeader;
	}

	/**
	 * Khu vực Form nhập liệu (dùng GridBagLayout để căn chỉnh chuẩn & co giãn tốt)
	 */
	private JPanel createFormPanel() {
		JPanel pnlForm = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 4, 6, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// --- Hàng 1: Username Label ---
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		JLabel lblUsername = new JLabel("Tên đăng nhập:");
		lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		pnlForm.add(lblUsername, gbc);

		// --- Hàng 1: Username Input ---
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		txtUsername = new JTextField(20);
		txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập username...");
		txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		txtUsername.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		pnlForm.add(txtUsername, gbc);

		// --- Hàng 2: Password Label ---
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		JLabel lblPassword = new JLabel("Mật khẩu:");
		lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		pnlForm.add(lblPassword, gbc);

		// --- Hàng 2: Password Input ---
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		txtPassword = new JPasswordField(20);
		txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu...");
		txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc: 8; showRevealButton: true");
		pnlForm.add(txtPassword, gbc);

		// --- Hàng 3: Label thông báo trạng thái/lỗi ---
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		lblResult = new JLabel(" ", SwingConstants.CENTER);
		lblResult.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		pnlForm.add(lblResult, gbc);

		return pnlForm;
	}

	/**
	 * Khu vực Button đăng nhập
	 */
	private JPanel createActionPanel() {
		JPanel pnlAction = new JPanel(new BorderLayout());
		pnlAction.setBorder(new EmptyBorder(5, 0, 0, 0));

		btnLogin = new JButton("Đăng nhập");
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btnLogin.setPreferredSize(new Dimension(0, 38));
		btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Bo góc và làm nổi bật nút đăng nhập theo phong cách FlatLaf
		btnLogin.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
		btnLogin.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #007ACC; foreground: #FFFFFF;");

		btnLogin.addActionListener((ActionEvent e) -> loginButtonPressed());

		pnlAction.add(btnLogin, BorderLayout.CENTER);
		return pnlAction;
	}

	/**
	 * Xử lý sự kiện đăng nhập và kiểm tra hợp lệ
	 */
	private void loginButtonPressed() {
		String username = txtUsername.getText().trim();
		String password = new String(txtPassword.getPassword());

		// 1. Kiểm tra đầu vào rỗng (Validate input)
		if (username.isEmpty() || password.isEmpty()) {
			showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
			return;
		}

		// 2. Chuyển trạng thái giao diện khi đang xử lý
		setLoadingState(true);

		// 3. Thực hiện truy vấn kiểm tra thông tin đăng nhập trong luồng riêng để tránh đơ giao diện
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PasswordRole realPasswordRole = DatabaseOperations.getPasswordByUserName(username);

					SwingUtilities.invokeLater(() -> {
						setLoadingState(false);

						// Kiểm tra tài khoản không tồn tại (tránh NullPointerException)
						if (realPasswordRole == null || !password.equals(realPasswordRole.password())) {
							showError("Tên đăng nhập hoặc mật khẩu không chính xác!");
							return;
						}

						// Đăng nhập thành công
						lblResult.setForeground(new Color(40, 167, 69));
						lblResult.setText("Đăng nhập thành công! Vai trò: " + realPasswordRole.role());

						// Điều hướng theo vai trò (sẽ mở các Dashboard tương ứng)
						switch (realPasswordRole.role().toLowerCase()) {
							case "borrower" -> {
								// Mở BorrowerDashboard
							}
							case "clerk" -> {
								// Mở ClerkDashboard
							}
							case "librarian" -> {
								// Mở LibrarianDashboard
							}
							case "admin" -> {
								// Mở AdminDashboard
							}
							default -> {
								showError("Vai trò không hợp lệ: " + realPasswordRole.role());
							}
						}
					});

				} catch (SQLException ex) {
					SwingUtilities.invokeLater(() -> {
						setLoadingState(false);
						showError("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
					});
					System.getLogger(LoginScreen.class.getName()).log(System.Logger.Level.ERROR, "Lỗi đăng nhập", ex);
				}
			}
		}).start();
	}

	/**
	 * Hiển thị thông báo lỗi màu đỏ
	 */
	private void showError(String message) {
		lblResult.setForeground(new Color(220, 53, 69));
		lblResult.setText(message);
	}

	/**
	 * Khóa/mở nút và thông báo trạng thái đang tải
	 */
	private void setLoadingState(boolean isLoading) {
		btnLogin.setEnabled(!isLoading);
		txtUsername.setEnabled(!isLoading);
		txtPassword.setEnabled(!isLoading);

		if (isLoading) {
			lblResult.setForeground(new Color(0, 122, 204));
			lblResult.setText("Đang xác thực thông tin...");
		}
	}
}
