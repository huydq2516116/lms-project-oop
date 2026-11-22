package lms.person;

import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;

import lms.database.DatabaseOperations;

public abstract class Staff extends Person {
	private double salary;
	protected int employeeId;
	public Staff(int id, String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo);
		this.salary = salary;
		this.employeeId = id;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary){
		this.salary = salary;
	}

	// Register a new borrower
    public void registerBorrower(
            String username, String password, String name,
            String address, String phoneNo) throws SQLException {

        DatabaseOperations.createPerson(
            username, password, name, address, phoneNo, "BORROWER"
        );
    }

	// Update borrower's personal information
    public void updateBorrower(Borrower borrower)
            throws SQLException {

        if (borrower == null) {
            throw new IllegalArgumentException(
                "Borrower cannot be null."
            );
        }

        DatabaseOperations.updatePerson(
            borrower.getId(),
            borrower.getUserName(),
            borrower.getAddress(),
            borrower.getPhoneNo()
        );
    }

    // Check in a returned item
    public void checkIn(int loanId) throws SQLException {

        Date returnDate = Date.valueOf(LocalDate.now());

        DatabaseOperations.updateLoanReturnDate(
            loanId, returnDate
        );
    }

    
	
}
