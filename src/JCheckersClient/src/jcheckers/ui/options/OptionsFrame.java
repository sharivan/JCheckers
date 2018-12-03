package jcheckers.ui.options;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;

import jcheckers.ui.common.OkCancelPanel;

public class OptionsFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3027734674070182378L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				OptionsFrame frame = new OptionsFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private OptionsPanel optionsPanel;
	private OkCancelPanel okCancelPanel;

	private ArrayList<OptionListener> listeners;

	/**
	 * Create the frame.
	 */
	public OptionsFrame() {
		listeners = new ArrayList<>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 551, 161);
		getContentPane().setLayout(new BorderLayout());

		optionsPanel = new OptionsPanel();
		getContentPane().add(optionsPanel, BorderLayout.CENTER);

		okCancelPanel = new OkCancelPanel();
		getContentPane().add(okCancelPanel, BorderLayout.SOUTH);

		okCancelPanel.addOKActionListener((e) -> onOK());
		okCancelPanel.addCancelActionListener((e) -> onCancel());
	}

	public void addListener(OptionListener listener) {
		listeners.add(listener);
	}

	public URL getURL() throws MalformedURLException {
		return optionsPanel.getURL();
	}

	public String getURLStr() {
		return optionsPanel.getURLStr();
	}

	private void onCancel() {
		setVisible(false);
		dispose();
	}

	private void onOK() {
		setVisible(false);
		dispose();
	}

	public void removeListener(OptionListener listener) {
		listeners.remove(listener);
	}

	public void setURL(URL url) {
		optionsPanel.setURL(url);
	}

	public void setURLStr(String url) {
		optionsPanel.setURLStr(url);
	}

}
