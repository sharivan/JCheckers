package jcheckers.server.net;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import common.db.SQLCommand;
import common.process.TimeOutException;
import common.process.timer.Timer;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;

public abstract class Room extends Base {

	public static final int PRIVATE = 1 << 1;
	public static final int WITH_ADMIN = 1 << 3;
	public static final int CURRENT_ROOM = 1 << 4;

	private int index;
	private int id;

	private String name;
	private int maxPlayers;

	private boolean withAdmin;
	private boolean isPublic;

	private Vector<Table> tables;
	private Hashtable<Integer, Table> tablesHash;

	private Timer tmbUpdateDB;

	protected Room() {
		super();
	}

	@Override
	protected void afterClose() {
		super.afterClose();

		tables.clear();
		tablesHash.clear();
	}

	@Override
	protected void beforeClose() {
		super.beforeClose();

		List<Table> tables;
		synchronized (this.tables) {
			for (Table table : this.tables)
				objectsToClose += table.closingMaxScore();
			tables = tables();
		}

		try {
			tmbUpdateDB.close();
		} finally {
			tmbUpdateDB = null;
		}

		for (Table table : tables) {
			int oldObjectsClosed = objectsClosed;
			logToOut(toString(), "Closing " + table + "...");
			while (true) {
				int oldScore = 0;
				try {
					table.close(true);

					break;
				} catch (TimeOutException e) {
					int score = table.closingScore();
					if (score <= oldScore) {
						table.interrupt();
						server.logToErr(toString(), "WARNING: The " + table + " was forced closed.");

						break;
					}
					oldScore = score;
				} finally {
					synchronized (this) {
						objectsClosed = oldObjectsClosed + table.closingScore();
					}
				}
			}
			logToOut(toString(), table + " closed.");
		}
	}

	public final boolean closeTable(Table table) {
		return closeTable(table, false);
	}

