package lms.screen;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginScreen extends JFrame {
	private static final long serialVersionUID = 3908597196893209836L;
	private JTextField userNameInput;
	private JTextField passwordInput;
	public LoginScreen() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel loginPanel = createLoginPanel();
		cp.add(loginPanel);
		setVisible(true);
		setTitle("Login");
		setSize(350,120);
	}
	
	private JPanel createLoginPanel() {
		JPanel login = new JPanel();
		login.setLayout(new GridLayout(3,2,2,2));
		login.add(new JLabel("Username: "));
		userNameInput = new JTextField(100);
		login.add(userNameInput);
		login.add(new JLabel("Password: "));
		passwordInput = new JTextField(100);
		login.add(passwordInput);
		login.add(new JButton("Login"));
		return login;
	}
}
