package jcheckers.ui.register;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

import jcheckers.ui.common.OkCancelPanel;

public class RegisterFrame extends JFrame {

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
				RegisterFrame frame = new RegisterFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private JLabel lblWarn;
	private RegisterPanel registerPanel;

	private OkCancelPanel okCancelPanel;

	private ArrayList<RegisterListener> listeners;

	/**
	 * Create the frame.
	 */
	public RegisterFrame() {
		listeners = new ArrayList<>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 165);
		getContentPane().setLayout(new BorderLayout());

		lblWarn = new JLabel("Usu\u00E1rio ou senha inv\u00E1lidos.");
		lblWarn.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblWarn.setForeground(Color.RED);
		lblWarn.setVisible(false);
		getContentPane().add(lblWarn, BorderLayout.NORTH);

		registerPanel = new RegisterPanel();
		GridBagLayout gridBagLayout = (GridBagLayout) registerPanel.getLayout();
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		getContentPane().add(registerPanel, BorderLayout.CENTER);

		okCancelPanel = new OkCancelPanel();
		okCancelPanel.setOKText("Registrar");
		getContentPane().add(okCancelPanel, BorderLayout.SOUTH);

		okCancelPanel.addOKActionListener((e) -> onOK());
		okCancelPanel.addCancelActionListener((e) -> onCancel());
	}

	public void addListener(RegisterListener listener) {
		listeners.add(listener);
	}

	public String getEmail() {
		return registerPanel.getEmail();
	}

	public char[] getPassword() {
		return registerPanel.getPassword();
	}

	public String getUsername() {
		return registerPanel.getUsername();
	}

	private void onCancel() {
		for (RegisterListener listener : listeners)
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
		for (RegisterListener listener : listeners)
			if (listener != null)
				try {
					listener.onRegister(getUsername(), getPassword(), getEmail());
				} catch (Throwable e) {
					e.printStackTrace();
				}

		setVisible(false);
		dispose();
	}

	public void removeListener(RegisterListener listener) {
		listeners.remove(listener);
	}

	public void setEMail(String email) {
		registerPanel.setEMail(email);
	}

	public void setPassword(String password) {
		registerPanel.setPassword(password);
	}

	public void setUsername(String username) {
		registerPanel.setUsername(username);
	}

	public void warn(boolean warn) {
		lblWarn.setVisible(warn);
	}

	public void warn(int warnIndex) {
		switch (warnIndex) {
			case 0:
				lblWarn.setVisible(false);
				registerPanel.highlightLabel(0);
				break;

			case 1:
				lblWarn.setVisible(true);
				lblWarn.setText("Usuário invlálido.");
				registerPanel.highlightLabel(1);
				break;

			case 2:
				lblWarn.setVisible(true);
				lblWarn.setText("Senha invlálida.");
				registerPanel.highlightLabel(2);
				break;

			case 3:
				lblWarn.setVisible(true);
				lblWarn.setText("e-mail invlálido.");
				registerPanel.highlightLabel(4);
				break;

			case 4:
				lblWarn.setVisible(true);
				lblWarn.setText("Usuário já registrado.");
				registerPanel.highlightLabel(1);
				break;
		}
	}

}
