package lms.screen.librarian;

import java.awt.Color;

/**
 * Callback giao tiếp và đồng bộ dữ liệu giữa các panel con và màn hình chính LibrarianScreen.
 */
public interface LibrarianViewListener {

	/**
	 * Cập nhật thông báo trên thanh trạng thái dưới cùng
	 */
	void onStatusUpdate(String message, Color color);

	/**
	 * Cập nhật số liệu 3 thẻ thống kê kho sách trên ManageBooksPanel
	 */
	void onBookStatsChanged(int totalBooks, int availableBooks, int issuedBooks);

	/**
	 * Kích hoạt làm mới dữ liệu chéo giữa các tab khi phát sinh giao dịch mượn/trả hoặc CRUD sách/đặt giữ
	 */
	void onDataChanged();

	/**
	 * Cập nhật tên/username hiển thị ở Sidebar và StatusBar khi lưu hồ sơ thành công
	 */
	void onProfileUpdated();
}
