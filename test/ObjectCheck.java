import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import lms.object.HoldRequest;
import lms.object.Loan;

/** Checks the date, fine and queue rules without touching the database. */
class ObjectCheck {
    public static void main(String[] args) {
        LocalDate issued = LocalDate.of(2026, 11, 1);
        LocalDate due = LocalDate.of(2026, 12, 1);
        Loan loan = new Loan(1, Date.valueOf(issued), null, false, 2, 3, 4, 0, Date.valueOf(due));
        check(!loan.isOverdue(due) && loan.calculateFine(due, 100) == 0, "No fine on the due date");
        check(loan.isOverdue(due.plusDays(1)) && loan.calculateFine(due.plusDays(1), 100) == 100, "One late day");
        loan.setDateReturned(Date.valueOf(due.plusDays(2)));
        check(!loan.isOverdue(due.plusDays(9)) && loan.calculateFine(due.plusDays(9), 100) == 200, "Fine stops on return");
        loan.setFinePaid(true);
        check(loan.calculateFine(due.plusDays(9), 100) == 0, "Paid fine is zero");

        Loan noDueDate = new Loan(2, Date.valueOf(issued), null, false, 2, 3, 4, 0, null);
        check(noDueDate.getDueDate().equals(Date.valueOf(due)), "Missing due date falls back to 30 days");

        HoldRequest hold = new HoldRequest(9, Date.valueOf(issued), 3, 2);
        check(hold.getHoldRequestId() == 9 && hold.getBorrowerId() == 3 && hold.getBookId() == 2, "Column order");
        LocalDate expiry = issued.plusDays(7);
        check(!hold.isExpired(expiry, 7) && hold.isExpired(expiry.plusDays(1), 7), "Expiry boundary");
        HoldRequest sameDay = new HoldRequest(5, Date.valueOf(issued), 4, 2);
        HoldRequest later = new HoldRequest(1, Date.valueOf(issued.plusDays(1)), 5, 2);
        check(HoldRequest.firstInQueue(List.of(hold, later, sameDay)) == sameDay, "Oldest date, then ID");
        check(HoldRequest.firstInQueue(List.of()) == null, "Empty queue");
        System.out.println("ObjectCheck passed");
    }

    private static void check(boolean passed, String message) {
        if (!passed) {
            throw new AssertionError(message);
        }
    }
}
