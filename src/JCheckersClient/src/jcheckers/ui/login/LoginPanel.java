package jcheckers.ui.login;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class LoginPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7228258944437323271L;
	private JTextField txtUsername;
	private JPasswordField txtPassword;

	/**
	 * Create the panel.
	 */
	public LoginPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
		setLayout(gridBagLayout);

		JLabel lblUsername = new JLabel("Usuário:");
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.anchor = GridBagConstraints.EAST;
		gbc_lblUsername.gridx = 0;
		gbc_lblUsername.gridy = 0;
		add(lblUsername, gbc_lblUsername);

		txtUsername = new JTextField();
		txtUsername.setText("");
		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
		gbc_txtUsername.insets = new Insets(0, 0, 5, 0);
		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUsername.gridx = 1;
		gbc_txtUsername.gridy = 0;
		add(txtUsername, gbc_txtUsername);
		txtUsername.setColumns(10);

		JLabel lblPassword = new JLabel("Senha:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		add(lblPassword, gbc_lblPassword);

		txtPassword = new JPasswordField();
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 1;
		add(txtPassword, gbc_txtPassword);
	}

	public char[] getPassword() {
		return txtPassword.getPassword();
	}

	public String getUsername() {
		return txtUsername.getText();
	}

	public void setPassword(String password) {
		txtPassword.setText(password);
	}

	public void setUsername(String username) {
		txtUsername.setText(username);
	}

}
