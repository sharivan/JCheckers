package jcheckers.ui.register;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class RegisterPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7228258944437323271L;

	private Font DEFAULT_LABEL_FONT = new Font("Tahoma", Font.PLAIN, 11);
	private Font HIGHLIGHT_LABEL_FONT = new Font("Tahoma", Font.BOLD, 11);

	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JTextField txtEMail;
	private Component lblUsername;
	private JLabel lblPassword;
	private JLabel lblEMail;

	/**
	 * Create the panel.
	 */
	public RegisterPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		lblUsername = new JLabel("Usuário:");
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

		lblPassword = new JLabel("Senha:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		add(lblPassword, gbc_lblPassword);

		txtPassword = new JPasswordField();
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.fill = GridBagConstraints.BOTH;
		gbc_txtPassword.insets = new Insets(0, 0, 5, 0);
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 1;
		add(txtPassword, gbc_txtPassword);

		lblEMail = new JLabel("e-mail:");
		GridBagConstraints gbc_lblEMail = new GridBagConstraints();
		gbc_lblEMail.insets = new Insets(0, 0, 5, 5);
		gbc_lblEMail.anchor = GridBagConstraints.EAST;
		gbc_lblEMail.gridx = 0;
		gbc_lblEMail.gridy = 2;
		add(lblEMail, gbc_lblEMail);

		txtEMail = new JTextField();
		GridBagConstraints gbc_txtEMail = new GridBagConstraints();
		gbc_txtEMail.fill = GridBagConstraints.BOTH;
		gbc_txtEMail.insets = new Insets(0, 0, 5, 0);
		gbc_txtEMail.gridx = 1;
		gbc_txtEMail.gridy = 2;
		add(txtEMail, gbc_txtEMail);
	}

	public String getEmail() {
		return txtEMail.getText();
	}

	public char[] getPassword() {
		return txtPassword.getPassword();
	}

	public String getUsername() {
		return txtUsername.getText();
	}

	public void highlightLabel(int bits) {
		if ((bits & 1) != 0) {
			lblUsername.setForeground(Color.RED);
			lblUsername.setFont(HIGHLIGHT_LABEL_FONT);
		} else {
			lblUsername.setForeground(Color.BLACK);
			lblUsername.setFont(DEFAULT_LABEL_FONT);
		}

		if ((bits & 2) != 0) {
			lblPassword.setForeground(Color.RED);
			lblPassword.setFont(HIGHLIGHT_LABEL_FONT);
		} else {
			lblPassword.setForeground(Color.BLACK);
			lblPassword.setFont(DEFAULT_LABEL_FONT);
		}

		if ((bits & 4) != 0) {
			lblEMail.setForeground(Color.RED);
			lblEMail.setFont(HIGHLIGHT_LABEL_FONT);
		} else {
			lblEMail.setForeground(Color.BLACK);
			lblEMail.setFont(DEFAULT_LABEL_FONT);
		}
	}

	public void setEMail(String email) {
		txtEMail.setText(email);
	}

	public void setPassword(String password) {
		txtPassword.setText(password);
	}

	public void setUsername(String username) {
		txtUsername.setText(username);
	}

}
