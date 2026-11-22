package lms.object;

import java.util.Date;

public class HoldRequest {
    private final int holdRequestId;
    private final Date requestDate;
    private final int borrowerId;
    private final int bookId;

    public HoldRequest(int bookId, Date requestDate, int borrowerId, int holdRequestId) {
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.holdRequestId = holdRequestId;
        this.requestDate = requestDate;
    }

    public int getHoldRequestId() {
        return holdRequestId;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public int getBookId() {
        return bookId;
    }
}
