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

import lms.person.Borrower;
import lms.screen.borrower.BorrowedPanel;
import lms.screen.borrower.BorrowerMockData;
import lms.screen.borrower.BorrowerViewListener;
import lms.screen.borrower.CatalogPanel;
import lms.screen.borrower.HoldsPanel;
import lms.screen.borrower.LoansPanel;
import lms.screen.borrower.ProfilePanel;
import lms.screen.util.UITheme;

/**
 * Màn hình chính dành cho Độc giả (Borrower) trong hệ thống LMS.
 * Đóng vai trò điều phối Sidebar, Header, StatusBar và CardLayout quản lý các panel chức năng.
 */
public final class BorrowerScreen extends JFrame implements BorrowerViewListener {
	private static final long serialVersionUID = 1L;

	// Tên các màn hình con trong CardLayout
	private static final String CARD_CATALOG = "CATALOG";
	private static final String CARD_BORROWED = "BORROWED";
	private static final String CARD_LOANS = "LOANS";
	private static final String CARD_HOLDS = "HOLDS";
	private static final String CARD_PROFILE = "PROFILE";

	private final transient Borrower borrower;
	private final boolean demoMode;
	private final transient BorrowerMockData mockData;

	// Thành phần điều hướng chính
	private CardLayout cardLayout;
	private JPanel pnlCards;
	private JLabel lblHeaderTitle;
	private JLabel lblHeaderSubtitle;
	private JLabel lblStatusBar;
	private JLabel lblStatusHint;
	private JLabel lblSidebarUserName;

	// Các nút điều hướng trên Sidebar
	private JButton btnNavCatalog;
	private JButton btnNavBorrowed;
	private JButton btnNavLoans;
	private JButton btnNavHolds;
	private JButton btnNavProfile;
	private JButton btnLogout;

	// 5 panel chức năng con
	private CatalogPanel pnlCatalog;
	private BorrowedPanel pnlBorrowed;
	private LoansPanel pnlLoans;
	private HoldsPanel pnlHolds;
	private ProfilePanel pnlProfile;

	/**
	 * Khởi tạo màn hình Borrower từ tài khoản đã đăng nhập.
	 */
	public BorrowerScreen(Borrower borrower) {
		this(borrower, false);
	}

	/**
	 * Khởi tạo màn hình Borrower (hỗ trợ chế độ demo offline nếu không kết nối được DB).
	 */
	public BorrowerScreen(Borrower borrower, boolean demoMode) {
		FlatLightLaf.setup();

		this.borrower = borrower != null
				? borrower
				: new Borrower(1, "borrower01", "123456", "Nguyễn Văn An", "Hà Nội", "0912345678");
		this.demoMode = demoMode;
		this.mockData = new BorrowerMockData(this.borrower.getId());

		setTitle("LMS - Cổng Thông Tin Độc Giả");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1120, 720);
		setMinimumSize(new Dimension(920, 600));
		setLocationRelativeTo(null);

		initUI();
		switchCard(CARD_CATALOG, btnNavCatalog, "Tra cứu & Đặt giữ sách",
				"Tìm kiếm sách trong thư viện và tạo yêu cầu đặt giữ trực tuyến");
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

		pnlCatalog = new CatalogPanel(borrower, demoMode, mockData, this);
		pnlBorrowed = new BorrowedPanel(borrower, demoMode, mockData, this);
		pnlLoans = new LoansPanel(borrower, demoMode, mockData, this);
		pnlHolds = new HoldsPanel(borrower, demoMode, mockData, this);
		pnlProfile = new ProfilePanel(borrower, demoMode, this);

		pnlCards.add(pnlCatalog, CARD_CATALOG);
		pnlCards.add(pnlBorrowed, CARD_BORROWED);
		pnlCards.add(pnlLoans, CARD_LOANS);
		pnlCards.add(pnlHolds, CARD_HOLDS);
		pnlCards.add(pnlProfile, CARD_PROFILE);

		pnlMain.add(pnlCards, BorderLayout.CENTER);
		pnlMain.add(createStatusBar(), BorderLayout.SOUTH);

