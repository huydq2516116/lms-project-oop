package lms.person;

public class Librarian extends Staff {
	private static int currentOfficeNo = 0;
	private int officeNo;
	public Librarian(String userName, String password, String name, String address, String phoneNo, double salary) {
		super(userName, password, name, address, phoneNo, salary);
		this.officeNo = currentOfficeNo++;
	}

	

	public int getOfficeNo() {
		return officeNo;
	}
}
