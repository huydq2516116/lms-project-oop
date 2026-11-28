package lms.object;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lms.database.DatabaseOperations;

public class HoldRequest {
    private final int holdRequestId;
    private final Date requestDate;
    private final int borrowerId;
    private final int bookId;

    /** Parameters follow the database column order. */
    public HoldRequest(int holdRequestId, Date requestDate, int borrowerId, int bookId) {
        this.holdRequestId = holdRequestId;
        this.requestDate = requestDate;
        this.borrowerId = borrowerId;
        this.bookId = bookId;
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

    /** The team has not chosen a window length yet, so the caller supplies it. */
    public boolean isExpired(LocalDate today, int holdDays) {
        return today.isAfter(Loan.toLocalDate(requestDate).plusDays(holdDays));
    }

    public void cancel() throws SQLException {
        DatabaseOperations.deleteHoldRequest(holdRequestId);
    }

    /** Same flow as the hold screens: lend the book, then delete the hold. */
    public Loan fulfill(int issuerId) throws SQLException {
        Book book = DatabaseOperations.getBookById(bookId);
        if (book == null) {
            throw new IllegalStateException("Reserved book is missing from the catalog.");
        }
        Loan loan = book.checkOut(borrowerId, issuerId);
        cancel();
        return loan;
    }

    /** Returns the oldest hold, or null if there is none. */
    public static HoldRequest firstInQueue(List<HoldRequest> holds) {
        return holds.stream()
                .min(Comparator.comparing(HoldRequest::getRequestDate).thenComparingInt(HoldRequest::getHoldRequestId))
                .orElse(null);
    }

    public static HoldRequest nextInQueue(int bookId) throws SQLException {
        return firstInQueue(DatabaseOperations.getHoldRequestsByBookId(bookId));
    }
}
