package jcheckers.ui.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

import jcheckers.ui.common.OkCancelPanel;

public class LoginFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4192542790849265663L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				LoginFrame frame = new LoginFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private LoginPanel loginPanel;

	private OkCancelPanel okCancelPanel;

	private ArrayList<LoginListener> listeners;

	private Component lblWarn;

	/**
	 * Create the frame.
	 */
	public LoginFrame() {
		listeners = new ArrayList<>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 146);
		getContentPane().setLayout(new BorderLayout());

		lblWarn = new JLabel("Usu\u00E1rio ou senha inv\u00E1lidos.");
		lblWarn.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblWarn.setForeground(Color.RED);
		lblWarn.setVisible(false);
		getContentPane().add(lblWarn, BorderLayout.NORTH);

		loginPanel = new LoginPanel();
		GridBagLayout gridBagLayout = (GridBagLayout) loginPanel.getLayout();
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		getContentPane().add(loginPanel, BorderLayout.CENTER);

		okCancelPanel = new OkCancelPanel();
		okCancelPanel.setOKText("Login");
		getContentPane().add(okCancelPanel, BorderLayout.SOUTH);

		okCancelPanel.addOKActionListener((e) -> onOK());
		okCancelPanel.addCancelActionListener((e) -> onCancel());
	}

	public void addListener(LoginListener listener) {
		listeners.add(listener);
	}

	public char[] getPassword() {
		return loginPanel.getPassword();
	}

	public String getUsername() {
		return loginPanel.getUsername();
	}

	private void onCancel() {
		for (LoginListener listener : listeners)
			if (listener != null)
				try {
					listener.onCancel();
				} catch (Throwable e) {
					e.printStackTrace();
				}

		setVisible(false);
		dispose();
	}

	private void onOK() {
		for (LoginListener listener : listeners)
			if (listener != null)
				try {
					listener.onLogin(getUsername(), getPassword());
				} catch (Throwable e) {
					e.printStackTrace();
				}

		setVisible(false);
		dispose();
	}

	public void removeListener(LoginListener listener) {
		listeners.remove(listener);
	}

	public void setPassword(String password) {
		loginPanel.setPassword(password);
	}

	public void setUsername(String username) {
		loginPanel.setUsername(username);
	}

	public void warn(boolean warn) {
		lblWarn.setVisible(warn);
	}

}
