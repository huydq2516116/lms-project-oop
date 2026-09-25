package lms.person;

public abstract class Staff extends Person {
	private double salary;
	public Staff(String userName, String password, String name, String address, String phoneNo, double salary) {
		super(userName, password, name, address, phoneNo);
		this.salary = salary;
	}

	public double getSalary() {
		return salary;
	}
	
	
}
