package jcheckers.ui.options;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class OptionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;
	private JTextField txtURL;

	/**
	 * Create the panel.
	 */
	public OptionsPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		setLayout(gridBagLayout);

		JLabel lbLoginURL = new JLabel("URL:");
		GridBagConstraints gbc_lbLoginURL = new GridBagConstraints();
		gbc_lbLoginURL.insets = new Insets(0, 0, 5, 5);
		gbc_lbLoginURL.anchor = GridBagConstraints.EAST;
		gbc_lbLoginURL.gridx = 0;
		gbc_lbLoginURL.gridy = 0;
		add(lbLoginURL, gbc_lbLoginURL);

		txtURL = new JTextField();
		txtURL.setText("http://localhost:8080/JCheckersServer/CoreServlet");
		GridBagConstraints gbc_txtURL = new GridBagConstraints();
		gbc_txtURL.insets = new Insets(0, 0, 5, 0);
		gbc_txtURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtURL.gridx = 1;
		gbc_txtURL.gridy = 0;
		add(txtURL, gbc_txtURL);
		txtURL.setColumns(10);
	}

	public URL getURL() throws MalformedURLException {
		return new URL(txtURL.getText());
	}

	public String getURLStr() {
		return txtURL.getText();
	}

	public void setURL(URL url) {
		txtURL.setText(url.toString());
	}

	public void setURLStr(String url) {
		txtURL.setText(url);
	}

}
