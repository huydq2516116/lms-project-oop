package lms.person;

public class Librarian extends Staff {
	private final int officeNo;
	public Librarian(int id, int officeNo ,String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo, salary);
		this.officeNo = officeNo;
	}
	public int getOfficeNo() {
		return officeNo;
	}
}
