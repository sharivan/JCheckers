package jcheckers.server.net;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import common.db.SQLCommand;
import common.process.Closeable;
import common.process.NonReturnableProcessWithoutArg;
import common.process.ReturnableProcess;
import common.process.timer.Timer;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.io.InputProtocol;
import jcheckers.server.io.OutputProtocol;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class User implements Closeable {

	private static final int RECONNECT_TIME = 7 * 60 * 1000; // 7 minutos

	private static AtomicInteger ID = new AtomicInteger(1000);

	public static final int CREATED = 0;
	public static final int JOINING = 1;
	public static final int OPEN = 2;
	public static final int RECONNECTING = 3;
	public static final int CLOSING = 4;
	public static final int CLOSED = 5;

	private Server server;
	private Connection connection;

	private String name;
	private String pcName;
	private String localIP;
	private int compid;

	private int id;
	private Base base;

	private int totalGames;
	private int wins;
	private int losses;
	private int abandoneds;
	private int bomb;

	private int adminLevel;
	private InetAddress ip;
	private int avatar;

	private Timer tmrReconnecting;

	protected User() {
		totalGames = 0;
		wins = 0;
		losses = 0;
		abandoneds = 0;
		bomb = 0;
	}

	@Override
	public void close() {
		closeNormal();
	}

	public void closeAndSetReconnecting() {
		post(() -> closeInternal(false));
	}

	protected void closeInternal(boolean normalExit) {
		Base base;
		Connection connection;
		synchronized (this) {
			if (server == null)
				return;

			base = this.base;
			connection = this.connection;
			this.connection = null;
			if (normalExit)
				server = null;

			if (!normalExit) {
				tmrReconnecting.reset();
				tmrReconnecting.play();
			} else
				try {
					tmrReconnecting.close();
				} finally {
					tmrReconnecting = null;
				}
		}

		if (connection != null)
			if (normalExit)
				connection.closeNormal();
			else
				connection.closeAndSetReconnecting();

		if (base != null)
			base.removeUser(this, normalExit);
	}

	public void closeNormal() {
		post(() -> closeInternal(true));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (!(obj instanceof User))
			return false;

		User other = (User) obj;

		return nameEquals(other);
	}

	public void fetchData() {
		fetchStats();
	}

	protected void fetchStats() {
		Server server;
		synchronized (this) {
			if (this.server == null)
				return;

			server = this.server;
		}

		try {
			server.executeStatsTransaction(name, (connection) -> {
				try (ResultSet rs = connection.selectAll(server.tableStats(), new String[] { "game", "user" }, new Object[] { server.getGameName(), name })) {
					if (!rs.next()) {
						List<Object> stats = getStats();
						stats.add(0, server.getGameName());
						stats.add(1, name);

						try {
							server.execute(SQLCommand.insert(server.tableStats(), stats.toArray()));
						} catch (InterruptedException e) {
							close();
						}
					} else
						readFromStats(rs);

					return null;
				}
			});
		} catch (SQLException e) {
			server.logToErr(toString(), null, e);
		} catch (InterruptedException e) {
			close();
		}
	}

	public int getAdminLevel() {
		return adminLevel;
	}

	public int getAvatar() {
		return avatar;
	}

	public synchronized Base getBase() {
		return base;
	}

	public synchronized int getBomb() {
		return bomb;
	}

	public int getCompID() {
		return compid;
	}

	public synchronized Connection getConnection() {
		return connection;
	}

	public Integer getID() {
		return id;
	}

	public long getInactiveTime() {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return 0;

			connection = this.connection;
		}

		return connection.getInactiveTime();
	}

	public InetAddress getIP() {
		return ip;
	}

	public String getLocalIP() {
		return localIP;
	}

	public String getName() {
		return name;
	}

	public String getPcName() {
		return pcName;
	}

	public synchronized int getReconnectingTime() {
		return isReconnecting() ? tmrReconnecting.getCurrentTime() : 0;
	}

	public int getRoomID() {
		Base base;
		synchronized (this) {
			if (this.base == null)
				return -1;

			base = this.base;
		}

		if (base instanceof Room)
			return ((Room) base).getID();

		return ((Table) base).getRoom().getID();
	}

	public String getRoomName() {
		Base base;
		synchronized (this) {
			if (this.base == null)
				return null;

			base = this.base;
		}

		if (base instanceof Room)
			return ((Room) base).getName();

		return ((Table) base).getRoom().getName();
	}

	public synchronized Server getServer() {
		return server;
	}

	protected List<Object> getStats() {
		ArrayList<Object> result = new ArrayList<>();

		result.add(totalGames);
		result.add(abandoneds);
		result.add(wins);
		result.add(losses);
		result.add(bomb);

		return result;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());

		return result;
	}

	public synchronized void interrupt() {
		server = null;
		notifyAll();
	}

	public synchronized boolean isAdmin() {
		return adminLevel >= 0;
	}

	public synchronized boolean isClosed() {
		return server == null;
	}

	public synchronized boolean isClosing() {
		return server == null;
	}

	public synchronized boolean isConnected() {
		return server != null && connection != null && base != null;
	}

	public synchronized boolean isConnecting() {
		return server != null && connection != null && base == null;
	}

	public synchronized boolean isReconnecting() {
		return server != null && connection == null;
	}

	public boolean nameEquals(User user) {
		return name.equalsIgnoreCase(user.getName());
	}

	protected void parseData(int opcode, JCheckersDataInputStream input) throws IOException {
		Server server;
		Base base;
		Connection connection;
		synchronized (this) {
			if (this.base == null || this.server == null || this.connection == null)
				return;

			server = this.server;
			base = this.base;
			connection = this.connection;
		}

		switch (opcode) {
			case InputProtocol.NO_CRYPT_NEXT_BLOCK:
				connection.noDecryptNextBlock();

				break;
			case InputProtocol.CHANGE_KEY:
				int method = input.read();
				connection.setInputMethod(method);

				break;
			case InputProtocol.EXIT:
				close();

				break;
			default:
				if (opcode == InputProtocol.CHANGE_ROOM) {
					if (!(base instanceof Room))
						return;

					Room lobby = (Room) base;

					int index = input.readInt();

					if (index == lobby.getIndex())
						return;

					base.removeUser(this, true);
					base = null;
					lobby = server.getLobbyByIndex(index);
					if (lobby == null) {
						OutputProtocol protocol = prepareProtocol();
						if (protocol != null)
							protocol.invalidRoom();
						close();

						return;
					}

					OutputProtocol protocol = prepareProtocol();
					if (protocol != null)
						protocol.roomChanged(lobby.getIndex());

					if (!lobby.join(this)) {
						close();

						return;
					}

					server.postRoomList(this);
				} else if (opcode != InputProtocol.PING_CONNECTION)
					base.parseData(this, opcode, input);
		}
	}

	public synchronized void post(NonReturnableProcessWithoutArg process) {
		if (connection != null)
			connection.post(process);
	}

	public void postBlock(byte[] buf) {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return;

			connection = this.connection;
		}

		connection.postBlock(buf);
	}

	public void postBlock(byte[] buf, boolean flush) {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return;

			connection = this.connection;
		}

		connection.postBlock(buf, flush);
	}

	public void postBlock(byte[] buf, int off, int len) {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return;

			connection = this.connection;
		}

		connection.postBlock(buf, off, len);
	}

	public void postBlock(byte[] buf, int off, int len, boolean flush) {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return;

			connection = this.connection;
		}

		connection.postBlock(buf, off, len, flush);
	}

	public JCheckersDataOutputStream prepareOutput() {
		Connection connection;
		synchronized (this) {
			if (this.connection == null)
				return null;

			connection = this.connection;
		}

		return connection.prepareOutput();
	}

	public OutputProtocol prepareProtocol() {
		Server server;
		synchronized (this) {
			if (this.server == null)
				return null;

			server = this.server;
		}

		JCheckersDataOutputStream output = prepareOutput();
		if (output == null)
			return null;

		return server.createOutputProtocol(output);
	}

	protected void readFromStats(ResultSet rs) throws SQLException {
		totalGames = rs.getInt("playeds");
		abandoneds = rs.getInt("abandoneds");
		wins = rs.getInt("wins");
		losses = rs.getInt("losses");
		bomb = rs.getInt("bomb");
	}

	public <T> T send(ReturnableProcess<T> process) {
		return connection != null ? connection.send(process) : null;
	}

	public void setAvatar(int avatar) {
		this.avatar = avatar;

		Base base;
		synchronized (this) {
			if (this.base == null)
				return;

			base = this.base;
		}

		if (base != null)
			base.notifyAvatars();
	}

	public void setBase(Base base) {
		Server server;
		synchronized (this) {
			if (this.server == null || this.base == base)
				return;

			server = this.server;
			this.base = base;
		}

		OutputProtocol protocol = prepareProtocol();
		if (protocol != null)
			protocol.myID(id, getRoomName());

		adminLevel = server.getAdminLevel(getRoomID(), name);
	}

	public void setConnection(Connection connection) {
		boolean wasReconnecting;
		synchronized (this) {
			if (this.connection != null)
				return;

			this.connection = connection;

			tmrReconnecting.pause();

			wasReconnecting = base != null;
		}

		if (wasReconnecting) {
			OutputProtocol protocol = prepareProtocol();
			if (protocol != null)
				protocol.myID(id, getRoomName());
		}
	}

	protected void setup(Server server, Connection connection, String name, String pcName, String localIP, int compid) {
		this.server = server;
		this.name = name;
		this.pcName = pcName;
		this.localIP = localIP;
		this.compid = compid;

		this.connection = null;

		id = ID.getAndUpdate((operand) -> {
			operand++;
			if (operand == 0)
				return 1000;
			return operand;
		});

		adminLevel = -1;
		ip = connection.getIP();

		tmrReconnecting = new Timer(server.getQueue(), RECONNECT_TIME, true, (e) -> server.logToErr(toString(), null, e));
		tmrReconnecting.addListener((timer, interval) -> {
			stopReconnecting();
		});

		setConnection(connection);
	}

	private void stopReconnecting() {
		Base base;
		synchronized (this) {
			if (!isReconnecting())
				return;

			tmrReconnecting.pause();

			base = User.this.base;
		}

		if (base != null)
			base.removeReconnecting(User.this);
	}

	public int tableCount() {
		Base base;
		synchronized (this) {
			if (this.base == null)
				return 0;

			base = this.base;
		}

		return base.tableCount(name);
	}

	public List<Table> tables() {
		Base base;
		synchronized (this) {
			if (this.base == null)
				return new ArrayList<>();

			base = this.base;
		}

		return base.tables(name);
	}

	@Override
	public String toString() {
		return "user " + name;
	}

	public void writeExtraInfo(JCheckersDataOutputStream output) {
		output.writeChar(tableCount());
	}

	public final void writeFullProfileData(JCheckersDataOutputStream output) {
		fetchData();

		writeFullProfileData1(output);
	}

	protected synchronized void writeFullProfileData1(JCheckersDataOutputStream output) {
		output.writeInt(totalGames);
		output.writeInt(abandoneds);
		output.writeInt(wins);
		output.writeInt(losses);
	}

	public final void writeProfileData(JCheckersDataOutputStream output) {
		fetchData();

		writeProfileData1(output);
	}

	protected void writeProfileData1(JCheckersDataOutputStream output) {

	}

}
