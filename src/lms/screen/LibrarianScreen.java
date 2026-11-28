package lms.screen;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import lms.person.Librarian;
import lms.screen.librarian.LibrarianBorrowersPanel;
import lms.screen.librarian.LibrarianCirculationPanel;
import lms.screen.librarian.LibrarianHoldsPanel;
import lms.screen.librarian.LibrarianMockData;
import lms.screen.librarian.LibrarianProfilePanel;
import lms.screen.librarian.LibrarianViewListener;
import lms.screen.librarian.ManageBooksPanel;
import lms.screen.util.UITheme;

/**
 * Màn hình chính dành cho Thủ thư (Librarian) trong hệ thống LMS.
 * Đóng vai trò điều phối Sidebar, Header, StatusBar và CardLayout quản lý 5 panel chức năng.
 */
public final class LibrarianScreen extends JFrame implements LibrarianViewListener {
	private static final long serialVersionUID = 1L;

	// Tên các màn hình con trong CardLayout
	private static final String CARD_BOOKS = "BOOKS";
	private static final String CARD_CIRCULATION = "CIRCULATION";
	private static final String CARD_BORROWERS = "BORROWERS";
	private static final String CARD_HOLDS = "HOLDS";
	private static final String CARD_PROFILE = "PROFILE";

	private final transient Librarian librarian;
	private final boolean demoMode;
	private final transient LibrarianMockData mockData;

	// Thành phần điều hướng chính
	private CardLayout cardLayout;
	private JPanel pnlCards;
	private JLabel lblHeaderTitle;
	private JLabel lblHeaderSubtitle;
	private JLabel lblStatusBar;
	private JLabel lblStatusHint;
	private JLabel lblSidebarUserName;

	// Các nút điều hướng trên Sidebar
	private JButton btnNavBooks;
	private JButton btnNavCirculation;
	private JButton btnNavBorrowers;
	private JButton btnNavHolds;
	private JButton btnNavProfile;
	private JButton btnLogout;

	// 5 panel chức năng con
	private ManageBooksPanel pnlBooks;
	private LibrarianCirculationPanel pnlCirculation;
	private LibrarianBorrowersPanel pnlBorrowers;
	private LibrarianHoldsPanel pnlHolds;
	private LibrarianProfilePanel pnlProfile;

	/**
	 * Khởi tạo màn hình Librarian từ tài khoản đã đăng nhập.
	 */
	public LibrarianScreen(Librarian librarian) {
		this(librarian, false);
	}

	/**
	 * Khởi tạo màn hình Librarian (hỗ trợ chế độ demo offline nếu không kết nối được DB).
	 */
	public LibrarianScreen(Librarian librarian, boolean demoMode) {
		FlatLightLaf.setup();

		this.librarian = librarian != null
				? librarian
				: new Librarian(3, 101, "librarian01", "123456", "Phạm Thu Hà", "Hà Nội", "0901234567", 12000000);
		this.demoMode = demoMode;
		this.mockData = new LibrarianMockData(this.librarian.getId());

		setTitle("LMS - Bàn Làm Việc Thủ Thư");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1120, 720);
		setMinimumSize(new Dimension(920, 600));
		setLocationRelativeTo(null);

		initUI();
		switchCard(CARD_BOOKS, btnNavBooks, "Quản lý Kho sách",
				"Tìm kiếm, thêm mới, cập nhật và quản lý trạng thái đầu sách");
		loadAllDataAsync();

