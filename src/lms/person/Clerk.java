package lms.person;

public class Clerk extends Staff {
	private final int deskNo;
	public Clerk(int id, int deskNo, String userName, String password, String name, String address, String phoneNo, double salary) {
		super(id, userName, password, name, address, phoneNo, salary);
		this.deskNo = deskNo;
	}

	

	public int getDeskNo() {
		return deskNo;
	}
	
}
