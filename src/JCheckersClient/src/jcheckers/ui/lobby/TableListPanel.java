package jcheckers.ui.lobby;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jcheckers.client.net.Table;

/**
 * 
 * Painel que implementa uma lista de mesas de uma sala.
 * @author miste
 *
 */
public class TableListPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339962114063466858L;

	private static int compareByRatingOnly(TableEntryPanel left, TableEntryPanel right) {
		int leftNumber = left.getNumber();
		int rightNumber = right.getNumber();

		if (leftNumber > rightNumber)
			return 1;

		if (leftNumber < rightNumber)
			return -1;

		return 0;
	}

	private JPanel entryPanel;
	private ArrayList<TableEntryListener> listeners;

	private ArrayList<TableEntryPanel> entries;

	/**
	 * Create the panel.
	 */
	public TableListPanel() {
		listeners = new ArrayList<>();
		entries = new ArrayList<>();

		setLayout(new BorderLayout(0, 0));

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

	public void addEntries(List<Table> tables) {
		for (Table table : tables)
			addEntry(table);
	}

	public void addEntry(int id, int number, String player1, boolean sit1Visible, String player2, boolean sit2Visible, boolean watchEnabled) {
		TableEntryPanel entry = new TableEntryPanel();
		GridBagConstraints gbc_entry = new GridBagConstraints();
		gbc_entry.fill = GridBagConstraints.HORIZONTAL;
		gbc_entry.gridwidth = GridBagConstraints.REMAINDER;
		gbc_entry.weightx = 1;
		entryPanel.add(entry, gbc_entry, entries.size());

		entry.setID(id);
		entry.setNumber(number);
		entry.setPlayer1(player1 != null ? player1 : "");
		entry.setPlayer2(player2 != null ? player2 : "");
		entry.setSit1Visible(sit1Visible);
		entry.setSit2Visible(sit2Visible);
		entry.setWatchEnabled(watchEnabled);

		for (TableEntryListener listener : listeners)
			entry.addListener(listener);

		entries.add(entry);

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void addEntry(Table table) {
		int seatBits = table.getSeatBits();
		String player1 = (seatBits & 1) != 0 ? table.getSeat(0).getUser().getName() : null;
		String player2 = (seatBits & 2) != 0 ? table.getSeat(1).getUser().getName() : null;

		addEntry(table.getID(), table.getNumber(), player1, player1 == null, player2, player2 == null, true);
	}

	public void addListener(TableEntryListener listener) {
		listeners.add(listener);

		for (TableEntryPanel entry : entries)
			entry.addListener(listener);
	}

	public void clear() {
		for (int i = entries.size() - 1; i >= 0; i--)
			removeEntry(i);
	}

	public TableEntryPanel entryOf(Table table) {
		int index = indexOf(table);
		if (index == -1)
			return null;

		return entries.get(index);
	}

	public int indexOf(Table table) {
		for (int i = 0; i < entries.size(); i++) {
			TableEntryPanel entry = entries.get(i);
			if (entry.getNumber() == table.getNumber())
				return i;
		}

		return -1;
	}

	private void refreshEntries() {
		entryPanel.removeAll();

		for (int i = 0; i < entries.size(); i++) {
			TableEntryPanel entry = entries.get(i);
			GridBagConstraints gbc_entry = new GridBagConstraints();
			gbc_entry.fill = GridBagConstraints.HORIZONTAL;
			gbc_entry.gridwidth = GridBagConstraints.REMAINDER;
			gbc_entry.weightx = 1;
			entryPanel.add(entry, gbc_entry, i);
		}

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void removeEntry(int index) {
		TableEntryPanel entry = entries.get(index);
		entryPanel.remove(entry);
		entries.remove(index);

		for (TableEntryListener listener : listeners)
			entry.removeListener(listener);

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void removeEntry(Table table) {
		int index = indexOf(table);
		if (index == -1)
			return;

		removeEntry(index);
	}

	public void removeListener(TableEntryListener listener) {
		listeners.remove(listener);

		for (TableEntryPanel entry : entries)
			entry.removeListener(listener);
	}

	public void sortByNumber(boolean descending) {
		entries.sort((left, right) -> descending ? -compareByRatingOnly(left, right) : compareByRatingOnly(left, right));
		refreshEntries();
	}

	public void updateEntry(int index, Table table) {
		TableEntryPanel entry = entries.get(index);
		int seatBits = table.getSeatBits();
		String player1 = (seatBits & 1) != 0 ? table.getSeat(0).getUser().getName() : null;
		String player2 = (seatBits & 2) != 0 ? table.getSeat(1).getUser().getName() : null;
		entry.setPlayer1(player1 != null ? player1 : "");
		entry.setSit1Visible(player1 == null);
		entry.setPlayer2(player2 != null ? player2 : "");
		entry.setSit2Visible(player2 == null);

		entryPanel.revalidate();
		entryPanel.repaint();
	}

	public void updateEntry(Table table) {
		int index = indexOf(table);
		if (index == -1)
			return;

		updateEntry(index, table);
	}

}
