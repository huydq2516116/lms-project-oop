package lms.object;

public class HoldRequest {
    private final int holdRequestId;
    private final int requestDate;
    private final int borrowerId;
    private final int bookId;

    public HoldRequest(int bookId, int borrowerId, int holdRequestId, int requestDate) {
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.holdRequestId = holdRequestId;
        this.requestDate = requestDate;
    }

    public int getHoldRequestId() {
        return holdRequestId;
    }

    public int getRequestDate() {
        return requestDate;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public int getBookId() {
        return bookId;
    }
}
