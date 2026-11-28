package lms.screen.clerk;

import java.awt.Color;

/**
 * Callback giao tiếp và đồng bộ dữ liệu giữa các panel con và màn hình chính ClerkScreen.
 */
public interface ClerkViewListener {

	/**
	 * Cập nhật thông báo trên thanh trạng thái dưới cùng
	 */
	void onStatusUpdate(String message, Color color);

	/**
	 * Cập nhật số phiếu mượn đang hoạt động lên thẻ thống kê
	 */
	void onActiveLoansCountChanged(int count);

	/**
	 * Cập nhật số phiếu quá hạn / chưa thanh toán phạt lên thẻ thống kê
	 */
	void onOverdueCountChanged(int count);

	/**
	 * Cập nhật số yêu cầu đặt giữ chờ xử lý lên thẻ thống kê
	 */
	void onPendingHoldsCountChanged(int count);

	/**
	 * Kích hoạt tải lại dữ liệu liên quan khi phát sinh giao dịch mượn/trả/thanh toán phạt
	 */
	void onTransactionCompleted();

	/**
	 * Cập nhật tên/username hiển thị ở Sidebar và StatusBar khi lưu hồ sơ thành công
	 */
	void onProfileUpdated();
}