	public final boolean closeTable(Table table, boolean wait) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return false;
		}

		if (!tables.contains(table))
			return false;

		if (wait)
			post(() -> closeTableInternal(table));
		else
			send(() -> closeTableInternal(table));

		return true;
	}

	protected boolean closeTableInternal(Table table) {
		if (!tables.remove(table))
			return false;

		tablesHash.remove(table.getID());
		table.close();

		OutputProtocol protocol = prepareBroadCastProtocol();
		if (protocol != null)
			protocol.closeTable(table.getID());

		if (server != null)
			server.notifyUpdated(Room.this);

		return true;
	}

	public final Table createTable(User user, JCheckersDataInputStream input) {
		return send(() -> {
			try {
				Table table;
				synchronized (tables) {
					int number = 1;
					for (Table table1 : tables)
						if (table1.getNumber() == number)
							number++;

					table = server.getTableClass().newInstance();
					table.open(server, this, number, input);
					tables.add(table);
					tablesHash.put(table.getID(), table);
				}

				int sitIndex = input.readUChar();
				user.setBase(table);
				table.joinPrivileged(user, sitIndex, InputProtocol.CREATE_TABLE);

				notifyUpdated(user.getName());
				server.notifyUpdated(this);

				return table;
			} catch (IOException | IllegalAccessException | InstantiationException e) {
				server.logToErr("Room " + name, e);
			}

			return null;
		});
	}

	public int getID() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public File getLogDir() {
		String homeDir = server.getHomeDir();
		String logDir = server.getLogDir();
		File home = new File(homeDir);
		File file = new File(logDir);
		if (!file.isAbsolute())
			file = new File(home, logDir);
		file = new File(file, "rooms");

		return file;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public String getName() {
		return name;
	}

	@Override
	public final Room getRoom() {
		return this;
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

	@Override
	public User getUserByIDExt(int id) {
		User result = getUserByID(id);
		if (result != null)
			return result;

		synchronized (tables) {
			for (Table table : tables) {
				result = table.getUserByID(id);
				if (result != null)
					return result;
			}

			return null;
		}
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isWithAdmin() {
		return withAdmin;
	}

	public boolean join(User user) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return false;
		}

		if (userCount() >= maxPlayers && !user.isAdmin()) {
			OutputProtocol protocol = user.prepareProtocol();
			if (protocol == null)
				return false;

			protocol.couldNotConnectToTheRoom();

			return false;
		}

		if (!super.addUser(user)) {
			OutputProtocol protocol = user.prepareProtocol();
			if (protocol == null)
				return false;

			protocol.couldNotConnectToTheRoom();

			return false;
		}

		logToOut("User " + user.getName() + " joined [compid=" + user.getCompID() + " ip=" + user.getIP().getHostAddress() + "]");

		OutputProtocol protocol = user.prepareProtocol();
		if (protocol == null)
			return false;

		protocol.wellcome(name);

		protocol = prepareBroadCastProtocol(user);
		if (protocol != null)
			protocol.joinUser(this, user);

		postUserList(user);

		List<Table> tables = tables();
		for (Table table : tables) {
			protocol = table.prepareBroadCastProtocol();
			if (protocol != null)
				protocol.joinUser(this, user);

			table.postState(user);
		}

		if (server != null)
			server.notifyUpdated(this);

		return true;
	}

	protected void open(Server server, int index, int id, String name, int maxPlayers, boolean withAdmin, boolean isPublic) throws IOException {
		this.index = index;
		this.id = id;
		this.name = name;
		this.maxPlayers = maxPlayers;
		this.withAdmin = withAdmin;
		this.isPublic = isPublic;

		super.open(new ThreadGroup(server.getGroup(), toString()), server);

		tables = new Vector<>();
		tablesHash = new Hashtable<>();

		getQueue().setName("Room queue [" + name + "]");

		tmbUpdateDB = new Timer(getQueue(), 60000, (e) -> server.logToErr(toString(), null, e));
		tmbUpdateDB.addListener((sender, interval) -> updateUserCountInDB());
	}

	public void postUserList(User user) {
		synchronized (this) {
			if (isClosing() || isClosed())
				return;
		}

		OutputProtocol protocol = user.prepareProtocol();
		if (protocol == null)
			return;

		protocol.userList(this, users(true));
	}

	@Override
	protected boolean removeUserInternal(User user, boolean normalExit) {
		if (super.removeUserInternal(user, normalExit)) {
			logToOut(user.getName() + " left");

			OutputProtocol protocol = prepareBroadCastProtocol();
			if (protocol != null)
				protocol.leaveUser(user.getID());

			synchronized (tables) {
				for (Table table : tables) {
					protocol = table.prepareBroadCastProtocol();
					if (protocol != null)
						protocol.leaveUser(user.getID());
				}
			}

			server.notifyUpdated(this);

			return true;
		}

		return false;
	}

	public final void stopAllGames() {
		stopAllGames(StopReason.NORMAL);
	}

	public final void stopAllGames(StopReason reason) {
		synchronized (tables) {
			for (Table table : tables)
				table.getGame().stop(reason);
		}
	}

	public int tableCount() {
		return tables.size();
	}

	@Override
	public int tableCount(String user) {
		synchronized (tables) {
			int result = 0;
			for (Table table : tables)
				if (table.containsUser(user))
					result++;

			return result;
		}
	}

	public List<Table> tables() {
		synchronized (tables) {
			return new ArrayList<>(tables);
		}
	}

	@Override
	public List<Table> tables(String user) {
		synchronized (tables) {
			ArrayList<Table> result = new ArrayList<>();
			for (Table table : tables)
				if (table.containsUser(user))
					result.add(table);

			return result;
		}
	}

	@Override
	public String toString() {
		return "room " + name;
	}

	private void updateUserCountInDB() {
		try {
			server.executeUpdate(
					SQLCommand.update(DBConsts.TABLE_ROOMS, new String[] { "game", "number" }, new Object[] { server.getGameName(), getID() }, new String[] { "users" }, new Object[] { userCount() }));
		} catch (InterruptedException e) {
		} catch (SQLException e) {
			server.logToErr("Room " + name, null, e);
		}
	}

	@Override
	public void writeUser(User user, JCheckersDataOutputStream output) {
		output.writeInt(user.getID());
		output.writeString(user.getName());
	}

}
