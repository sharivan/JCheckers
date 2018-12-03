package jcheckers.ui.lobby;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TableEntryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	private int id;
	private int number;
	private boolean sit1Visible;
	private boolean sit2Visible;

	private ArrayList<TableEntryListener> listeners;

	private JLabel lblTableNumber;
	private JButton btnWatch;

	private JLabel lblPlayer1;
	private JButton btnSit1;
	private JLabel lblPlayer2;
	private JButton btnSit2;

	private JPanel pnlPlayer1;
	private JPanel pnlPlayer2;

	/**
	 * Create the panel.
	 */
	public TableEntryPanel() {
		listeners = new ArrayList<>();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 48, 170, 66 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 1.0 };
		setLayout(gridBagLayout);

		lblTableNumber = new JLabel("#");
		GridBagConstraints gbc_lblTableNumber = new GridBagConstraints();
		gbc_lblTableNumber.insets = new Insets(0, 0, 0, 5);
		gbc_lblTableNumber.gridx = 0;
		gbc_lblTableNumber.gridy = 0;
		add(lblTableNumber, gbc_lblTableNumber);

		JPanel pnlPlayers = new JPanel();
		GridBagConstraints gbc_pnlPlayers = new GridBagConstraints();
		gbc_pnlPlayers.insets = new Insets(0, 0, 0, 5);
		gbc_pnlPlayers.fill = GridBagConstraints.BOTH;
		gbc_pnlPlayers.gridx = 1;
		gbc_pnlPlayers.gridy = 0;
		add(pnlPlayers, gbc_pnlPlayers);
		pnlPlayers.setLayout(new GridLayout(0, 2, 0, 0));

		pnlPlayer1 = new JPanel();
		pnlPlayers.add(pnlPlayer1);
		pnlPlayer1.setLayout(new BorderLayout(0, 0));

		lblPlayer1 = new JLabel("Player1");
		lblPlayer1.setHorizontalAlignment(SwingConstants.CENTER);
		pnlPlayer1.add(lblPlayer1, BorderLayout.CENTER);

		btnSit1 = new JButton("Sentar");
		btnSit1.addActionListener((e) -> onSit(0));

		pnlPlayer2 = new JPanel();
		pnlPlayers.add(pnlPlayer2);
		pnlPlayer2.setLayout(new BorderLayout(0, 0));

		lblPlayer2 = new JLabel("Player2");
		lblPlayer2.setHorizontalAlignment(SwingConstants.CENTER);
		pnlPlayer2.add(lblPlayer2, BorderLayout.CENTER);

		btnSit2 = new JButton("Sentar");
		btnSit2.addActionListener((e) -> onSit(1));

		btnWatch = new JButton("Assistir");
		btnWatch.addActionListener((e) -> onWatch());
		GridBagConstraints gbc_btnWatch = new GridBagConstraints();
		gbc_btnWatch.anchor = GridBagConstraints.EAST;
		gbc_btnWatch.gridx = 2;
		gbc_btnWatch.gridy = 0;
		add(btnWatch, gbc_btnWatch);
	}

	public void addListener(TableEntryListener listener) {
		listeners.add(listener);
	}

	public int getID() {
		return id;
	}

	public int getNumber() {
		return number;
	}

	public String getPlayer1() {
		return lblPlayer1.getText();
	}

	public String getPlayer2() {
		return lblPlayer2.getText();
	}

	public boolean isSit1Visible() {
		return sit1Visible;
	}

	public boolean isSit2Visible() {
		return sit2Visible;
	}

	private void onSit(int sitIndex) {
		for (TableEntryListener listener : listeners)
			if (listener != null)
				try {
					listener.onSit(id, sitIndex);
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	private void onWatch() {
		for (TableEntryListener listener : listeners)
			if (listener != null)
				try {
					listener.onWatch(id);
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	public void removeListener(TableEntryListener listener) {
		listeners.remove(listener);
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setNumber(int number) {
		this.number = number;
		lblTableNumber.setText("#" + number);
	}

	public void setPlayer1(String name) {
		lblPlayer1.setText(name);
	}

	public void setPlayer2(String name) {
		lblPlayer2.setText(name);
	}

	public void setSit1Visible(boolean value) {
		if (value && !sit1Visible) {
			sit1Visible = true;
			pnlPlayer1.remove(lblPlayer1);
			pnlPlayer1.add(btnSit1, BorderLayout.CENTER);
			pnlPlayer1.revalidate();
			pnlPlayer1.repaint();
		} else if (!value && sit1Visible) {
			sit1Visible = false;
			pnlPlayer1.remove(btnSit1);
			pnlPlayer1.add(lblPlayer1, BorderLayout.CENTER);
			pnlPlayer1.revalidate();
			pnlPlayer1.repaint();
		}
	}

	public void setSit2Visible(boolean value) {
		if (value && !sit2Visible) {
			sit2Visible = true;
			pnlPlayer2.remove(lblPlayer2);
			pnlPlayer2.add(btnSit2, BorderLayout.CENTER);
			pnlPlayer2.revalidate();
			pnlPlayer2.repaint();
		} else if (!value && sit2Visible) {
			sit2Visible = false;
			pnlPlayer2.remove(btnSit2);
			pnlPlayer2.add(lblPlayer2, BorderLayout.CENTER);
			pnlPlayer2.revalidate();
			pnlPlayer2.repaint();
		}
	}

	public void setWatchEnabled(boolean value) {
		btnWatch.setEnabled(value);
	}

}
