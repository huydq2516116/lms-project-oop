package lms.object;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;

import lms.database.DatabaseOperations;

public class Loan {
    public static final int LOAN_DAYS = 30; // same period DatabaseOperations.createLoan stores

    private final int loanId;
    private final Date issuedDate;
    private Date dateReturned;
    private boolean finePaid;
    private final int bookId;
    private final int borrowerId;
    private final int issuerId;
    private int receiverId;
    private Date dueDate;

    public Loan(int loanId, Date issuedDate, Date dateReturned, boolean finePaid, int bookId, int borrowerId,
            int issuerId, int receiverId, Date dueDate) {
        this.loanId = loanId;
        this.issuedDate = issuedDate;
        this.dateReturned = dateReturned;
        this.finePaid = finePaid;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.issuerId = issuerId;
        this.receiverId = receiverId;
        this.dueDate = dueDate;
    }

    public int getLoanId() {
        return loanId;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public Date getDateReturned() {
        return dateReturned;
    }

    public boolean isFinePaid() {
        return finePaid;
    }

    public int getBookId() {
        return bookId;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public int getIssuerId() {
        return issuerId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setDateReturned(Date dateReturned) {
        this.dateReturned = dateReturned;
    }

    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    /** Rows created before the due_date column existed fall back to the issue date plus the loan period. */
    public Date getDueDate() {
        return dueDate != null ? dueDate : java.sql.Date.valueOf(toLocalDate(issuedDate).plusDays(LOAN_DAYS));
    }

    public boolean isReturned() {
        return dateReturned != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(toLocalDate(getDueDate()));
    }

    /** Days past the due date, counted up to the return date, or up to today for an open loan. */
    public long getDaysLate(LocalDate today) {
        LocalDate end = isReturned() ? toLocalDate(dateReturned) : today;
        return Math.max(0, ChronoUnit.DAYS.between(toLocalDate(getDueDate()), end));
    }

    /** The team has not chosen a daily rate yet, so the caller supplies it. */
    public long calculateFine(LocalDate today, long finePerDay) {
        return finePaid ? 0 : getDaysLate(today) * finePerDay;
    }

    /** Closes the loan and frees the book. */
    public void returnBook(int receiverId) throws SQLException {
        refresh();
        if (isReturned()) {
            throw new IllegalStateException("Loan has already been returned.");
        }
        Book book = DatabaseOperations.getBookById(bookId);
        if (book == null) {
            throw new IllegalStateException("Loan book is missing from the catalog.");
        }
        LocalDate today = LocalDate.now();
        java.sql.Date returnDate = java.sql.Date.valueOf(today);
        // Book first: if the second write fails the loan stays open and the return can simply be retried.
        book.markReturned();
        DatabaseOperations.updateLoanReturnDate(loanId, returnDate);
        dateReturned = returnDate;
        // shortcut: receiver_id is kept in memory only; DatabaseOperations has no method that saves it.
        this.receiverId = receiverId;
    }

    public void payFine() throws SQLException {
        refresh();
        if (finePaid) {
            throw new IllegalStateException("The fine on this loan is already paid.");
        }
        DatabaseOperations.updateFineStatus(loanId);
        finePaid = true;
    }

    public void renew(Date newDueDate) throws SQLException {
        if (newDueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null.");
        }
        refresh();
        if (isReturned()) {
            throw new IllegalStateException("Cannot renew a returned loan.");
        }
        java.sql.Date renewed = java.sql.Date.valueOf(toLocalDate(newDueDate));
        DatabaseOperations.updateLoanDueDate(loanId, renewed);
        dueDate = renewed;
    }

    private void refresh() throws SQLException {
        Loan stored = DatabaseOperations.getLoanById(loanId);
        if (stored == null) {
            throw new IllegalStateException("No loan found with ID: " + loanId);
        }
        dateReturned = stored.dateReturned;
        finePaid = stored.finePaid;
        receiverId = stored.receiverId;
        dueDate = stored.dueDate;
    }

    static Loan latestForBook(int bookId) throws SQLException {
        return DatabaseOperations.getLoansByBookId(bookId).stream()
                .max(Comparator.comparingInt(Loan::getLoanId))
                .orElseThrow(() -> new IllegalStateException("No loan found for book " + bookId + "."));
    }

    static LocalDate toLocalDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
}