		setVisible(true);
	}

	/**
	 * Khởi tạo bố cục tổng thể gồm Sidebar bên trái và Khu vực nội dung bên phải
	 */
	private void initUI() {
		JPanel pnlRoot = new JPanel(new BorderLayout());
		pnlRoot.setBackground(UITheme.CONTENT_BG);

		// 1. Sidebar điều hướng bên trái
		pnlRoot.add(createSidebarPanel(), BorderLayout.WEST);

		// 2. Phần bên phải: Header + Nội dung chính (CardLayout) + Status bar
		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBackground(UITheme.CONTENT_BG);
		pnlMain.add(createHeaderPanel(), BorderLayout.NORTH);

		cardLayout = new CardLayout();
		pnlCards = new JPanel(cardLayout);
		pnlCards.setBackground(UITheme.CONTENT_BG);
		pnlCards.setBorder(new EmptyBorder(16, 22, 16, 22));

		pnlBooks = new ManageBooksPanel(librarian, demoMode, mockData, this);
		pnlCirculation = new LibrarianCirculationPanel(librarian, demoMode, mockData, this);
		pnlBorrowers = new LibrarianBorrowersPanel(librarian, demoMode, mockData, this);
		pnlHolds = new LibrarianHoldsPanel(librarian, demoMode, mockData, this);
		pnlProfile = new LibrarianProfilePanel(librarian, demoMode, this);

		pnlCards.add(pnlBooks, CARD_BOOKS);
		pnlCards.add(pnlCirculation, CARD_CIRCULATION);
		pnlCards.add(pnlBorrowers, CARD_BORROWERS);
		pnlCards.add(pnlHolds, CARD_HOLDS);
		pnlCards.add(pnlProfile, CARD_PROFILE);

		pnlMain.add(pnlCards, BorderLayout.CENTER);
		pnlMain.add(createStatusBar(), BorderLayout.SOUTH);

		pnlRoot.add(pnlMain, BorderLayout.CENTER);
		setContentPane(pnlRoot);
	}

	/**
	 * Khu vực Sidebar: Thông tin thủ thư, menu chức năng và nút Đăng xuất
	 */
	private JPanel createSidebarPanel() {
		JPanel pnlSidebar = new JPanel(new BorderLayout());
		pnlSidebar.setPreferredSize(new Dimension(245, 0));
		pnlSidebar.setBackground(UITheme.SIDEBAR_BG);
		pnlSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

		// --- Phần trên: Brand + Thẻ User ---
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
		pnlTop.setOpaque(false);
		pnlTop.setBorder(new EmptyBorder(20, 16, 16, 16));

		JLabel lblBrand = new JLabel("LMS LIBRARY");
		lblBrand.setFont(UITheme.FONT_BRAND);
		lblBrand.setForeground(UITheme.PRIMARY_COLOR);
		lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblBrandSub = new JLabel("Hệ Thống Quản Lý Thư Viện");
		lblBrandSub.setFont(UITheme.FONT_SUB_SMALL);
		lblBrandSub.setForeground(UITheme.TEXT_MUTED);
		lblBrandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

		pnlTop.add(lblBrand);
		pnlTop.add(Box.createVerticalStrut(2));
		pnlTop.add(lblBrandSub);
		pnlTop.add(Box.createVerticalStrut(18));

		// Card hiển thị thủ thư đang đăng nhập
		JPanel pnlUserCard = new JPanel(new BorderLayout(10, 0));
		pnlUserCard.setBackground(UITheme.CARD_BG);
		pnlUserCard.setBorder(new EmptyBorder(12, 12, 12, 12));
		pnlUserCard.putClientProperty(FlatClientProperties.STYLE,
				"arc: 12; border: 1,1,1,1,#E2E8F0");
		pnlUserCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
		pnlUserCard.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblAvatar = new JLabel(getInitials(librarian.getName()), SwingConstants.CENTER);
		lblAvatar.setPreferredSize(new Dimension(38, 38));
		lblAvatar.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblAvatar.setForeground(Color.WHITE);
		lblAvatar.setOpaque(true);
		lblAvatar.setBackground(UITheme.PRIMARY_COLOR);
		lblAvatar.putClientProperty(FlatClientProperties.STYLE, "arc: 999");

		JPanel pnlUserText = new JPanel(new GridLayout(2, 1, 0, 2));
		pnlUserText.setOpaque(false);
		lblSidebarUserName = new JLabel(UITheme.safeText(librarian.getName(), librarian.getUserName()));
		lblSidebarUserName.setFont(UITheme.FONT_BOLD);
		lblSidebarUserName.setForeground(UITheme.TEXT_PRIMARY);

		JLabel lblRole = new JLabel("Thủ thư (ID: #" + librarian.getId() + ")");
		lblRole.setFont(UITheme.FONT_SUB_SMALL);
		lblRole.setForeground(UITheme.TEXT_MUTED);

		pnlUserText.add(lblSidebarUserName);
		pnlUserText.add(lblRole);

		pnlUserCard.add(lblAvatar, BorderLayout.WEST);
		pnlUserCard.add(pnlUserText, BorderLayout.CENTER);
		pnlTop.add(pnlUserCard);

		pnlSidebar.add(pnlTop, BorderLayout.NORTH);

		// --- Phần giữa: Danh sách nút Menu ---
		JPanel pnlMenu = new JPanel();
		pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
		pnlMenu.setOpaque(false);
		pnlMenu.setBorder(new EmptyBorder(4, 12, 12, 12));

		btnNavBooks = createNavButton("Quản lý Kho sách");
		btnNavCirculation = createNavButton("Mượn & Trả sách");
		btnNavBorrowers = createNavButton("Quản lý Độc giả");
		btnNavHolds = createNavButton("Yêu cầu đặt giữ");
		btnNavProfile = createNavButton("Hồ sơ cá nhân");

		btnNavBooks.addActionListener(e -> switchCard(CARD_BOOKS, btnNavBooks,
				"Quản lý Kho sách", "Tìm kiếm, thêm mới, cập nhật và quản lý trạng thái đầu sách"));
		btnNavCirculation.addActionListener(e -> switchCard(CARD_CIRCULATION, btnNavCirculation,
				"Quản lý Mượn & Trả sách", "Lập phiếu mượn, nhận trả sách và ghi nhận thanh toán tiền phạt"));
		btnNavBorrowers.addActionListener(e -> switchCard(CARD_BORROWERS, btnNavBorrowers,
				"Quản lý Độc giả", "Tìm kiếm, thêm mới và cập nhật thông tin độc giả"));
		btnNavHolds.addActionListener(e -> switchCard(CARD_HOLDS, btnNavHolds,
				"Yêu cầu đặt giữ sách", "Xử lý yêu cầu giữ sách hoặc hủy đặt giữ cho độc giả"));
		btnNavProfile.addActionListener(e -> switchCard(CARD_PROFILE, btnNavProfile,
				"Hồ sơ cá nhân", "Xem và cập nhật thông tin liên hệ của thủ thư"));

		pnlMenu.add(btnNavBooks);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavCirculation);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavBorrowers);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavHolds);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavProfile);

		pnlSidebar.add(pnlMenu, BorderLayout.CENTER);

		// --- Phần dưới: Nút Đăng xuất ---
		JPanel pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.setOpaque(false);
		pnlBottom.setBorder(new EmptyBorder(12, 12, 18, 12));

		btnLogout = new JButton("Đăng xuất");
		btnLogout.setFont(UITheme.FONT_BOLD);
		btnLogout.setPreferredSize(new Dimension(0, 38));
		btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnLogout.putClientProperty(FlatClientProperties.STYLE,
				"arc: 10; background: #FEE2E2; foreground: #B91C1C; borderWidth: 0; hoverBackground: #FECACA");
		btnLogout.addActionListener(e -> logoutAction());

		pnlBottom.add(btnLogout, BorderLayout.CENTER);
		pnlSidebar.add(pnlBottom, BorderLayout.SOUTH);

		return pnlSidebar;
	}

	private JButton createNavButton(String text) {
		JButton btn = new JButton(text);
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.setFont(UITheme.FONT_NAV);
		btn.setPreferredSize(new Dimension(220, 40));
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setAlignmentX(Component.LEFT_ALIGNMENT);
		styleNavButton(btn, false);
		return btn;
	}

	private void styleNavButton(JButton btn, boolean active) {
		if (active) {
			btn.setFont(UITheme.FONT_NAV_ACTIVE);
			btn.putClientProperty(FlatClientProperties.STYLE,
					"arc: 10; background: #007ACC; foreground: #FFFFFF; borderWidth: 0; margin: 8,14,8,14");
		} else {
			btn.setFont(UITheme.FONT_NAV);
			btn.putClientProperty(FlatClientProperties.STYLE,
					"arc: 10; background: #F8FAFC; foreground: #334155; borderWidth: 0; hoverBackground: #E2E8F0; margin: 8,14,8,14");
		}
	}

	/**
	 * Khu vực Header của trang nội dung
	 */
	private JPanel createHeaderPanel() {
		JPanel pnlHeader = new JPanel(new BorderLayout());
		pnlHeader.setBackground(UITheme.CARD_BG);
		pnlHeader.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
				new EmptyBorder(16, 22, 16, 22)));

		JPanel pnlTitleGroup = new JPanel(new GridLayout(2, 1, 0, 3));
		pnlTitleGroup.setOpaque(false);

		lblHeaderTitle = new JLabel("Quản lý Kho sách");
		lblHeaderTitle.setFont(UITheme.FONT_TITLE);
		lblHeaderTitle.setForeground(UITheme.TEXT_PRIMARY);

		lblHeaderSubtitle = new JLabel("Tìm kiếm, thêm mới, cập nhật và quản lý trạng thái đầu sách");
		lblHeaderSubtitle.setFont(UITheme.FONT_REGULAR);
		lblHeaderSubtitle.setForeground(UITheme.TEXT_MUTED);

		pnlTitleGroup.add(lblHeaderTitle);
		pnlTitleGroup.add(lblHeaderSubtitle);

		JButton btnSyncAll = UITheme.createSecondaryButton("Đồng bộ dữ liệu");
		btnSyncAll.addActionListener(e -> loadAllDataAsync());

		pnlHeader.add(pnlTitleGroup, BorderLayout.WEST);
		pnlHeader.add(btnSyncAll, BorderLayout.EAST);
		return pnlHeader;
	}

	/**
	 * Thanh trạng thái dưới cùng của cửa sổ
	 */
	private JPanel createStatusBar() {
		JPanel pnlStatus = new JPanel(new BorderLayout());
		pnlStatus.setBackground(UITheme.CARD_BG);
		pnlStatus.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
				new EmptyBorder(6, 22, 6, 22)));

		lblStatusBar = new JLabel("Sẵn sàng");
		lblStatusBar.setFont(UITheme.FONT_STATUS);
		lblStatusBar.setForeground(UITheme.TEXT_MUTED);

		lblStatusHint = new JLabel(getSessionHintText());
		lblStatusHint.setFont(UITheme.FONT_STATUS);
		lblStatusHint.setForeground(UITheme.TEXT_MUTED);

		pnlStatus.add(lblStatusBar, BorderLayout.WEST);
		pnlStatus.add(lblStatusHint, BorderLayout.EAST);
		return pnlStatus;
	}

	private void switchCard(String cardName, JButton activeBtn, String title, String subtitle) {
		cardLayout.show(pnlCards, cardName);
		lblHeaderTitle.setText(title);
		lblHeaderSubtitle.setText(subtitle);

		styleNavButton(btnNavBooks, activeBtn == btnNavBooks);
		styleNavButton(btnNavCirculation, activeBtn == btnNavCirculation);
		styleNavButton(btnNavBorrowers, activeBtn == btnNavBorrowers);
		styleNavButton(btnNavHolds, activeBtn == btnNavHolds);
		styleNavButton(btnNavProfile, activeBtn == btnNavProfile);
	}

	private void loadAllDataAsync() {
		pnlBooks.loadBooksAsync("ALL", "");
		pnlCirculation.loadLoansAsync();
		pnlBorrowers.loadBorrowersAsync();
		pnlHolds.loadHoldRequestsAsync();
	}

	private void logoutAction() {
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
				"Xác nhận đăng xuất",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (confirm == JOptionPane.YES_OPTION) {
			dispose();
			new LoginScreen();
		}
	}

	private String getSessionHintText() {
		return demoMode
				? "Chế độ: Demo Offline"
				: "Phiên đăng nhập: " + librarian.getUserName() + " (Phòng #" + librarian.getOfficeNo() + ")";
	}

	private String getInitials(String name) {
		if (name == null || name.isBlank()) {
			return "L";
		}
		String[] parts = name.trim().split("\\s+");
		return parts[parts.length - 1].substring(0, 1).toUpperCase();
	}

	// =========================================================================
	// TRIỂN KHAI CALLBACK ĐỒNG BỘ GIỮA CÁC PANEL CON
	// =========================================================================

	@Override
	public void onStatusUpdate(String message, Color color) {
		lblStatusBar.setForeground(color);
		lblStatusBar.setText(message);
	}

	@Override
	public void onBookStatsChanged(int totalBooks, int availableBooks, int issuedBooks) {
		pnlBooks.updateBookStats(totalBooks, availableBooks, issuedBooks);
	}

	@Override
	public void onDataChanged() {
		pnlBooks.loadBooksAsync("ALL", "");
		pnlCirculation.loadLoansAsync();
		pnlHolds.loadHoldRequestsAsync();
	}

	@Override
	public void onProfileUpdated() {
		lblSidebarUserName.setText(UITheme.safeText(librarian.getName(), librarian.getUserName()));
		lblStatusHint.setText(getSessionHintText());
	}

	/**
	 * Hàm main để chạy thử giao diện LibrarianScreen ở chế độ Demo Offline
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Librarian demoLibrarian = new Librarian(
					3, 101, "thuhapham", "123456", "Phạm Thu Hà", "25 Lý Thường Kiệt, Hà Nội", "0901234567", 12000000);
			new LibrarianScreen(demoLibrarian, true);
		});
	}
}
