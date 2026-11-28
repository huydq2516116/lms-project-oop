package lms.object;

import java.util.Date;

public class Loan {
    private final int loanId;
    private final Date issuedDate;
    private Date dateReturned;
    private boolean finePaid;
    private final int bookId;
    private final int borrowerId;
    private final int issuerId;
    private int receiverId;
    private final Date dueDate;

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

    public Date getDueDate() {
        return dueDate;
    }
}
