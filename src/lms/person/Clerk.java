package lms.person;

import java.sql.SQLException;
import java.time.LocalDate;

import lms.database.DatabaseOperations;

public class Clerk extends Staff {
	private final int deskNo;
	public Clerk(int id, int deskNo, String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo, salary);
		this.deskNo = deskNo;
	}

	

	public int getDeskNo() {
		return deskNo;
	}
	
	// Check out an item for a borrower
    public void checkOut(int borrowerId, int bookId)
            throws SQLException {

        DatabaseOperations.createLoan(bookId, borrowerId, this.employeeId);
    }

    // Renew an item's loan
    public void renewLoan(int loanId, LocalDate newDueDate)
            throws SQLException {

        if (newDueDate == null) {
            throw new IllegalArgumentException(
                "Due date cannot be null."
            );
        }

        DatabaseOperations.updateLoanDueDate(
            loanId, newDueDate.toString()
        );
    }

    // Record that a fine has been paid
    public void recordFinePayment(int loanId)
            throws SQLException {

        DatabaseOperations.updateFineStatus(loanId);
    }
}
