package lms.screen;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lms.database.DatabaseOperations;

public class LoginScreen extends JFrame {
	private static final long serialVersionUID = 3908597196893209836L;
	private JTextField userNameInput;
	private JTextField passwordInput;
	private final JLabel loginResult;

	public LoginScreen() {
		loginResult = new JLabel("");
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel loginPanel = createLoginPanel();
		cp.add(loginPanel);
		//cp.add(loginResult);
		setVisible(true);
		setTitle("Login");
		setSize(350, 120);
	}

	private JPanel createLoginPanel() {
		JPanel login = new JPanel();
		login.setLayout(new GridLayout(3, 2, 2, 2));
		login.add(new JLabel("Username: "));
		userNameInput = new JTextField(100);
		login.add(userNameInput);
		login.add(new JLabel("Password: "));
		passwordInput = new JTextField(100);
		login.add(passwordInput);
		login.add(createLoginButton());
		login.add(loginResult);
		return login;
	}

	private JButton createLoginButton() {
		JButton button = new JButton("login");
		button.addActionListener((ActionEvent e) -> {
                    loginButtonPressed();
                });
		return button;
	}

	private void loginButtonPressed() {
		String username = userNameInput.getText();
		String password = passwordInput.getText();

		String realPassword;

		try {
			realPassword = DatabaseOperations.findPasswordByUserName(username);
			if (password.equals(realPassword)) {
				loginResult.setText("Login successful!");
			}
			else
			{
				loginResult.setText("Username or password is incorrect");
			}
		} catch (SQLException ex) {
			System.getLogger(LoginScreen.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
		}

	}
}
