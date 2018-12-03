package jcheckers.client.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jcheckers.client.io.InputProtocol;
import jcheckers.common.io.JCheckersDataInputStream;

public abstract class Room extends Base {

	public static final int PRIVATE = 1 << 1;
	public static final int WITH_ADMIN = 1 << 3;
	public static final int CURRENT_ROOM = 1 << 4;

	private int index = -1;
	private int maxPlayers;

	private boolean withAdmin;
	private boolean isPublic;

	private Vector<Table> tables;
	private Hashtable<Integer, Table> tablesHash;

	protected Room(Connection connection) {
		super(connection);

		tables = new Vector<>();
		tablesHash = new Hashtable<>();
	}

	public int getIndex() {
		return index;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public Table getTableByID(int id) {
		return tablesHash.get(id);
	}

	public Table getTableByNumber(int number) {
		synchronized (tables) {
			for (Table table : tables)
				if (table.getNumber() == number)
					return table;

			return null;
		}
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isWithAdmin() {
		return withAdmin;
	}

	@Override
	protected void parseData(int opcode, JCheckersDataInputStream in) throws IOException {
		switch (opcode) {
			case InputProtocol.UPDATE_TABLE: {
				int id = in.readInt();
				Table table = tablesHash.get(id);
				if (table == null) {
					table = connection.createTable(getRoomID(), id, in);
					tables.add(table);
					tablesHash.put(id, table);

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof RoomConnectionListener)
							try {
								((RoomConnectionListener) listener).onNewTable(connection, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}
				} else {
					table.readFrom(in);

					for (ConnectionListener listener : listeners)
						if (listener != null && listener instanceof RoomConnectionListener)
							try {
								((RoomConnectionListener) listener).onUpdateTable(connection, table);
							} catch (Throwable e) {
								e.printStackTrace();
							}
				}

				break;
			}

			case InputProtocol.CLOSE_TABLE: {
				int id = in.readInt();
				Table table = tablesHash.remove(id);
				tables.remove(table);

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof RoomConnectionListener)
						try {
							((RoomConnectionListener) listener).onRemoveTable(connection, table);
						} catch (Throwable e) {
							e.printStackTrace();
						}
			}

			default:
				super.parseData(opcode, in);
		}
	}

	public int tableCount() {
		return tables.size();
	}

	public List<Table> tables() {
		synchronized (tables) {
			return new ArrayList<>(tables);
		}
	}

	@Override
	public String toString() {
		return "room " + getRoomName();
	}

}