		pnlRoot.add(pnlMain, BorderLayout.CENTER);
		setContentPane(pnlRoot);
	}

	/**
	 * Khu vực Sidebar: Thông tin người dùng, menu chức năng và nút Đăng xuất
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

		// Card hiển thị người dùng đang đăng nhập
		JPanel pnlUserCard = new JPanel(new BorderLayout(10, 0));
		pnlUserCard.setBackground(UITheme.CARD_BG);
		pnlUserCard.setBorder(new EmptyBorder(12, 12, 12, 12));
		pnlUserCard.putClientProperty(FlatClientProperties.STYLE,
				"arc: 12; border: 1,1,1,1,#E2E8F0");
		pnlUserCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
		pnlUserCard.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel lblAvatar = new JLabel(getInitials(borrower.getName()), SwingConstants.CENTER);
		lblAvatar.setPreferredSize(new Dimension(38, 38));
		lblAvatar.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 14));
		lblAvatar.setForeground(Color.WHITE);
		lblAvatar.setOpaque(true);
		lblAvatar.setBackground(UITheme.PRIMARY_COLOR);
		lblAvatar.putClientProperty(FlatClientProperties.STYLE, "arc: 999");

		JPanel pnlUserText = new JPanel(new GridLayout(2, 1, 0, 2));
		pnlUserText.setOpaque(false);
		lblSidebarUserName = new JLabel(UITheme.safeText(borrower.getName(), borrower.getUserName()));
		lblSidebarUserName.setFont(UITheme.FONT_BOLD);
		lblSidebarUserName.setForeground(UITheme.TEXT_PRIMARY);

		JLabel lblRole = new JLabel("Độc giả (ID: #" + borrower.getId() + ")");
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

		btnNavCatalog = createNavButton("Tra cứu sách");
		btnNavBorrowed = createNavButton("Sách đang mượn");
		btnNavLoans = createNavButton("Lịch sử mượn");
		btnNavHolds = createNavButton("Yêu cầu đặt giữ");
		btnNavProfile = createNavButton("Hồ sơ cá nhân");

		btnNavCatalog.addActionListener(e -> switchCard(CARD_CATALOG, btnNavCatalog,
				"Tra cứu & Đặt giữ sách", "Tìm kiếm sách trong thư viện và tạo yêu cầu đặt giữ trực tuyến"));
		btnNavBorrowed.addActionListener(e -> switchCard(CARD_BORROWED, btnNavBorrowed,
				"Sách đang mượn", "Danh sách các đầu sách hiện tại bạn đang mượn từ thư viện"));
		btnNavLoans.addActionListener(e -> switchCard(CARD_LOANS, btnNavLoans,
				"Lịch sử phiếu mượn", "Theo dõi ngày mượn, ngày trả và trạng thái tiền phạt"));
		btnNavHolds.addActionListener(e -> switchCard(CARD_HOLDS, btnNavHolds,
				"Yêu cầu đặt giữ sách", "Quản lý các yêu cầu giữ sách bạn đã gửi"));
		btnNavProfile.addActionListener(e -> switchCard(CARD_PROFILE, btnNavProfile,
				"Hồ sơ cá nhân", "Xem và cập nhật thông tin liên hệ của độc giả"));

		pnlMenu.add(btnNavCatalog);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavBorrowed);
		pnlMenu.add(Box.createVerticalStrut(6));
		pnlMenu.add(btnNavLoans);
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

		lblHeaderTitle = new JLabel("Tra cứu & Đặt giữ sách");
		lblHeaderTitle.setFont(UITheme.FONT_TITLE);
		lblHeaderTitle.setForeground(UITheme.TEXT_PRIMARY);

		lblHeaderSubtitle = new JLabel("Tìm kiếm sách trong thư viện");
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

		styleNavButton(btnNavCatalog, activeBtn == btnNavCatalog);
		styleNavButton(btnNavBorrowed, activeBtn == btnNavBorrowed);
		styleNavButton(btnNavLoans, activeBtn == btnNavLoans);
		styleNavButton(btnNavHolds, activeBtn == btnNavHolds);
		styleNavButton(btnNavProfile, activeBtn == btnNavProfile);
	}

	private void loadAllDataAsync() {
		pnlCatalog.loadBooksAsync("ALL", "");
		pnlBorrowed.loadBorrowedBooksAsync();
		pnlLoans.loadLoansAsync();
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
		return demoMode ? "Chế độ: Demo Offline" : "Phiên đăng nhập: " + borrower.getUserName();
	}

	private String getInitials(String name) {
		if (name == null || name.isBlank()) {
			return "B";
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
	public void onBorrowedCountChanged(int count) {
		pnlCatalog.updateBorrowedCount(count);
	}

	@Override
	public void onLoansCountChanged(int count) {
		pnlCatalog.updateLoansCount(count);
	}

	@Override
	public void onHoldsCountChanged(int count) {
		pnlCatalog.updateHoldsCount(count);
	}

	@Override
	public void onHoldPlaced() {
		pnlHolds.loadHoldRequestsAsync();
	}

	@Override
	public void onProfileUpdated() {
		lblSidebarUserName.setText(UITheme.safeText(borrower.getName(), borrower.getUserName()));
		lblStatusHint.setText(getSessionHintText());
	}

	/**
	 * Hàm main để chạy thử giao diện BorrowerScreen ở chế độ Demo Offline
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Borrower demoBorrower = new Borrower(
					1, "nguyenan", "123456", "Nguyễn Văn An", "1 Đại Cồ Việt, Hà Nội", "0912345678");
			new BorrowerScreen(demoBorrower, true);
		});
	}
}
