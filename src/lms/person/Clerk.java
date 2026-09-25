package lms.person;

public class Clerk extends Staff {
	private static int currentDeskNo = 0;
	private int deskNo;
	public Clerk(String userName, String password, String name, String address, String phoneNo, double salary) {
		super(userName, password, name, address, phoneNo, salary);
		this.deskNo = currentDeskNo++;
	}

	

	public int getDeskNo() {
		return deskNo;
	}
	
}
