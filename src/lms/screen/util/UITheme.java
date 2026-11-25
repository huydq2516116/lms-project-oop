package lms.screen.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * Tiện ích giao diện dùng chung (bảng màu, font chữ, factory component chuẩn FlatLaf).
 */
public final class UITheme {

	// Bảng màu đồng bộ với LoginScreen
	public static final Color PRIMARY_COLOR = new Color(0, 122, 204);
	public static final Color SIDEBAR_BG = new Color(248, 250, 252);
	public static final Color CONTENT_BG = new Color(243, 246, 250);
	public static final Color CARD_BG = Color.WHITE;
	public static final Color BORDER_COLOR = new Color(226, 232, 240);
	public static final Color TEXT_PRIMARY = new Color(30, 41, 59);
	public static final Color TEXT_MUTED = new Color(100, 116, 139);
	public static final Color SUCCESS_COLOR = new Color(34, 154, 72);
	public static final Color WARNING_COLOR = new Color(217, 119, 6);
	public static final Color DANGER_COLOR = new Color(220, 53, 69);

	// Font chữ chuẩn Segoe UI
	public static final String FONT_FAMILY = "Segoe UI";
	public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 13);
	public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
	public static final Font FONT_NAV = new Font(FONT_FAMILY, Font.PLAIN, 14);
	public static final Font FONT_NAV_ACTIVE = new Font(FONT_FAMILY, Font.BOLD, 14);
	public static final Font FONT_STATUS = new Font(FONT_FAMILY, Font.PLAIN, 12);
	public static final Font FONT_BADGE = new Font(FONT_FAMILY, Font.BOLD, 12);
	public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 20);
	public static final Font FONT_BRAND = new Font(FONT_FAMILY, Font.BOLD, 18);
	public static final Font FONT_STAT = new Font(FONT_FAMILY, Font.BOLD, 24);
	public static final Font FONT_SUB_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);

	private UITheme() {
	}

	/**
	 * Tạo thẻ panel nền trắng bo góc có viền nhẹ chuẩn FlatLaf
	 */
	public static JPanel createWhiteCardPanel(LayoutManager layout) {
		JPanel pnl = new JPanel(layout);
		pnl.setBackground(CARD_BG);
		pnl.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1,#E2E8F0");
		return pnl;
	}

	/**
	 * Tạo nút nhấn chính màu xanh chủ đạo
	 */
	public static JButton createPrimaryButton(String text) {
		JButton btn = new JButton(text);
		btn.setFont(FONT_BOLD);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
		btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #007ACC; foreground: #FFFFFF;");
		return btn;
	}

	/**
	 * Tạo nút nhấn phụ nền xám nhạt
	 */
	public static JButton createSecondaryButton(String text) {
		JButton btn = new JButton(text);
		btn.setFont(FONT_REGULAR);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.putClientProperty(FlatClientProperties.STYLE,
				"arc: 8; background: #F1F5F9; foreground: #1E293B; borderWidth: 0; hoverBackground: #E2E8F0");
		return btn;
	}

	/**
	 * Tạo nhãn trường nhập liệu trong form
	 */
	public static JLabel createFormLabel(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(FONT_REGULAR);
		lbl.setForeground(TEXT_PRIMARY);
		return lbl;
	}

	/**
	 * Tạo ô nhập liệu có placeholder và nút xóa nhanh
	 */
	public static JTextField createStyledTextField(String value, String placeholder) {
		JTextField txt = new JTextField(safeText(value, ""), 24);
		txt.setFont(FONT_REGULAR);
		txt.setPreferredSize(new Dimension(280, 36));
		txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
		txt.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		txt.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		return txt;
	}

	/**
	 * Tạo TableModel chỉ đọc (không cho sửa trực tiếp trên ô bảng)
	 */
	public static DefaultTableModel createReadOnlyTableModel(String[] columns) {
		return new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}

	/**
	 * Tạo JTable chuẩn giao diện hiện đại của dự án
	 */
	public static JTable createStyledTable(DefaultTableModel model) {
		JTable table = new JTable(model);
		table.setFont(FONT_REGULAR);
		table.setRowHeight(34);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowHorizontalLines(true);
		table.setGridColor(new Color(241, 245, 249));
		table.getTableHeader().setFont(FONT_BOLD);
		table.getTableHeader().setReorderingAllowed(false);
		return table;
	}

	public static String safeText(String value, String fallback) {
		return (value == null || value.isBlank()) ? fallback : value;
	}

	public static void showWarning(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
	}

	public static void showError(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Renderer tô màu trạng thái nổi bật trong bảng dữ liệu
	 */
	public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public StatusBadgeRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			JLabel lbl = (JLabel) super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setFont(FONT_BADGE);

			if (!isSelected && value != null) {
				String text = value.toString();
				if (text.equals("Có sẵn") || text.equals("Đã trả") || text.equals("Đã thanh toán")) {
					lbl.setForeground(SUCCESS_COLOR);
				} else if (text.equals("Đang được mượn") || text.equals("Đang mượn")) {
					lbl.setForeground(WARNING_COLOR);
				} else if (text.equals("Chưa thanh toán")) {
					lbl.setForeground(DANGER_COLOR);
				} else {
					lbl.setForeground(TEXT_PRIMARY);
				}
			}
			return lbl;
		}
	}
}
