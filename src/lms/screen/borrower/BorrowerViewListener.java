package lms.screen.borrower;

import java.awt.Color;

/**
 * Callback giao tiếp và đồng bộ dữ liệu giữa các panel con và màn hình chính BorrowerScreen.
 */
public interface BorrowerViewListener {

	/**
	 * Cập nhật thông báo trên thanh trạng thái dưới cùng
	 */
	void onStatusUpdate(String message, Color color);

	/**
	 * Cập nhật số lượng sách đang mượn lên thẻ thống kê
	 */
	void onBorrowedCountChanged(int count);

	/**
	 * Cập nhật tổng số phiếu mượn lên thẻ thống kê
	 */
	void onLoansCountChanged(int count);

	/**
	 * Cập nhật số lượng yêu cầu đặt giữ lên thẻ thống kê
	 */
	void onHoldsCountChanged(int count);

	/**
	 * Kích hoạt tải lại danh sách đặt giữ khi người dùng đặt giữ thành công ở CatalogPanel
	 */
	void onHoldPlaced();

	/**
	 * Cập nhật tên/username hiển thị ở Sidebar và StatusBar khi lưu hồ sơ thành công
	 */
	void onProfileUpdated();
}
