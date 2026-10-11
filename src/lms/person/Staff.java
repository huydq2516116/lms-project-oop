package lms.person;

public abstract class Staff extends Person {
	private double salary;
	public Staff(int id, String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo);
		this.salary = salary;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary){
		this.salary = salary;
	}
	
	
}
