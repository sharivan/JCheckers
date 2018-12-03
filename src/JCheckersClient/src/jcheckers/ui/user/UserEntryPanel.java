package jcheckers.ui.user;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class UserEntryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	private ArrayList<UserEntryListener> listeners;

	private int id;
	private String name;
	private int rating;

	private JLabel lblName;
	private JLabel lblRating;

	/**
	 * Create the panel.
	 */
	public UserEntryPanel() {
		setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		listeners = new ArrayList<>();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] { 30 };
		gridBagLayout.columnWidths = new int[] { 300, 49 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 1.0 };
		setLayout(gridBagLayout);

		lblName = new JLabel("user");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 10, 5, 5);
		gbc_lblName.fill = GridBagConstraints.BOTH;
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

		lblRating = new JLabel("1200");
		GridBagConstraints gbc_lblRating = new GridBagConstraints();
		gbc_lblRating.anchor = GridBagConstraints.WEST;
		gbc_lblRating.fill = GridBagConstraints.VERTICAL;
		gbc_lblRating.insets = new Insets(0, 0, 5, 0);
		gbc_lblRating.gridx = 1;
		gbc_lblRating.gridy = 0;
		add(lblRating, gbc_lblRating);
	}

	public void addListener(UserEntryListener listener) {
		listeners.add(listener);
	}

	public int getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getRating() {
		return rating;
	}

	public void removeListener(UserEntryListener listener) {
		listeners.remove(listener);
	}

	public void setID(int id) {
		this.id = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		lblName.setText(name);
	}

	public void setRating(int rating) {
		this.rating = rating;
		lblRating.setText(Integer.toString(rating));
	}

}
