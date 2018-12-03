package jcheckers.ui.lobby;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jcheckers.client.net.RoomInfo;

public class RoomListPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				RoomListPanel panel = new RoomListPanel();

				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(new BorderLayout(0, 0));
				frame.setSize(new Dimension(800, 600));
				frame.setVisible(true);

				frame.getContentPane().add(panel, BorderLayout.CENTER);

				panel.addEntry(1, "Test", 2, 4, true);
				panel.addEntry(2, "Lalala", 3, 8, true);
				panel.addEntry(3, "LOL", 10, 12, true);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	private JPanel entryPanel;

	private ArrayList<RoomEntryListener> listeners;
	private ArrayList<RoomEntryPanel> entries;

	/**
	 * Create the panel.
	 */
	public RoomListPanel() {
		listeners = new ArrayList<>();
		entries = new ArrayList<>();

		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		entryPanel = new JPanel();
		scrollPane.setViewportView(entryPanel);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		entryPanel.setLayout(gridBagLayout);
	}

	public void addEntries(List<RoomInfo> list) {
		for (RoomInfo info : list)
			addEntry(info.getID(), info.getName(), info.getUsers(), info.getMaxUsers(), true);
	}

	public void addEntry(int id, String roomName, int userCount, int maxUsers, boolean joinEnabled) {
		RoomEntryPanel entry = new RoomEntryPanel();
		GridBagConstraints gbc_entry = new GridBagConstraints();
		gbc_entry.fill = GridBagConstraints.HORIZONTAL;
		gbc_entry.anchor = GridBagConstraints.NORTH;
		gbc_entry.gridx = 0;
		gbc_entry.gridy = entries.size();
		entryPanel.add(entry, gbc_entry);

		entry.setID(id);
		entry.setRoomName(roomName);
		entry.setUserCount(userCount);
		entry.setMaxUsers(maxUsers);
		entry.setJoinEnabled(joinEnabled);

		for (RoomEntryListener listener : listeners)
			entry.addListener(listener);

		entries.add(entry);
	}

	public void addListener(RoomEntryListener listener) {
		listeners.add(listener);

		for (RoomEntryPanel entry : entries)
			entry.addListener(listener);
	}

	public void removeListener(RoomEntryListener listener) {
		listeners.remove(listener);

		for (RoomEntryPanel entry : entries)
			entry.removeListener(listener);
	}

}
