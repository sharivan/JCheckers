package jcheckers.ui.user;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jcheckers.client.net.User;
import jcheckers.client.net.boards.draughts.DraughtsUser;

/**
 * 
 * Painel que representa uma lista de usuários da sala ou da mesa.
 * @author miste
 *
 */
public class UserListPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	private static int compareByName(UserEntryPanel left, UserEntryPanel right) {
		int result = compareByNameOnly(left, right);
		if (result == 0)
			return compareByRatingOnly(left, right);

		return result;
	}

	private static int compareByNameOnly(UserEntryPanel left, UserEntryPanel right) {
		return left.getName().compareTo(right.getName());
	}

	private static int compareByRating(UserEntryPanel left, UserEntryPanel right) {
		int result = compareByRatingOnly(left, right);
		if (result == 0)
			return compareByNameOnly(left, right);

		return result;
	}

	private static int compareByRatingOnly(UserEntryPanel left, UserEntryPanel right) {
		int leftRating = left.getRating();
		int rightRating = right.getRating();

		if (leftRating > rightRating)
			return 1;

		if (leftRating < rightRating)
			return -1;

		return 0;
	}

	private ArrayList<UserEntryListener> listeners;

	private ArrayList<UserEntryPanel> entries;

	private JPanel entryPanel;

	/**
	 * Create the panel.
	 */
	public UserListPanel() {
		listeners = new ArrayList<>();
		entries = new ArrayList<>();

		setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);

		entryPanel = new JPanel();
		GridBagLayout gbl_entryPanel = new GridBagLayout();
		gbl_entryPanel.columnWeights = new double[] { 1.0 };
		entryPanel.setLayout(gbl_entryPanel);

		scrollPane.setViewportView(entryPanel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		entryPanel.add(new JPanel(), gbc);
	}

	public void addEntries(List<DraughtsUser> users) {
		for (DraughtsUser user : users)
			addEntry(user);
	}

	public void addEntries(User[] users) {
		for (User user : users)
			addEntry((DraughtsUser) user);
	}

	public void addEntry(DraughtsUser user) {
		addEntry(user.getID(), user.getName(), user.getRating());
	}

	public void addEntry(int id, String name, int rating) {
		UserEntryPanel entry = new UserEntryPanel();
		entry.setPreferredSize(new Dimension(entryPanel.getWidth(), 30));
		GridBagConstraints gbc_entry = new GridBagConstraints();
		gbc_entry.fill = GridBagConstraints.HORIZONTAL;
		gbc_entry.gridwidth = GridBagConstraints.REMAINDER;
		gbc_entry.weightx = 1;
		entryPanel.add(entry, gbc_entry, entries.size());

		entry.setID(id);
		entry.setName(name);
		entry.setRating(rating);

		for (UserEntryListener listener : listeners)
			entry.addListener(listener);

		entries.add(entry);
		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void addListener(UserEntryListener listener) {
		listeners.add(listener);

		for (UserEntryPanel entry : entries)
			entry.addListener(listener);
	}

	public void clear() {
		for (int i = entries.size() - 1; i >= 0; i--)
			removeEntry(i);
	}

	public UserEntryPanel entryOf(DraughtsUser user) {
		int index = indexOf(user);
		if (index == -1)
			return null;

		return entries.get(index);
	}

	public int indexOf(DraughtsUser user) {
		for (int i = 0; i < entries.size(); i++) {
			UserEntryPanel entry = entries.get(i);
			if (entry.getID() == user.getID())
				return i;
		}

		return -1;
	}

	private void refreshEntries() {
		entryPanel.removeAll();

		for (int i = 0; i < entries.size(); i++) {
			UserEntryPanel entry = entries.get(i);
			GridBagConstraints gbc_entry = new GridBagConstraints();
			gbc_entry.fill = GridBagConstraints.HORIZONTAL;
			// gbc_entry.anchor = GridBagConstraints.NORTH;
			gbc_entry.gridwidth = GridBagConstraints.REMAINDER;
			gbc_entry.weightx = 1;
			entryPanel.add(entry, gbc_entry, i);
		}

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void removeEntry(DraughtsUser user) {
		int index = indexOf(user);
		if (index == -1)
			return;

		removeEntry(index);
	}

	public void removeEntry(int index) {
		UserEntryPanel entry = entries.get(index);
		entryPanel.remove(entry);
		entries.remove(index);

		for (UserEntryListener listener : listeners)
			entry.removeListener(listener);

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void removeListener(UserEntryListener listener) {
		listeners.remove(listener);

		for (UserEntryPanel entry : entries)
			entry.removeListener(listener);
	}

	public void sortByName(boolean descending) {
		entries.sort((left, right) -> descending ? -compareByName(left, right) : compareByName(left, right));
		refreshEntries();
	}

	public void sortByRating(boolean descending) {
		entries.sort((left, right) -> descending ? -compareByRating(left, right) : compareByRating(left, right));
	}

	public void updateEntries(User[] users) {
		for (User user : users)
			updateEntry((DraughtsUser) user);
	}

	public void updateEntry(DraughtsUser user) {
		int index = indexOf(user);
		if (index == -1)
			return;

		UserEntryPanel entry = entries.get(index);
		entry.setName(user.getName());
		entry.setRating(user.getRating());
	}

}
