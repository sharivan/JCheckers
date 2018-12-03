package jcheckers.ui.lobby;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RoomEntryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	private JLabel lblUserCount;
	private JLabel lblRoomName;
	private JButton btnJoin;
	private int id;
	private int maxUsers;
	private int userCount;
	private String roomName;

	private ArrayList<RoomEntryListener> listeners;

	/**
	 * Create the panel.
	 */
	public RoomEntryPanel() {
		listeners = new ArrayList<>();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 48, 298, 66 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		setLayout(gridBagLayout);

		lblUserCount = new JLabel("0/0");
		GridBagConstraints gbc_lblUserCount = new GridBagConstraints();
		gbc_lblUserCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblUserCount.gridx = 0;
		gbc_lblUserCount.gridy = 0;
		add(lblUserCount, gbc_lblUserCount);

		lblRoomName = new JLabel("Sala #");
		GridBagConstraints gbc_lblRoomName = new GridBagConstraints();
		gbc_lblRoomName.anchor = GridBagConstraints.WEST;
		gbc_lblRoomName.insets = new Insets(0, 0, 0, 5);
		gbc_lblRoomName.gridx = 1;
		gbc_lblRoomName.gridy = 0;
		add(lblRoomName, gbc_lblRoomName);

		btnJoin = new JButton("Entrar");
		btnJoin.addActionListener((e) -> onJoin());
		GridBagConstraints gbc_btnJoin = new GridBagConstraints();
		gbc_btnJoin.anchor = GridBagConstraints.EAST;
		gbc_btnJoin.gridx = 2;
		gbc_btnJoin.gridy = 0;
		add(btnJoin, gbc_btnJoin);
	}

	public void addListener(RoomEntryListener listener) {
		listeners.add(listener);
	}

	public int getID() {
		return id;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public String getRoomName() {
		return roomName;
	}

	public int getUserCount() {
		return userCount;
	}

	public boolean isJoinEnabled() {
		return btnJoin.isEnabled();
	}

	private void onJoin() {
		for (RoomEntryListener listener : listeners)
			if (listener != null)
				try {
					listener.onJoin(id);
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	public void removeListener(RoomEntryListener listener) {
		listeners.remove(listener);
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setJoinEnabled(boolean enabled) {
		btnJoin.setEnabled(enabled);
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
		lblUserCount.setText(userCount + "/" + maxUsers);
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
		lblRoomName.setText(roomName);
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
		lblUserCount.setText(userCount + "/" + maxUsers);
	}

}
